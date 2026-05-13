# Infrastructure Design - Resource Service

## Infrastructure Mapping

| Logical Component | Infrastructure | Configuration |
|---|---|---|
| Resource Service App | Docker container (Spring Boot) | Port 8084 |
| MongoDB (resource-db) | Shared MongoDB instance, separate DB | mongodb://mongodb:27017/resource-db |
| RabbitMQ | Shared RabbitMQ instance | rabbitmq:5672 |
| LevenshteinCalculator | In-app domain service | No external infra |
| Hard Delete Scheduler | In-app @Scheduled | Daily 03:00 |

---

## MongoDB Configuration

| Property | Value |
|---|---|
| Database | resource-db |
| Collections | resources |
| Authentication | 미적용 (Docker 내부 네트워크) |

### Indexes
```javascript
// Partial unique index (deleted=false만)
db.resources.createIndex(
    { "resourceKey": 1 },
    { unique: true, partialFilterExpression: { deleted: false } }
)

// Compound index for search
db.resources.createIndex({ "deleted": 1, "resourceType": 1, "createdAt": -1 })

// Text index for keyword search
db.resources.createIndex({ "resourceKey": "text", "content": "text" })

// Hard delete scheduler
db.resources.createIndex({ "deletedAt": 1 })
```

---

## RabbitMQ Configuration

| Property | Value |
|---|---|
| Exchange | mkfmm.resource (topic, durable) |
| DLX | mkfmm.resource.dlx (direct, durable) |
| DLQ | mkfmm.resource.dlq (durable) |

---

## Docker Compose (Resource Service 부분)

```yaml
resource-service:
  build:
    context: ./backend/resource-service
    dockerfile: Dockerfile
  ports:
    - "8084:8084"
  environment:
    - SPRING_DATA_MONGODB_URI=mongodb://mongodb:27017/resource-db
    - SPRING_RABBITMQ_HOST=rabbitmq
    - SPRING_RABBITMQ_PORT=5672
    - APP_SIMILARITY_THRESHOLD=3
    - APP_SIMILARITY_MAX_RESULTS=5
    - APP_HARD_DELETE_RETENTION_DAYS=30
  depends_on:
    mongodb:
      condition: service_healthy
    rabbitmq:
      condition: service_healthy
  networks:
    - mkfmm-network
  restart: on-failure:3
  healthcheck:
    test: ["CMD", "curl", "-f", "http://localhost:8084/actuator/health"]
    interval: 30s
    timeout: 10s
    retries: 3
    start_period: 40s
```

---

## Environment Variables

| Variable | Value | Purpose |
|---|---|---|
| SPRING_DATA_MONGODB_URI | mongodb://mongodb:27017/resource-db | MongoDB 연결 |
| SPRING_RABBITMQ_HOST | rabbitmq | RabbitMQ 호스트 |
| SPRING_RABBITMQ_PORT | 5672 | RabbitMQ 포트 |
| APP_SIMILARITY_THRESHOLD | 3 | Levenshtein 임계값 |
| APP_SIMILARITY_MAX_RESULTS | 5 | 유사 후보 최대 수 |
| APP_HARD_DELETE_RETENTION_DAYS | 30 | Soft Delete 보존 기간 |

---

## Dockerfile

```dockerfile
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY build/libs/resource-service-*.jar app.jar
EXPOSE 8084
ENTRYPOINT ["java", "-jar", "app.jar"]
```

---

## Network & Dependencies

```
mongodb (healthy) ──┐
                    ├──→ resource-service:8084
rabbitmq (healthy) ─┘
```

- API Gateway → resource-service (내부: http://resource-service:8084)
- DataIO Service → resource-service (REST 호출: 중복 검증, 리소스 저장)

# Infrastructure Design - History Service

## Infrastructure Mapping

| Logical Component | Infrastructure | Configuration |
|---|---|---|
| History Service App | Docker container (Spring Boot) | Port 8083 |
| MongoDB (history-db) | Shared MongoDB instance, separate DB | mongodb://mongodb:27017/history-db |
| RabbitMQ Consumer | Shared RabbitMQ instance | mkfmm.history.events queue |
| Retention Scheduler | In-app @Scheduled | 04:00 + 04:30 |

## MongoDB Indexes

```javascript
db.work_logs.createIndex({ "sourceEvent": 1 }, { unique: true })
db.work_logs.createIndex({ "workLogType": 1, "performedAt": -1 })
db.work_logs.createIndex({ "userId": 1, "performedAt": -1 })
db.work_logs.createIndex({ "markedForDeletion": 1 })
db.work_logs.createIndex({ "performedAt": 1 })
```

## RabbitMQ Configuration

| Property | Value |
|---|---|
| Queue | mkfmm.history.events (durable) |
| Bindings | mkfmm.auth/#, mkfmm.user/#, mkfmm.resource/#, mkfmm.dataio/#, mkfmm.deploy/# |
| DLQ | mkfmm.history.dlq |
| Ack Mode | Manual |

## Docker Compose

```yaml
history-service:
  build:
    context: ./backend/history-service
    dockerfile: Dockerfile
  ports:
    - "8083:8083"
  environment:
    - SPRING_DATA_MONGODB_URI=mongodb://mongodb:27017/history-db
    - SPRING_RABBITMQ_HOST=rabbitmq
    - SPRING_RABBITMQ_PORT=5672
    - APP_RETENTION_SEARCH_DAYS=180
  depends_on:
    mongodb:
      condition: service_healthy
    rabbitmq:
      condition: service_healthy
  networks:
    - mkfmm-network
  restart: on-failure:3
  healthcheck:
    test: ["CMD", "curl", "-f", "http://localhost:8083/actuator/health"]
    interval: 30s
    timeout: 10s
    retries: 3
    start_period: 40s
```

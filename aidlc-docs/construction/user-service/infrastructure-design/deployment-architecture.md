# Deployment Architecture - User Service

## Docker Compose (User Service 부분)

```yaml
# docker-compose.yml (user-service 관련 발췌)

services:
  user-service:
    build:
      context: ./backend/user-service
      dockerfile: Dockerfile
    ports:
      - "8082:8082"
    environment:
      - SPRING_DATA_MONGODB_URI=mongodb://mongodb:27017/user-db
      - SPRING_RABBITMQ_HOST=rabbitmq
      - SPRING_RABBITMQ_PORT=5672
      - APP_AUDIT_LOG_RETENTION_DAYS=730
      - MKFMM_ROOT_USER=${MKFMM_ROOT_USER}
    depends_on:
      mongodb:
        condition: service_healthy
      rabbitmq:
        condition: service_healthy
    networks:
      - mkfmm-network
    restart: on-failure:3
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:8082/actuator/health"]
      interval: 30s
      timeout: 10s
      retries: 3
      start_period: 40s
```

---

## Dockerfile

```dockerfile
# backend/user-service/Dockerfile

FROM eclipse-temurin:17-jdk-alpine AS build
WORKDIR /app
COPY pom.xml .
COPY ../shared/pom.xml ../shared/pom.xml
RUN mvn dependency:go-offline -B
COPY src ./src
COPY ../shared/src ../shared/src
RUN mvn package -DskipTests -pl user-service -am

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/target/user-service-*.jar app.jar
EXPOSE 8082
ENTRYPOINT ["java", "-jar", "app.jar"]
```

---

## Shared Infrastructure (전체 프로젝트 공통)

```yaml
# docker-compose.yml (공유 인프라 부분)

services:
  mongodb:
    image: mongo:7
    ports:
      - "27017:27017"
    volumes:
      - mongodb-data:/data/db
    networks:
      - mkfmm-network
    healthcheck:
      test: ["CMD", "mongosh", "--eval", "db.adminCommand('ping')"]
      interval: 10s
      timeout: 5s
      retries: 5

  rabbitmq:
    image: rabbitmq:3-management
    ports:
      - "5672:5672"
      - "15672:15672"
    volumes:
      - rabbitmq-data:/var/lib/rabbitmq
    networks:
      - mkfmm-network
    healthcheck:
      test: ["CMD", "rabbitmq-diagnostics", "check_port_connectivity"]
      interval: 10s
      timeout: 5s
      retries: 5

volumes:
  mongodb-data:
  rabbitmq-data:

networks:
  mkfmm-network:
    driver: bridge
```

---

## Service Dependency Graph (시작 순서)

```
mongodb (healthy) ──┐
                    ├──→ user-service
rabbitmq (healthy) ─┘
```

---

## Health Check & Recovery

| Aspect | Strategy |
|---|---|
| Health Check | Spring Actuator /actuator/health (30s interval) |
| Failure Detection | 3회 연속 실패 시 unhealthy |
| Recovery | Docker restart on-failure (최대 3회) |
| MongoDB 연결 실패 | Spring retry → 서비스 unhealthy → restart |
| RabbitMQ 연결 실패 | Spring AMQP auto-reconnect |

---

## Local Development

```bash
# 전체 인프라 + User Service 실행
docker-compose up mongodb rabbitmq user-service

# User Service만 로컬에서 실행 (IDE 디버깅)
docker-compose up mongodb rabbitmq
cd backend/user-service
mvn spring-boot:run
```

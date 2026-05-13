# Infrastructure Design - User Service

## Infrastructure Mapping

| Logical Component | Infrastructure | Configuration |
|---|---|---|
| User Service App | Docker container (Spring Boot) | Port 8082, JVM default memory |
| MongoDB (user-db) | Shared MongoDB instance, separate DB | mongodb://mongodb:27017/user-db |
| RabbitMQ | Shared RabbitMQ instance | rabbitmq:5672 |
| DLQ Management | User Service 내장 API | /admin/dlq/* endpoints |
| AuditLog Scheduler | User Service 내장 @Scheduled | Daily 02:00 cron |

---

## MongoDB Configuration

| Property | Value |
|---|---|
| Instance | 단일 공유 MongoDB 컨테이너 (mongo:7) |
| Database | user-db (User Service 전용) |
| Collections | users, audit_logs |
| Authentication | 미적용 (Docker 내부 네트워크, 사내 환경) |
| Storage | Docker volume (mongodb-data) |

### Indexes (초기 생성)
```javascript
// users collection
db.users.createIndex({ "userId": 1 }, { unique: true })
db.users.createIndex({ "name": 1, "department": 1 })
db.users.createIndex({ "role": 1 })

// audit_logs collection
db.audit_logs.createIndex({ "action": 1, "targetUserId": 1 })
db.audit_logs.createIndex({ "performedBy": 1 })
db.audit_logs.createIndex({ "performedAt": 1 })
```

---

## RabbitMQ Configuration

| Property | Value |
|---|---|
| Instance | 단일 공유 RabbitMQ 컨테이너 (rabbitmq:3-management) |
| Connection | rabbitmq:5672 (AMQP), rabbitmq:15672 (Management UI) |
| Virtual Host | / (default) |
| Exchange | mkfmm.user (type: topic, durable: true) |
| DLX | mkfmm.user.dlx (type: direct, durable: true) |
| DLQ | mkfmm.user.dlq (durable: true) |

---

## Network

| Service | Docker Network | Internal Access |
|---|---|---|
| user-service | mkfmm-network (bridge) | http://user-service:8082 |
| mongodb | mkfmm-network | mongodb://mongodb:27017 |
| rabbitmq | mkfmm-network | amqp://rabbitmq:5672 |
| api-gateway | mkfmm-network | 외부 → gateway → user-service |

---

## Docker Container

| Property | Value |
|---|---|
| Image | Build from backend/user-service/Dockerfile |
| Base Image | eclipse-temurin:17-jre-alpine |
| Port | 8082 (internal) |
| Resource Limits | 미설정 (사내 환경) |
| Restart Policy | on-failure (max 3) |
| Health Check | GET /actuator/health |
| Environment Variables | SPRING_DATA_MONGODB_URI, SPRING_RABBITMQ_HOST, etc. |

---

## Environment Variables

| Variable | Value | Purpose |
|---|---|---|
| SPRING_DATA_MONGODB_URI | mongodb://mongodb:27017/user-db | MongoDB 연결 |
| SPRING_RABBITMQ_HOST | rabbitmq | RabbitMQ 호스트 |
| SPRING_RABBITMQ_PORT | 5672 | RabbitMQ 포트 |
| APP_AUDIT_LOG_RETENTION_DAYS | 730 | 감사 로그 보존 (2년) |
| MKFMM_ROOT_USER | (env에서 주입) | Root Admin userId |

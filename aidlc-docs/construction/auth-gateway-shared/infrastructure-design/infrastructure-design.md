# Infrastructure Design - Auth Service + API Gateway + Shared Library

## 1. Docker Compose Service Definitions (Dev 1 Scope)

### Auth Service
```yaml
auth-service:
  build:
    context: ./backend/auth-service
    dockerfile: Dockerfile
  container_name: mkfmm-auth-service
  ports:
    - "8081:8081"
  environment:
    - SPRING_PROFILES_ACTIVE=docker
    - MONGODB_URI=mongodb://mongodb:27017/mkfmm-auth
    - RABBITMQ_HOST=rabbitmq
    - RABBITMQ_PORT=5672
    - RABBITMQ_USER=guest
    - RABBITMQ_PASS=guest
    - MKFMM_ROOT_USER=rootadmin
    - MKFMM_ROOT_PASSWORD=changeme123
    - JWT_PRIVATE_KEY_PATH=/app/keys/private.pem
    - JWT_PUBLIC_KEY_PATH=/app/keys/public.pem
  volumes:
    - jwt-keys:/app/keys:ro
  depends_on:
    mongodb:
      condition: service_healthy
    rabbitmq:
      condition: service_healthy
  healthcheck:
    test: ["CMD", "curl", "-f", "http://localhost:8081/actuator/health"]
    interval: 30s
    timeout: 10s
    retries: 3
    start_period: 40s
  restart: on-failure
  networks:
    - mkfmm-network
```

### API Gateway
```yaml
api-gateway:
  build:
    context: ./backend/api-gateway
    dockerfile: Dockerfile
  container_name: mkfmm-api-gateway
  ports:
    - "8080:8080"
  environment:
    - SPRING_PROFILES_ACTIVE=docker
    - JWT_PUBLIC_KEY_PATH=/app/keys/public.pem
    - CORS_ALLOWED_ORIGINS=http://localhost:3000
    - AUTH_SERVICE_URL=http://auth-service:8081
    - USER_SERVICE_URL=http://user-service:8082
    - HISTORY_SERVICE_URL=http://history-service:8083
    - RESOURCE_SERVICE_URL=http://resource-service:8084
    - DATAIO_SERVICE_URL=http://dataio-service:8085
    - DEPLOY_SERVICE_URL=http://deploy-service:8086
  volumes:
    - jwt-keys:/app/keys:ro
  depends_on:
    auth-service:
      condition: service_healthy
  healthcheck:
    test: ["CMD", "curl", "-f", "http://localhost:8080/actuator/health"]
    interval: 30s
    timeout: 10s
    retries: 3
    start_period: 30s
  restart: on-failure
  networks:
    - mkfmm-network
```

### Infrastructure Services (MongoDB + RabbitMQ)
```yaml
mongodb:
  image: mongo:7
  container_name: mkfmm-mongodb
  ports:
    - "27017:27017"
  volumes:
    - mongodb-data:/data/db
  healthcheck:
    test: ["CMD", "mongosh", "--eval", "db.adminCommand('ping')"]
    interval: 10s
    timeout: 5s
    retries: 5
    start_period: 20s
  networks:
    - mkfmm-network

rabbitmq:
  image: rabbitmq:3-management
  container_name: mkfmm-rabbitmq
  ports:
    - "5672:5672"
    - "15672:15672"
  environment:
    - RABBITMQ_DEFAULT_USER=guest
    - RABBITMQ_DEFAULT_PASS=guest
  volumes:
    - rabbitmq-data:/var/lib/rabbitmq
  healthcheck:
    test: ["CMD", "rabbitmq-diagnostics", "check_port_connectivity"]
    interval: 10s
    timeout: 5s
    retries: 5
    start_period: 30s
  networks:
    - mkfmm-network
```

### Volumes & Networks
```yaml
volumes:
  mongodb-data:
  rabbitmq-data:
  jwt-keys:

networks:
  mkfmm-network:
    driver: bridge
```

---

## 2. Network Topology

```
                    [External]
                        |
                   port 8080
                        |
              +-------------------+
              |   API Gateway     |
              |   (8080)          |
              +-------------------+
                   |         |
        +----------+         +----------+
        |                               |
+---------------+              +------------------+
| auth-service  |              | other services   |
| (8081)        |              | (8082-8086)      |
+---------------+              +------------------+
        |                               |
        +----------+         +----------+
                   |         |
              +-------------------+
              |    MongoDB        |
              |    (27017)        |
              +-------------------+
              
              +-------------------+
              |    RabbitMQ       |
              |  (5672 / 15672)  |
              +-------------------+
```

### Internal Communication
| From | To | Protocol | Purpose |
|---|---|---|---|
| API Gateway | auth-service:8081 | HTTP (internal) | Auth API 라우팅 |
| API Gateway | user-service:8082 | HTTP (internal) | User API 라우팅 |
| API Gateway | resource-service:8084 | HTTP (internal) | Resource API 라우팅 |
| API Gateway | dataio-service:8085 | HTTP (internal) | DataIO API 라우팅 |
| API Gateway | deploy-service:8086 | HTTP (internal) | Deploy API 라우팅 |
| API Gateway | history-service:8083 | HTTP (internal) | History API 라우팅 |
| auth-service | mongodb:27017 | MongoDB protocol | 데이터 저장 |
| auth-service | rabbitmq:5672 | AMQP | 이벤트 발행 |

### External Ports (Host-exposed)
| Port | Service | Access |
|---|---|---|
| 8080 | API Gateway | Frontend → Backend 진입점 |
| 3000 | Frontend | 브라우저 접속 (Dev 3 담당) |
| 27017 | MongoDB | 개발 시 직접 접속용 |
| 15672 | RabbitMQ Management | 개발 시 큐 모니터링 |

---

## 3. Dockerfile - Auth Service

```dockerfile
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY build/libs/auth-service-*.jar app.jar
RUN apk add --no-cache curl
EXPOSE 8081
HEALTHCHECK --interval=30s --timeout=10s --retries=3 --start-period=40s \
  CMD curl -f http://localhost:8081/actuator/health || exit 1
ENTRYPOINT ["java", "-jar", "app.jar"]
```

## 4. Dockerfile - API Gateway

```dockerfile
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY build/libs/api-gateway-*.jar app.jar
RUN apk add --no-cache curl
EXPOSE 8080
HEALTHCHECK --interval=30s --timeout=10s --retries=3 --start-period=30s \
  CMD curl -f http://localhost:8080/actuator/health || exit 1
ENTRYPOINT ["java", "-jar", "app.jar"]
```

---

## 5. JWT Key Volume Strategy

### Key Generation (One-time setup)
```bash
# 프로젝트 루트에서 실행
mkdir -p keys/
openssl genrsa -out keys/private.pem 2048
openssl rsa -in keys/private.pem -pubout -out keys/public.pem
```

### Docker Volume Mount
- Named volume `jwt-keys`에 private.pem + public.pem 저장
- Auth Service: read-only mount (`/app/keys/`)
- API Gateway: read-only mount (`/app/keys/`) — public.pem만 사용

### 개발 환경 대안
개발 시에는 bind mount 사용 가능:
```yaml
volumes:
  - ./keys:/app/keys:ro
```

---

## 6. MongoDB Database Separation

각 서비스는 논리적으로 별도 DB를 사용 (단일 MongoDB 인스턴스 내):

| Service | Database Name | Collections |
|---|---|---|
| auth-service | mkfmm-auth | users, refresh_tokens, login_history, account_lock_events |
| user-service | mkfmm-user | users, audit_logs |
| history-service | mkfmm-history | work_logs, processed_events |
| resource-service | mkfmm-resource | resources |
| dataio-service | mkfmm-dataio | import_jobs |
| deploy-service | mkfmm-deploy | deployments |

### Auth Service Collections
| Collection | Purpose | Indexes |
|---|---|---|
| users | 사용자 인증 정보 | userId (unique) |
| refresh_tokens | Refresh Token 저장 | token (unique), userId, expiresAt (TTL) |
| login_history | 로그인 이력 | userId + loginAt (compound), loginAt (for queries) |
| account_lock_events | 계정 잠금 이력 | userId, lockedAt |

---

## 7. RabbitMQ Infrastructure

### Exchange & Queue Setup (Auto-declaration via Spring AMQP)
```
Exchange: mkfmm.events
  Type: topic
  Durable: true

Queue: history.auth-events
  Durable: true
  Arguments:
    x-dead-letter-exchange: mkfmm.events.dlx
    x-dead-letter-routing-key: dlq

Binding: mkfmm.events → history.auth-events
  Routing keys: auth.#

DLX Exchange: mkfmm.events.dlx
  Type: direct
  Durable: true

DLQ Queue: mkfmm.events.dlq
  Durable: true

Binding: mkfmm.events.dlx → mkfmm.events.dlq
  Routing key: dlq
```

### Connection Configuration
```yaml
spring:
  rabbitmq:
    host: ${RABBITMQ_HOST:localhost}
    port: ${RABBITMQ_PORT:5672}
    username: ${RABBITMQ_USER:guest}
    password: ${RABBITMQ_PASS:guest}
    publisher-confirm-type: correlated
    publisher-returns: true
```

---

## 8. Spring Configuration Profiles

### Auth Service - application.yml
```yaml
server:
  port: 8081

spring:
  application:
    name: auth-service
  data:
    mongodb:
      uri: ${MONGODB_URI:mongodb://localhost:27017/mkfmm-auth}

management:
  endpoints:
    web:
      exposure:
        include: health
  endpoint:
    health:
      probes:
        enabled: true
  health:
    mongo:
      enabled: true
    rabbit:
      enabled: true

jwt:
  private-key-path: ${JWT_PRIVATE_KEY_PATH:classpath:keys/private.pem}
  public-key-path: ${JWT_PUBLIC_KEY_PATH:classpath:keys/public.pem}
  access-token-ttl: 900
  refresh-token-ttl: 604800
  issuer: mkfmm-auth

mkfmm:
  root:
    user: ${MKFMM_ROOT_USER:rootadmin}
    password: ${MKFMM_ROOT_PASSWORD:changeme123}
```

### API Gateway - application.yml
```yaml
server:
  port: 8080

spring:
  application:
    name: api-gateway
  cloud:
    gateway:
      routes:
        - id: auth-service
          uri: ${AUTH_SERVICE_URL:http://localhost:8081}
          predicates:
            - Path=/api/auth/**
          filters:
            - StripPrefix=1
        - id: user-service
          uri: ${USER_SERVICE_URL:http://localhost:8082}
          predicates:
            - Path=/api/users/**
          filters:
            - StripPrefix=1
        - id: resource-service
          uri: ${RESOURCE_SERVICE_URL:http://localhost:8084}
          predicates:
            - Path=/api/resources/**
          filters:
            - StripPrefix=1
        - id: dataio-service
          uri: ${DATAIO_SERVICE_URL:http://localhost:8085}
          predicates:
            - Path=/api/dataio/**
          filters:
            - StripPrefix=1
        - id: deploy-service
          uri: ${DEPLOY_SERVICE_URL:http://localhost:8086}
          predicates:
            - Path=/api/deploy/**
          filters:
            - StripPrefix=1
        - id: history-service
          uri: ${HISTORY_SERVICE_URL:http://localhost:8083}
          predicates:
            - Path=/api/history/**
          filters:
            - StripPrefix=1

jwt:
  public-key-path: ${JWT_PUBLIC_KEY_PATH:classpath:keys/public.pem}
  issuer: mkfmm-auth

cors:
  allowed-origins: ${CORS_ALLOWED_ORIGINS:http://localhost:3000}

management:
  endpoints:
    web:
      exposure:
        include: health
```

---

## 9. Build Configuration (Gradle Multi-Module)

### Root settings.gradle.kts
```kotlin
rootProject.name = "mkfmm-backend"

include(
    "shared",
    "auth-service",
    "api-gateway"
)

project(":shared").projectDir = file("backend/shared")
project(":auth-service").projectDir = file("backend/auth-service")
project(":api-gateway").projectDir = file("backend/api-gateway")
```

### Shared Library - build.gradle.kts
```kotlin
plugins {
    java
    id("java-library")
}

dependencies {
    api("com.fasterxml.jackson.core:jackson-databind")
    api("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")
}
```

### Auth Service - build.gradle.kts (key dependencies)
```kotlin
dependencies {
    implementation(project(":shared"))
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-data-mongodb")
    implementation("org.springframework.boot:spring-boot-starter-amqp")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.security:spring-security-crypto")
    implementation("io.jsonwebtoken:jjwt-api:0.12.5")
    runtimeOnly("io.jsonwebtoken:jjwt-impl:0.12.5")
    runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.12.5")
    
    testImplementation("net.jqwik:jqwik:1.8.4")
    testImplementation("org.testcontainers:mongodb")
    testImplementation("org.testcontainers:rabbitmq")
}
```

### API Gateway - build.gradle.kts (key dependencies)
```kotlin
dependencies {
    implementation(project(":shared"))
    implementation("org.springframework.cloud:spring-cloud-starter-gateway")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("io.jsonwebtoken:jjwt-api:0.12.5")
    runtimeOnly("io.jsonwebtoken:jjwt-impl:0.12.5")
    runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.12.5")
    
    testImplementation("net.jqwik:jqwik:1.8.4")
}
```

---

## 10. Development Workflow

### Local Development (without Docker)
```bash
# 1. MongoDB & RabbitMQ만 Docker로 실행
docker compose up mongodb rabbitmq -d

# 2. JWT keys 생성 (최초 1회)
mkdir -p keys && openssl genrsa -out keys/private.pem 2048 && openssl rsa -in keys/private.pem -pubout -out keys/public.pem

# 3. Auth Service 실행 (default profile)
cd backend/auth-service && ./gradlew bootRun

# 4. API Gateway 실행 (default profile)
cd backend/api-gateway && ./gradlew bootRun
```

### Full Docker Deployment
```bash
# 모든 서비스 빌드 & 실행
docker compose up --build -d

# 로그 확인
docker compose logs -f auth-service api-gateway
```

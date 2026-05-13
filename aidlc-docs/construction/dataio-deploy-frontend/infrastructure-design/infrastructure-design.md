# Infrastructure Design - DataIO Service + Deploy Service + Frontend

## 1. Docker Compose Service Definitions (Dev 3 Scope)

### DataIO Service
```yaml
dataio-service:
  build:
    context: ./backend/dataio-service
    dockerfile: Dockerfile
  container_name: mkfmm-dataio-service
  ports:
    - "8085:8085"
  environment:
    - SPRING_PROFILES_ACTIVE=docker
    - MONGODB_URI=mongodb://mongodb:27017/mkfmm-dataio
    - RABBITMQ_HOST=rabbitmq
    - RABBITMQ_PORT=5672
    - RABBITMQ_USER=guest
    - RABBITMQ_PASS=guest
    - RESOURCE_SERVICE_URL=http://resource-service:8084
  depends_on:
    mongodb:
      condition: service_healthy
    rabbitmq:
      condition: service_healthy
  healthcheck:
    test: ["CMD", "curl", "-f", "http://localhost:8085/actuator/health"]
    interval: 30s
    timeout: 10s
    retries: 3
    start_period: 40s
  restart: on-failure
  networks:
    - mkfmm-network
```

### Deploy Service
```yaml
deploy-service:
  build:
    context: ./backend/deploy-service
    dockerfile: Dockerfile
  container_name: mkfmm-deploy-service
  ports:
    - "8086:8086"
  environment:
    - SPRING_PROFILES_ACTIVE=docker
    - MONGODB_URI=mongodb://mongodb:27017/mkfmm-deploy
    - RABBITMQ_HOST=rabbitmq
    - RABBITMQ_PORT=5672
    - RABBITMQ_USER=guest
    - RABBITMQ_PASS=guest
    - DATAIO_SERVICE_URL=http://dataio-service:8085
    - DEPLOY_FILE_PATH=/data/deployments
  volumes:
    - deploy-data:/data/deployments
  depends_on:
    mongodb:
      condition: service_healthy
    rabbitmq:
      condition: service_healthy
    dataio-service:
      condition: service_healthy
  healthcheck:
    test: ["CMD", "curl", "-f", "http://localhost:8086/actuator/health"]
    interval: 30s
    timeout: 10s
    retries: 3
    start_period: 40s
  restart: on-failure
  networks:
    - mkfmm-network
```

### Frontend
```yaml
frontend:
  build:
    context: ./frontend
    dockerfile: Dockerfile
    args:
      - VITE_API_BASE_URL=http://localhost:8080
  container_name: mkfmm-frontend
  ports:
    - "3000:3000"
  depends_on:
    api-gateway:
      condition: service_healthy
  healthcheck:
    test: ["CMD", "curl", "-f", "http://localhost:3000/"]
    interval: 30s
    timeout: 5s
    retries: 3
    start_period: 10s
  restart: on-failure
  networks:
    - mkfmm-network
```

### Additional Volumes (추가)
```yaml
volumes:
  mongodb-data:      # Dev 1에서 정의됨
  rabbitmq-data:     # Dev 1에서 정의됨
  jwt-keys:          # Dev 1에서 정의됨
  deploy-data:       # NEW: 배포 파일 영구 저장
```

---

## 2. Network Communication (Dev 3 추가분)

### Internal Communication
| From | To | Protocol | Purpose |
|---|---|---|---|
| API Gateway | dataio-service:8085 | HTTP | DataIO API 라우팅 |
| API Gateway | deploy-service:8086 | HTTP | Deploy API 라우팅 |
| dataio-service | resource-service:8084 | HTTP (internal) | 중복 검사, 리소스 CRUD |
| deploy-service | dataio-service:8085 | HTTP (internal) | exportAll 호출 |
| dataio-service | rabbitmq:5672 | AMQP | 이벤트 발행 |
| deploy-service | rabbitmq:5672 | AMQP | 이벤트 발행 |
| Frontend (browser) | api-gateway:8080 | HTTP | 모든 API 요청 |

### Service Dependency Graph (Dev 3)
```
[Frontend :3000]
     |
     v (HTTP REST)
[API Gateway :8080]
     |
     +---> [dataio-service :8085] ---> [resource-service :8084]
     |            |
     |            v (AMQP)
     |      [rabbitmq :5672]
     |
     +---> [deploy-service :8086] ---> [dataio-service :8085]
                  |
                  v (AMQP)
            [rabbitmq :5672]
```

---

## 3. Dockerfile - DataIO Service

```dockerfile
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY build/libs/dataio-service-*.jar app.jar
RUN apk add --no-cache curl
EXPOSE 8085
HEALTHCHECK --interval=30s --timeout=10s --retries=3 --start-period=40s \
  CMD curl -f http://localhost:8085/actuator/health || exit 1
ENTRYPOINT ["java", "-jar", "app.jar"]
```

## 4. Dockerfile - Deploy Service

```dockerfile
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
RUN mkdir -p /data/deployments
COPY build/libs/deploy-service-*.jar app.jar
RUN apk add --no-cache curl
EXPOSE 8086
HEALTHCHECK --interval=30s --timeout=10s --retries=3 --start-period=40s \
  CMD curl -f http://localhost:8086/actuator/health || exit 1
ENTRYPOINT ["java", "-jar", "app.jar"]
```

## 5. Dockerfile - Frontend (Multi-stage Build)

```dockerfile
# Stage 1: Build
FROM node:20-alpine AS build
WORKDIR /app
COPY package.json package-lock.json ./
RUN npm ci
COPY . .
ARG VITE_API_BASE_URL=http://localhost:8080
ENV VITE_API_BASE_URL=$VITE_API_BASE_URL
RUN npm run build

# Stage 2: Serve
FROM nginx:alpine
COPY --from=build /app/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf
RUN apk add --no-cache curl
EXPOSE 3000
HEALTHCHECK --interval=30s --timeout=5s --retries=3 --start_period=10s \
  CMD curl -f http://localhost:3000/ || exit 1
CMD ["nginx", "-g", "daemon off;"]
```

---

## 6. Nginx Configuration (Frontend)

```nginx
server {
    listen 3000;
    server_name localhost;
    root /usr/share/nginx/html;
    index index.html;

    # SPA fallback - all routes → index.html
    location / {
        try_files $uri $uri/ /index.html;
    }

    # Static asset caching
    location ~* \.(js|css|png|jpg|jpeg|gif|ico|svg|woff2?)$ {
        expires 1y;
        add_header Cache-Control "public, immutable";
    }

    # Gzip compression
    gzip on;
    gzip_vary on;
    gzip_min_length 1024;
    gzip_types text/plain text/css application/json application/javascript text/xml application/xml text/javascript;
}
```

---

## 7. MongoDB Collections (Dev 3 추가분)

| Service | Database Name | Collections |
|---|---|---|
| dataio-service | mkfmm-dataio | import_jobs |
| deploy-service | mkfmm-deploy | deployments |

### DataIO Service Collections
| Collection | Purpose | Indexes |
|---|---|---|
| import_jobs | Import 작업 상태/결과 | userId + createdAt (compound), status |

### Deploy Service Collections
| Collection | Purpose | Indexes |
|---|---|---|
| deployments | 배포 메타데이터 | createdAt (descending), version (unique) |

---

## 8. RabbitMQ Infrastructure (Dev 3 추가분)

### Additional Queues & Bindings
```
기존 Exchange: mkfmm.events (topic, durable) — Dev 1에서 생성됨

New Queues:
  history.dataio-events (durable)
    - x-dead-letter-exchange: mkfmm.events.dlx
    - x-dead-letter-routing-key: dlq

  history.deploy-events (durable)
    - x-dead-letter-exchange: mkfmm.events.dlx
    - x-dead-letter-routing-key: dlq

New Bindings:
  mkfmm.events → history.dataio-events
    Routing keys: dataio.#

  mkfmm.events → history.deploy-events
    Routing keys: deploy.#
```

### Routing Key Convention
```
{service}.{entity}.{action}

Examples:
  dataio.import.completed
  dataio.export.completed
  deploy.completed
```

---

## 9. Spring Configuration (DataIO + Deploy)

### DataIO Service - application.yml
```yaml
server:
  port: 8085

spring:
  application:
    name: dataio-service
  data:
    mongodb:
      uri: ${MONGODB_URI:mongodb://localhost:27017/mkfmm-dataio}
  servlet:
    multipart:
      max-file-size: 5MB
      max-request-size: 6MB
  rabbitmq:
    host: ${RABBITMQ_HOST:localhost}
    port: ${RABBITMQ_PORT:5672}
    username: ${RABBITMQ_USER:guest}
    password: ${RABBITMQ_PASS:guest}
    publisher-confirm-type: correlated
    publisher-returns: true

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
    diskspace:
      enabled: true

mkfmm:
  resource-service:
    url: ${RESOURCE_SERVICE_URL:http://localhost:8084}
    timeout: 30s
```

### Deploy Service - application.yml
```yaml
server:
  port: 8086

spring:
  application:
    name: deploy-service
  data:
    mongodb:
      uri: ${MONGODB_URI:mongodb://localhost:27017/mkfmm-deploy}
  rabbitmq:
    host: ${RABBITMQ_HOST:localhost}
    port: ${RABBITMQ_PORT:5672}
    username: ${RABBITMQ_USER:guest}
    password: ${RABBITMQ_PASS:guest}
    publisher-confirm-type: correlated
    publisher-returns: true

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
    diskspace:
      enabled: true
      path: ${DEPLOY_FILE_PATH:/data/deployments}
      threshold: 104857600

mkfmm:
  dataio-service:
    url: ${DATAIO_SERVICE_URL:http://localhost:8085}
    timeout: 60s
  deploy:
    file-path: ${DEPLOY_FILE_PATH:/data/deployments}
```

---

## 10. Build Configuration (Gradle - Dev 3 추가)

### Root settings.gradle.kts (추가)
```kotlin
// Dev 1 기존:
// include("shared", "auth-service", "api-gateway")

// Dev 3 추가:
include(
    "dataio-service",
    "deploy-service"
)

project(":dataio-service").projectDir = file("backend/dataio-service")
project(":deploy-service").projectDir = file("backend/deploy-service")
```

### DataIO Service - build.gradle.kts
```kotlin
plugins {
    java
    id("org.springframework.boot")
    id("io.spring.dependency-management")
}

dependencies {
    implementation(project(":shared"))
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-data-mongodb")
    implementation("org.springframework.boot:spring-boot-starter-amqp")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-webflux") // WebClient
    implementation("org.apache.poi:poi-ooxml:5.2.5") // Excel
    implementation("com.fasterxml.jackson.core:jackson-databind")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("net.jqwik:jqwik:1.8.4")
    testImplementation("org.testcontainers:mongodb")
    testImplementation("org.testcontainers:rabbitmq")
}
```

### Deploy Service - build.gradle.kts
```kotlin
plugins {
    java
    id("org.springframework.boot")
    id("io.spring.dependency-management")
}

dependencies {
    implementation(project(":shared"))
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-data-mongodb")
    implementation("org.springframework.boot:spring-boot-starter-amqp")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-webflux") // WebClient

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("net.jqwik:jqwik:1.8.4")
    testImplementation("org.testcontainers:mongodb")
}
```

### Frontend - package.json (주요 의존성)
```json
{
  "name": "mkfmm-frontend",
  "private": true,
  "version": "0.1.0",
  "type": "module",
  "scripts": {
    "dev": "vite",
    "build": "tsc && vite build",
    "preview": "vite preview",
    "test": "vitest",
    "lint": "eslint . --ext ts,tsx"
  },
  "dependencies": {
    "react": "^18.3.1",
    "react-dom": "^18.3.1",
    "react-router-dom": "^6.23.0",
    "axios": "^1.7.2",
    "react-hook-form": "^7.51.0",
    "@headlessui/react": "^2.1.0",
    "@heroicons/react": "^2.1.0"
  },
  "devDependencies": {
    "typescript": "^5.4.0",
    "vite": "^5.3.0",
    "@vitejs/plugin-react": "^4.3.0",
    "tailwindcss": "^3.4.0",
    "postcss": "^8.4.0",
    "autoprefixer": "^10.4.0",
    "vitest": "^1.6.0",
    "@testing-library/react": "^15.0.0",
    "fast-check": "^3.19.0",
    "eslint": "^8.57.0",
    "@typescript-eslint/eslint-plugin": "^7.0.0"
  }
}
```

---

## 11. Development Workflow (Dev 3)

### Local Development
```bash
# 1. Infra만 Docker로 실행 (MongoDB, RabbitMQ — Dev 1에서 이미 설정됨)
docker compose up mongodb rabbitmq -d

# 2. Resource Service도 실행 필요 (DataIO가 의존)
cd backend/resource-service && ./gradlew bootRun &

# 3. DataIO Service 실행
cd backend/dataio-service && ./gradlew bootRun &

# 4. Deploy Service 실행
cd backend/deploy-service && ./gradlew bootRun &

# 5. Frontend 개발 서버 실행
cd frontend && npm install && npm run dev
# → http://localhost:5173 (Vite dev server, API는 localhost:8080 직접 호출)
```

### Full Docker Deployment
```bash
# 전체 서비스 빌드 & 실행
docker compose up --build -d

# Dev 3 서비스만 로그 확인
docker compose logs -f dataio-service deploy-service frontend
```

### Frontend 환경 변수
```bash
# 개발: .env.development
VITE_API_BASE_URL=http://localhost:8080

# Docker 빌드 시: build arg로 전달
# docker compose에서 args: VITE_API_BASE_URL=http://localhost:8080
```

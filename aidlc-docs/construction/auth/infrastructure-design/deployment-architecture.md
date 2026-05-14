# Deployment Architecture - Unit 1: Auth

## Docker Compose Configuration

```yaml
# docker-compose.yml
version: '3.8'

services:
  frontend:
    build:
      context: ./frontend
      dockerfile: Dockerfile
    ports:
      - "80:80"
    depends_on:
      - backend
    networks:
      - app-network
    restart: unless-stopped
    mem_limit: 128m

  backend:
    build:
      context: ./backend
      dockerfile: Dockerfile
    expose:
      - "8080"
    environment:
      - JWT_SECRET=${JWT_SECRET}
      - ADMIN_EMAIL=${ADMIN_EMAIL}
      - ADMIN_PASSWORD=${ADMIN_PASSWORD}
      - CORS_ORIGINS=${CORS_ORIGINS}
      - SPRING_PROFILES_ACTIVE=production
      - DB_PATH=/data/resource-manager.db
    volumes:
      - db-data:/data
      - app-logs:/var/log/resource-manager
    networks:
      - app-network
    restart: unless-stopped
    mem_limit: 512m

volumes:
  db-data:
    driver: local
  app-logs:
    driver: local

networks:
  app-network:
    driver: bridge
```

## Backend Dockerfile

```dockerfile
# backend/Dockerfile
FROM eclipse-temurin:21-jre-alpine AS runtime

RUN addgroup -S appgroup && adduser -S appuser -G appgroup

WORKDIR /app

COPY build/libs/resource-manager-*.jar app.jar

RUN mkdir -p /data /var/log/resource-manager && \
    chown -R appuser:appgroup /data /var/log/resource-manager

USER appuser

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
```

## Frontend Dockerfile

```dockerfile
# frontend/Dockerfile

# Build stage
FROM node:20-alpine AS build

WORKDIR /app
COPY package.json package-lock.json ./
RUN npm ci
COPY . .
RUN npm run build

# Production stage
FROM nginx:1.25-alpine

COPY --from=build /app/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]
```

## Nginx Configuration

```nginx
# nginx.conf
server {
    listen 80;
    server_name _;

    root /usr/share/nginx/html;
    index index.html;

    # Security headers (프론트엔드 정적 파일용)
    add_header X-Content-Type-Options "nosniff" always;
    add_header X-Frame-Options "DENY" always;
    add_header Referrer-Policy "strict-origin-when-cross-origin" always;

    # API reverse proxy
    location /api/ {
        proxy_pass http://backend:8080/api/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;

        # Request size limit (import 파일 업로드용)
        client_max_body_size 10m;
    }

    # SPA fallback (React Router)
    location / {
        try_files $uri $uri/ /index.html;
    }

    # 정적 파일 캐싱
    location ~* \.(js|css|png|jpg|jpeg|gif|ico|svg|woff|woff2)$ {
        expires 1y;
        add_header Cache-Control "public, immutable";
    }

    # 디렉토리 리스팅 비활성화
    autoindex off;
}
```

## Deployment Process

### Initial Setup (EC2)
```bash
# 1. Docker & Docker Compose 설치
sudo yum update -y
sudo yum install -y docker
sudo systemctl start docker
sudo systemctl enable docker
sudo usermod -aG docker ec2-user

# Docker Compose plugin
sudo mkdir -p /usr/local/lib/docker/cli-plugins
sudo curl -SL https://github.com/docker/compose/releases/latest/download/docker-compose-linux-x86_64 -o /usr/local/lib/docker/cli-plugins/docker-compose
sudo chmod +x /usr/local/lib/docker/cli-plugins/docker-compose

# 2. 프로젝트 배포
git clone <repository-url> /opt/resource-manager
cd /opt/resource-manager

# 3. 환경 변수 설정
cp .env.example .env
# .env 파일 편집 (JWT_SECRET, ADMIN_EMAIL 등)

# 4. 빌드 & 실행
docker compose up -d --build
```

### Update Deployment
```bash
cd /opt/resource-manager
git pull
docker compose up -d --build
```

### Rollback
```bash
docker compose down
git checkout <previous-tag>
docker compose up -d --build
```

## .env.example

```env
# JWT Configuration
JWT_SECRET=change-this-to-a-secure-random-string-at-least-32-characters

# Initial Admin
ADMIN_EMAIL=admin@company.com
ADMIN_PASSWORD=ChangeThisPassword123!

# CORS
CORS_ORIGINS=http://your-domain.com

# Spring Profile
SPRING_PROFILES_ACTIVE=production
```

## Logging Configuration

### Docker Compose Logging
```yaml
# 각 서비스에 추가
logging:
  driver: json-file
  options:
    max-size: "50m"
    max-file: "5"
```

### Backend (logback-spring.xml)
- Profile `production`: JSON format, file appender (/var/log/resource-manager/app.log)
- Profile `default`: Console, readable format (개발용)
- Log rotation: 일별, 90일 보존

## Health Check

```yaml
# docker-compose.yml backend 서비스에 추가
healthcheck:
  test: ["CMD", "wget", "--spider", "-q", "http://localhost:8080/actuator/health"]
  interval: 30s
  timeout: 10s
  retries: 3
  start_period: 40s
```

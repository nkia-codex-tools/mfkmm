# Build Instructions

## Prerequisites

- **Java**: JDK 21 (Eclipse Temurin 권장)
- **Gradle**: 8.x (wrapper 포함)
- **Node.js**: 20.x LTS
- **npm**: 10.x
- **Docker**: 24.x+
- **Docker Compose**: v2.x

## Environment Variables

```bash
export JWT_SECRET="your-secret-key-at-least-32-characters-long"
export ADMIN_EMAIL="admin@company.com"
export ADMIN_PASSWORD="SecurePassword123!"
export CORS_ORIGINS="http://localhost:5173"
```

## Build Steps

### 1. Backend Build

```bash
cd backend

# Gradle wrapper 사용 (Unix)
chmod +x gradlew
./gradlew clean build

# 또는 Windows
# gradlew.bat clean build
```

**Expected Output**:
- `BUILD SUCCESSFUL`
- JAR: `backend/build/libs/resource-manager-0.0.1-SNAPSHOT.jar`

### 2. Frontend Build

```bash
cd frontend

# 의존성 설치
npm ci

# TypeScript 타입 체크 + 빌드
npm run build
```

**Expected Output**:
- `dist/` 디렉토리에 빌드 결과물
- `dist/index.html`, `dist/assets/` (JS, CSS)

### 3. Docker Build (Production)

```bash
# 프로젝트 루트에서
cd backend && ./gradlew clean build && cd ..
docker compose build
```

### 4. Run (Development)

```bash
# Backend (터미널 1)
cd backend
./gradlew bootRun

# Frontend (터미널 2)
cd frontend
npm run dev
```

- Backend: http://localhost:8080
- Frontend: http://localhost:5173 (Vite dev proxy → backend)

### 5. Run (Production - Docker Compose)

```bash
# .env 파일 설정
cp .env.example .env
# .env 편집하여 실제 값 입력

# 빌드 및 실행
docker compose up -d --build

# 상태 확인
docker compose ps
docker compose logs -f backend
```

- Application: http://localhost:80

## Verify Build Success

| Check | Command | Expected |
|-------|---------|----------|
| Backend JAR | `ls backend/build/libs/*.jar` | resource-manager-0.0.1-SNAPSHOT.jar |
| Frontend dist | `ls frontend/dist/index.html` | 파일 존재 |
| Docker images | `docker compose images` | frontend, backend 이미지 |
| Health check | `curl http://localhost:8080/actuator/health` | `{"status":"UP"}` |
| Initial admin | Backend 로그 확인 | "Initial admin account created" |

## Troubleshooting

### Gradle build fails with "Could not resolve dependencies"
- Maven Central 접근 확인 (프록시/방화벽)
- `./gradlew --refresh-dependencies clean build`

### npm ci fails
- `rm -rf node_modules package-lock.json && npm install`
- Node.js 버전 확인: `node --version` (20.x 필요)

### Docker compose fails - port 80 already in use
- `sudo lsof -i :80` 로 점유 프로세스 확인
- 또는 docker-compose.yml에서 포트 변경: `"8080:80"`

### SQLite "database is locked"
- WAL 모드 확인: application.yml의 connection-init-sql
- 동시에 하나의 backend 인스턴스만 실행

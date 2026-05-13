# Build Instructions - MKFMM

## Prerequisites

| Tool | Version | Purpose |
|---|---|---|
| Java JDK | 17+ | Backend 빌드 |
| Gradle | 8.x (Wrapper 포함) | 빌드 도구 |
| Node.js | 18+ | Frontend 빌드 |
| npm | 9+ | 패키지 매니저 |
| Docker | 24+ | 컨테이너 빌드/실행 |
| Docker Compose | v2+ | 서비스 오케스트레이션 |
| OpenSSL | any | JWT 키 생성 |

## Environment Variables

| Variable | Default | Purpose |
|---|---|---|
| MKFMM_ROOT_USER | rootadmin | Root Admin 사용자 ID |
| MKFMM_ROOT_PASSWORD | changeme123 | Root Admin 초기 비밀번호 |

---

## Build Steps

### 1. JWT 키 생성 (최초 1회)

```bash
chmod +x generate-keys.sh
./generate-keys.sh
```

결과: `keys/private.pem`, `keys/public.pem` 생성됨

### 2. Backend 빌드 (전체 서비스)

```bash
cd backend
./gradlew clean build -x test
```

빌드 결과:
- `backend/auth-service/build/libs/auth-service-*.jar`
- `backend/api-gateway/build/libs/api-gateway-*.jar`
- `backend/user-service/build/libs/user-service-*.jar`
- `backend/resource-service/build/libs/resource-service-*.jar`
- `backend/history-service/build/libs/history-service-*.jar`
- `backend/dataio-service/build/libs/dataio-service-*.jar`
- `backend/deploy-service/build/libs/deploy-service-*.jar`

### 3. Frontend 빌드

```bash
cd frontend
npm install
npm run build
```

빌드 결과: `frontend/dist/` 디렉토리

### 4. Docker 이미지 빌드 + 전체 실행

```bash
cd ..  # 프로젝트 루트
docker-compose up --build
```

### 5. 빌드 성공 확인

```bash
# 모든 서비스 healthy 확인
docker-compose ps

# 예상 출력: 모든 서비스 Up (healthy)
# mkfmm-mongodb         Up (healthy)
# mkfmm-rabbitmq        Up (healthy)
# mkfmm-auth-service    Up (healthy)
# mkfmm-api-gateway     Up (healthy)
# mkfmm-user-service    Up (healthy)
# mkfmm-resource-service Up (healthy)
# mkfmm-history-service Up (healthy)
# mkfmm-dataio-service  Up (healthy)
# mkfmm-deploy-service  Up (healthy)
# mkfmm-frontend        Up
```

---

## Troubleshooting

### Gradle 빌드 실패 (Shared Library 못 찾음)
```bash
# settings.gradle.kts에 모든 서비스 포함 확인
cat backend/settings.gradle.kts
# shared가 먼저 빌드되어야 함
cd backend && ./gradlew :shared:build
```

### Docker Compose 시작 순서 문제
- MongoDB/RabbitMQ healthcheck가 먼저 통과해야 서비스 시작
- `depends_on: condition: service_healthy` 설정 확인

### Port 충돌
```bash
# 사용 중인 포트 확인
lsof -i :8080 -i :8081 -i :8082 -i :8083 -i :8084 -i :8085 -i :8086 -i :27017 -i :5672
```

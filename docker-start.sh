#!/bin/bash
set -e

PROJECT_ROOT="/home/ec2-user/environment/noahkim/mfkmm"
cd "$PROJECT_ROOT"

echo "============================================"
echo " 리소스 관리 시스템 - Docker 실행"
echo "============================================"

# 1. Docker Compose 플러그인 설치
if ! sudo docker compose version &>/dev/null; then
    echo "[1/4] Docker Compose 플러그인 설치 중..."
    sudo mkdir -p /usr/local/lib/docker/cli-plugins
    sudo curl -SL "https://github.com/docker/compose/releases/download/v2.29.7/docker-compose-linux-x86_64" \
        -o /usr/local/lib/docker/cli-plugins/docker-compose
    sudo chmod +x /usr/local/lib/docker/cli-plugins/docker-compose
    echo "  Docker Compose $(sudo docker compose version --short) 설치 완료"
else
    echo "[1/4] Docker Compose 이미 설치됨"
fi

# 2. .env 파일 생성
if [ ! -f "$PROJECT_ROOT/.env" ]; then
    echo "[2/4] .env 파일 생성 중..."
    cat > "$PROJECT_ROOT/.env" << 'EOF'
JWT_SECRET=my-super-secret-jwt-key-for-resource-manager-2026
ADMIN_EMAIL=admin@company.com
ADMIN_PASSWORD=Admin1234!
CORS_ORIGINS=http://98.81.253.40
EOF
    echo "  .env 생성 완료"
else
    echo "[2/4] .env 파일 이미 존재"
fi

# 3. Backend JAR 빌드 (multi-stage Docker build로 처리)
echo "[3/4] Docker 이미지 빌드 중... (첫 실행 시 5-10분 소요)"

# Backend Dockerfile을 multi-stage로 업데이트 (빌드 포함)
cat > "$PROJECT_ROOT/backend/Dockerfile" << 'DOCKERFILE'
FROM eclipse-temurin:21-jdk-alpine AS build

WORKDIR /app
COPY build.gradle.kts settings.gradle.kts ./
COPY gradle/ gradle/
COPY gradlew ./
RUN chmod +x gradlew

# Gradle wrapper 다운로드 + 의존성 캐시
RUN ./gradlew --no-daemon dependencies 2>/dev/null || true

COPY src/ src/
RUN ./gradlew clean build -x test --no-daemon

FROM eclipse-temurin:21-jre-alpine AS runtime

RUN addgroup -S appgroup && adduser -S appuser -G appgroup
WORKDIR /app

COPY --from=build /app/build/libs/resource-manager-*.jar app.jar

RUN mkdir -p /data && chown -R appuser:appgroup /data
USER appuser

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
DOCKERFILE

# Gradle wrapper JAR 다운로드 (빌드에 필요)
if [ ! -f "$PROJECT_ROOT/backend/gradle/wrapper/gradle-wrapper.jar" ]; then
    curl -sL "https://services.gradle.org/distributions/gradle-8.10.2-bin.zip" -o /tmp/gradle-dist.zip
    unzip -q -o /tmp/gradle-dist.zip -d /tmp/ 2>/dev/null
    cp /tmp/gradle-8.10.2/lib/gradle-launcher-8.10.2.jar "$PROJECT_ROOT/backend/gradle/wrapper/gradle-wrapper.jar" 2>/dev/null || \
    curl -sL "https://github.com/gradle/gradle/raw/v8.10.2/gradle/wrapper/gradle-wrapper.jar" -o "$PROJECT_ROOT/backend/gradle/wrapper/gradle-wrapper.jar" 2>/dev/null || true
fi

# 4. Docker Compose 빌드 및 실행
echo "[4/4] 컨테이너 빌드 및 실행..."
sudo docker compose up -d --build

echo ""
echo "============================================"
echo " 🚀 실행 완료!"
echo "============================================"
echo ""
echo " 접속 URL: http://98.81.253.40"
echo ""
echo " 관리자 로그인:"
echo "   Email: admin@company.com"
echo "   Password: Admin1234!"
echo ""
echo " 유용한 명령어:"
echo "   로그 확인:  sudo docker compose logs -f"
echo "   상태 확인:  sudo docker compose ps"
echo "   중지:       sudo docker compose down"
echo "   재시작:     sudo docker compose restart"
echo ""
echo "============================================"

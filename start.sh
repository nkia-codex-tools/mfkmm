#!/bin/bash
set -e

PROJECT_ROOT="/home/ec2-user/environment/noahkim/mfkmm"
cd "$PROJECT_ROOT"

echo "============================================"
echo " 리소스 관리 시스템 - 시작 스크립트"
echo "============================================"

# 1. Java 21 설치 확인
if ! java --version 2>/dev/null | grep -q "21"; then
    echo "[1/5] Java 21 설치 중..."
    sudo yum install -y java-21-amazon-corretto-devel
else
    echo "[1/5] Java 21 이미 설치됨"
fi

# 2. .env 파일 확인
if [ ! -f "$PROJECT_ROOT/.env" ]; then
    echo "[2/5] .env 파일 생성 중..."
    cp "$PROJECT_ROOT/.env.example" "$PROJECT_ROOT/.env"
    # 기본값 설정
    sed -i 's|change-this-to-a-secure-random-string-at-least-32-characters|my-super-secret-jwt-key-for-resource-manager-2026|' "$PROJECT_ROOT/.env"
    sed -i 's|CORS_ORIGINS=http://localhost|CORS_ORIGINS=http://98.81.253.40|' "$PROJECT_ROOT/.env"
    echo "  .env 파일이 생성되었습니다. 필요시 수정하세요."
else
    echo "[2/5] .env 파일 이미 존재"
fi

# 환경변수 로드
export $(grep -v '^#' "$PROJECT_ROOT/.env" | xargs)

# 3. Gradle Wrapper JAR 다운로드
if [ ! -f "$PROJECT_ROOT/backend/gradle/wrapper/gradle-wrapper.jar" ]; then
    echo "[3/5] Gradle Wrapper 다운로드 중..."
    curl -sL https://services.gradle.org/distributions/gradle-8.10.2-bin.zip -o /tmp/gradle.zip
    unzip -q -o /tmp/gradle.zip -d /tmp/
    cp /tmp/gradle-8.10.2/lib/gradle-wrapper-*.jar "$PROJECT_ROOT/backend/gradle/wrapper/gradle-wrapper.jar" 2>/dev/null || \
    curl -sL "https://raw.githubusercontent.com/gradle/gradle/v8.10.2/gradle/wrapper/gradle-wrapper.jar" -o "$PROJECT_ROOT/backend/gradle/wrapper/gradle-wrapper.jar" 2>/dev/null || \
    echo "  Gradle wrapper JAR 다운로드 실패 - 수동 설치 필요"
fi
chmod +x "$PROJECT_ROOT/backend/gradlew"

# 4. Backend 빌드 및 실행
echo "[4/5] Backend 빌드 중..."
cd "$PROJECT_ROOT/backend"

# Gradle이 시스템에 없으면 직접 다운로드
if [ ! -f "gradle/wrapper/gradle-wrapper.jar" ]; then
    echo "  Gradle 직접 다운로드..."
    curl -sL https://services.gradle.org/distributions/gradle-8.10.2-bin.zip -o /tmp/gradle-dist.zip
    unzip -q -o /tmp/gradle-dist.zip -d /opt/
    export PATH="/opt/gradle-8.10.2/bin:$PATH"
    gradle wrapper
fi

./gradlew clean build -x test --no-daemon 2>/dev/null || \
    /opt/gradle-8.10.2/bin/gradle clean build -x test --no-daemon

echo "  Backend JAR 생성 완료"

# Backend 실행 (백그라운드)
echo "  Backend 시작 중 (port 8080)..."
export SPRING_PROFILES_ACTIVE=default
export DB_PATH="$PROJECT_ROOT/backend/resource-manager.db"
java -jar build/libs/resource-manager-0.0.1-SNAPSHOT.jar &
BACKEND_PID=$!
echo "  Backend PID: $BACKEND_PID"

# Health check 대기
echo "  Backend 시작 대기 중..."
for i in $(seq 1 30); do
    if curl -s http://localhost:8080/actuator/health 2>/dev/null | grep -q "UP"; then
        echo "  Backend 시작 완료!"
        break
    fi
    sleep 2
done

# 5. Frontend 빌드 및 실행
echo "[5/5] Frontend 설정 중..."
cd "$PROJECT_ROOT/frontend"
npm install 2>/dev/null

echo "  Frontend dev server 시작 중 (port 5173)..."
# Vite dev server를 0.0.0.0으로 바인딩 (외부 접근 허용)
npx vite --host 0.0.0.0 --port 5173 &
FRONTEND_PID=$!

echo ""
echo "============================================"
echo " 🚀 실행 완료!"
echo "============================================"
echo ""
echo " Backend:  http://localhost:8080"
echo " Frontend: http://98.81.253.40:5173"
echo ""
echo " 관리자 계정:"
echo "   Email: $ADMIN_EMAIL"
echo "   Password: $ADMIN_PASSWORD"
echo ""
echo " 프로세스 종료:"
echo "   kill $BACKEND_PID $FRONTEND_PID"
echo ""
echo "============================================"

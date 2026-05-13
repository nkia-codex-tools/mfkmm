# Integration Test Instructions - MKFMM

## Purpose

서비스 간 상호작용을 검증합니다:
- API Gateway → Backend Services (JWT 라우팅)
- Service → MongoDB (데이터 영속화)
- Service → RabbitMQ (이벤트 발행/수신)
- History Service ← All Services (이벤트 수신 → 이력 기록)

---

## Setup Integration Test Environment

```bash
# 전체 시스템 실행
docker-compose up --build -d

# 모든 서비스 healthy 대기
docker-compose ps  # 모두 healthy 확인
```

---

## Integration Test Scenarios

### Scenario 1: 인증 흐름 (Auth → Gateway → Client)

```bash
# 1. 로그인
TOKEN=$(curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"userId":"rootadmin","password":"changeme123"}' | jq -r '.accessToken')

echo "Access Token: $TOKEN"

# 2. JWT로 보호된 API 접근
curl -s http://localhost:8080/api/users/me \
  -H "Authorization: Bearer $TOKEN" | jq .

# 3. 잘못된 토큰으로 접근 (401 예상)
curl -s -o /dev/null -w "%{http_code}" http://localhost:8080/api/users/me \
  -H "Authorization: Bearer invalid_token"
# Expected: 401
```

### Scenario 2: 사용자 관리 (User Service)

```bash
# 1. 사용자 등록
curl -s -X POST http://localhost:8080/api/users \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"userId":"dev01","name":"Developer 1","email":"dev01@company.com","department":"Dev","role":"WRITE","password":"temp1234"}' | jq .

# 2. 사용자 목록 조회
curl -s http://localhost:8080/api/users \
  -H "Authorization: Bearer $TOKEN" | jq .

# 3. 권한 변경
curl -s -X PUT http://localhost:8080/api/users/dev01/permission \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"role":"ADMIN"}' | jq .
```

### Scenario 3: 리소스 관리 + 유사도 탐지 (Resource Service)

```bash
# 1. 리소스 등록
curl -s -X POST http://localhost:8080/api/resources \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"resourceKey":"msg.hello.world","resourceType":"MESSAGE_KEY","content":"Hello World","description":"인사말"}' | jq .

# 2. 유사 리소스 등록 시도 (유사도 탐지 확인)
curl -s -X POST http://localhost:8080/api/resources \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"resourceKey":"msg.hello.worl","resourceType":"MESSAGE_KEY","content":"Hello Worl","description":"오타"}' | jq .
# Expected: SimilarityResult list 반환 (편집 거리 1)

# 3. 검색
curl -s "http://localhost:8080/api/resources/search?keyword=hello" \
  -H "Authorization: Bearer $TOKEN" | jq .
```

### Scenario 4: 이벤트 기반 이력 기록 (History Service)

```bash
# 1. 리소스 등록 후 잠시 대기 (이벤트 비동기 처리)
sleep 2

# 2. 작업 이력 조회
curl -s "http://localhost:8080/api/history/work-logs" \
  -H "Authorization: Bearer $TOKEN" | jq .
# Expected: ResourceCreated, UserCreated 등 이력 존재

# 3. 본인 이력 조회
curl -s "http://localhost:8080/api/history/work-logs/my" \
  -H "Authorization: Bearer $TOKEN" | jq .
```

### Scenario 5: IMPORT/EXPORT (DataIO Service)

```bash
# 1. JSON IMPORT
curl -s -X POST http://localhost:8080/api/dataio/import \
  -H "Authorization: Bearer $TOKEN" \
  -F "file=@test-data.json" \
  -F "format=JSON" \
  -F "conflictPolicy=SKIP" | jq .

# 2. EXPORT
curl -s -X POST "http://localhost:8080/api/dataio/export/all?format=JSON" \
  -H "Authorization: Bearer $TOKEN" --output export.json
```

### Scenario 6: 배포 (Deploy Service)

```bash
# 1. 배포 실행
curl -s -X POST "http://localhost:8080/api/deploy?format=JSON" \
  -H "Authorization: Bearer $TOKEN" | jq .

# 2. 배포 이력 조회
curl -s http://localhost:8080/api/deploy/history \
  -H "Authorization: Bearer $TOKEN" | jq .
```

---

## Cleanup

```bash
docker-compose down -v  # 볼륨까지 삭제 (DB 초기화)
```

---

## Pass Criteria

- [ ] 로그인 성공 + JWT 발급
- [ ] JWT로 보호된 API 정상 접근
- [ ] 잘못된 토큰 시 401 반환
- [ ] 사용자 CRUD 정상 동작
- [ ] 리소스 등록 시 중복/유사도 탐지 동작
- [ ] 검색 결과 정상 반환
- [ ] 이벤트 발행 후 History Service에 이력 기록
- [ ] IMPORT/EXPORT 정상 동작
- [ ] 배포 실행 + 이력 기록
- [ ] RabbitMQ Management UI에서 메시지 흐름 확인 (localhost:15672)

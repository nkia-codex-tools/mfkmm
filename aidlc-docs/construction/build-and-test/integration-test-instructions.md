# Integration Test Instructions

## Purpose
유닛 간 상호작용을 검증합니다 (Auth → Resource, Resource → History, Resource → Version).

## Setup Integration Test Environment

### 1. Start Backend
```bash
cd backend
export JWT_SECRET="test-secret-key-at-least-32-characters-long"
export ADMIN_EMAIL="admin@test.com"
export ADMIN_PASSWORD="TestPass123!"
export CORS_ORIGINS="http://localhost:5173"
./gradlew bootRun
```

### 2. Wait for Startup
```bash
# Health check 확인 (최대 30초 대기)
until curl -s http://localhost:8080/actuator/health | grep -q "UP"; do sleep 2; done
```

## Integration Test Scenarios

### Scenario 1: Auth → Resource Access Control
**Description**: 인증된 사용자만 리소스에 접근 가능한지 확인

```bash
# 1. 미인증 접근 → 401
curl -s -o /dev/null -w "%{http_code}" http://localhost:8080/api/resources/functions
# Expected: 401

# 2. 로그인
TOKEN=$(curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@test.com","password":"TestPass123!"}' | jq -r '.accessToken')

# 3. 인증 후 접근 → 200
curl -s -o /dev/null -w "%{http_code}" -H "Authorization: Bearer $TOKEN" \
  http://localhost:8080/api/resources/functions
# Expected: 200
```

### Scenario 2: Resource CRUD → History Recording
**Description**: 리소스 생성/수정/삭제 시 변경 이력이 자동 기록되는지 확인

```bash
# 1. 리소스 생성
FUNC_ID=$(curl -s -X POST http://localhost:8080/api/resources/functions \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"functionId":"TEST.FUNC.001","functionName":"Test Function","action":"cmm.view","type":"읽기","light":false,"standard":true,"enterprise":true,"systemMenu":false}' | jq -r '.id')

# 2. 이력 확인
curl -s -H "Authorization: Bearer $TOKEN" \
  "http://localhost:8080/api/history?resourceType=functions" | jq '.content | length'
# Expected: > 0 (Unit 4 통합 후)

# 3. 리소스 수정
curl -s -X PUT http://localhost:8080/api/resources/functions/$FUNC_ID \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"functionId":"TEST.FUNC.001","functionName":"Updated Function","action":"cmm.view","type":"쓰기","light":false,"standard":true,"enterprise":true,"systemMenu":false}'

# 4. 삭제
curl -s -X DELETE -H "Authorization: Bearer $TOKEN" \
  http://localhost:8080/api/resources/functions/$FUNC_ID
# Expected: 204
```

### Scenario 3: Version Tag → Rollback
**Description**: 버전 태깅 후 롤백이 데이터를 올바르게 복원하는지 확인

```bash
# 1. 데이터 생성 (2건)
curl -s -X POST http://localhost:8080/api/resources/functions \
  -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" \
  -d '{"functionId":"V1.FUNC.001","functionName":"Func 1","action":"cmm.view","type":"읽기","light":false,"standard":true,"enterprise":true,"systemMenu":false}'

curl -s -X POST http://localhost:8080/api/resources/functions \
  -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" \
  -d '{"functionId":"V1.FUNC.002","functionName":"Func 2","action":"cmm.insert","type":"쓰기","light":false,"standard":true,"enterprise":true,"systemMenu":false}'

# 2. 버전 태그 생성
VERSION_ID=$(curl -s -X POST http://localhost:8080/api/versions \
  -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" \
  -d '{"tagName":"v1.0","description":"Initial version","resourceType":"functions"}' | jq -r '.id')

# 3. 데이터 추가 변경 (1건 더 추가)
curl -s -X POST http://localhost:8080/api/resources/functions \
  -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" \
  -d '{"functionId":"V1.FUNC.003","functionName":"Func 3","action":"cmm.delete","type":"쓰기","light":false,"standard":true,"enterprise":true,"systemMenu":false}'

# 4. 현재 3건 확인
COUNT=$(curl -s -H "Authorization: Bearer $TOKEN" http://localhost:8080/api/resources/functions | jq 'length')
echo "Before rollback: $COUNT"  # Expected: 3

# 5. 롤백
curl -s -X POST -H "Authorization: Bearer $TOKEN" http://localhost:8080/api/versions/$VERSION_ID/rollback

# 6. 2건으로 복원 확인
COUNT=$(curl -s -H "Authorization: Bearer $TOKEN" http://localhost:8080/api/resources/functions | jq 'length')
echo "After rollback: $COUNT"  # Expected: 2
```

### Scenario 4: Import/Export Round-Trip
**Description**: Export → Import로 데이터가 손실 없이 복원되는지 확인

```bash
# 1. Export
curl -s -X POST -H "Authorization: Bearer $TOKEN" \
  http://localhost:8080/api/transfer/functions/export -o /tmp/functions_export.tsv

# 2. 데이터 전부 삭제 (테스트용)
# ... (개별 삭제 또는 버전 롤백)

# 3. Import preview
curl -s -X POST -H "Authorization: Bearer $TOKEN" \
  -F "file=@/tmp/functions_export.tsv" \
  http://localhost:8080/api/transfer/functions/import/preview | jq '.totalRows'
# Expected: 2 (이전에 생성한 행 수)
```

## Cleanup
```bash
# Backend 종료
# Ctrl+C 또는
kill $(lsof -t -i:8080)

# 테스트 DB 삭제
rm -f backend/resource-manager.db
```

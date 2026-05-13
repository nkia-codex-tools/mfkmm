# Unit Test Execution - MKFMM

## Run All Unit Tests

```bash
cd backend
./gradlew test
```

## Per-Service Test Execution

```bash
# Auth Service
./gradlew :auth-service:test

# User Service
./gradlew :user-service:test

# Resource Service
./gradlew :resource-service:test

# History Service
./gradlew :history-service:test

# DataIO Service
./gradlew :dataio-service:test

# Deploy Service
./gradlew :deploy-service:test
```

## Frontend Tests

```bash
cd frontend
npm test
```

---

## Test Report Location

| Service | Report Path |
|---|---|
| Auth Service | `backend/auth-service/build/reports/tests/test/index.html` |
| User Service | `backend/user-service/build/reports/tests/test/index.html` |
| Resource Service | `backend/resource-service/build/reports/tests/test/index.html` |
| History Service | `backend/history-service/build/reports/tests/test/index.html` |
| DataIO Service | `backend/dataio-service/build/reports/tests/test/index.html` |
| Deploy Service | `backend/deploy-service/build/reports/tests/test/index.html` |
| Frontend | `frontend/coverage/` |

---

## PBT (Property-Based Tests) 포함

각 서비스의 PBT 테스트는 jqwik 엔진으로 실행됩니다:

| Service | PBT Tests | Properties |
|---|---|---|
| Auth Service | JwtTokenPropertyTest, PasswordPropertyTest, AccountLockPropertyTest | JWT encode/decode, bcrypt round-trip, lock threshold |
| User Service | UserPermissionPropertyTest, UserValidationPropertyTest | Root immutability, self-demotion, userId format |
| Resource Service | LevenshteinPropertyTest, ResourceInvariantPropertyTest | Symmetry, identity, triangle, oracle, max 5, threshold |
| History Service | EventConsumerPropertyTest | Idempotent processing, event mapping |

### PBT 실패 시

```bash
# 실패한 seed로 재현
./gradlew :resource-service:test --tests "*.LevenshteinPropertyTest" -Djqwik.seed=<FAILED_SEED>
```

---

## Expected Results

- **총 테스트**: 약 50~70개 (PBT property 포함)
- **Pass 기준**: 100% pass, 0 failures
- **PBT 실행 횟수**: 각 property 기본 1000회 시도

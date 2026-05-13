# Build and Test Summary - MKFMM

## Build Status

| Item | Value |
|---|---|
| Build Tool | Gradle 8.x (Backend), npm (Frontend) |
| Build Command | `cd backend && ./gradlew clean build` + `cd frontend && npm run build` |
| Docker Build | `docker-compose up --build` |
| Total Services | 9 (7 backend + 1 frontend + 1 gateway) |
| Infrastructure | MongoDB 7, RabbitMQ 3 |

## Service Port Map

| Service | Port | Health Check |
|---|---|---|
| API Gateway | 8080 | /actuator/health |
| Auth Service | 8081 | /actuator/health |
| User Service | 8082 | /actuator/health |
| History Service | 8083 | /actuator/health |
| Resource Service | 8084 | /actuator/health |
| DataIO Service | 8085 | /actuator/health |
| Deploy Service | 8086 | /actuator/health |
| Frontend | 3000 | — |
| MongoDB | 27017 | mongosh ping |
| RabbitMQ | 5672 / 15672 | diagnostics |

---

## Test Execution Summary

### Unit Tests (per service)
| Service | Tests | PBT Properties | Framework |
|---|---|---|---|
| Auth Service | ~10 | 4 (JWT, bcrypt, lock) | JUnit 5 + jqwik |
| User Service | ~8 | 7 (permissions, validation) | JUnit 5 + jqwik |
| Resource Service | ~6 | 10 (Levenshtein, invariants) | JUnit 5 + jqwik |
| History Service | ~4 | 3 (idempotent, mapping) | JUnit 5 + jqwik |
| DataIO Service | ~6 | TBD | JUnit 5 + jqwik |
| Deploy Service | ~4 | TBD | JUnit 5 + jqwik |
| Frontend | ~20 | — | Vitest + fast-check |
| **Total** | **~58** | **24+** | — |

### Integration Tests
| Scenario | Description | Services Involved |
|---|---|---|
| 인증 흐름 | Login → JWT → Protected API | Auth, Gateway |
| 사용자 관리 | CRUD + 권한 | User, Gateway |
| 리소스 + 유사도 | 등록 → 중복/유사 탐지 | Resource, Gateway |
| 이벤트 이력 | 작업 → 이벤트 → 이력 기록 | All → History |
| IMPORT/EXPORT | 파일 처리 | DataIO, Resource |
| 배포 | 전체 EXPORT + 이력 | Deploy, DataIO |

### Performance Tests
- 대상: 사내 도구 (10~50명), 특별 성능 기준 없음
- 기본 부하 테스트: 동시 50 요청 시 정상 응답 확인

---

## Generated Instruction Files

| File | Purpose |
|---|---|
| `build-instructions.md` | 빌드 환경 설정 및 전체 빌드 절차 |
| `unit-test-instructions.md` | 단위 테스트 실행 + PBT 포함 |
| `integration-test-instructions.md` | 서비스 간 통합 테스트 시나리오 |
| `build-and-test-summary.md` | 전체 요약 (이 문서) |

---

## Overall Status

| Category | Status |
|---|---|
| Build | ⏳ Ready to execute |
| Unit Tests | ⏳ Ready to execute |
| Integration Tests | ⏳ Ready to execute |
| Performance Tests | N/A (사내 도구, 특별 기준 없음) |
| Ready for Operations | ⏳ After tests pass |

## Quick Start (전체 실행 요약)

```bash
# 1. 키 생성
./generate-keys.sh

# 2. 백엔드 빌드
cd backend && ./gradlew clean build -x test && cd ..

# 3. 프론트엔드 빌드
cd frontend && npm install && npm run build && cd ..

# 4. Docker Compose 실행
docker-compose up --build

# 5. 테스트
cd backend && ./gradlew test

# 6. 통합 테스트 (integration-test-instructions.md 참조)
```

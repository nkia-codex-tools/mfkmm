# Unit Test Execution

## Backend Unit Tests

### Run All Backend Tests
```bash
cd backend
./gradlew test
```

### Run Specific Test Class
```bash
./gradlew test --tests "com.resourcemanager.auth.service.AuthServiceTest"
./gradlew test --tests "com.resourcemanager.auth.service.UserServiceTest"
./gradlew test --tests "com.resourcemanager.auth.property.JwtTokenProviderPropertyTest"
```

### Test Report
- HTML Report: `backend/build/reports/tests/test/index.html`
- XML Results: `backend/build/test-results/test/`

### Expected Results
| Test Class | Tests | Expected |
|-----------|-------|----------|
| AuthServiceTest | 6 | All PASS |
| UserServiceTest | 5 | All PASS |
| JwtTokenProviderPropertyTest | 3 PBT | All PASS (100 tries each) |

## Frontend Unit Tests

### Run All Frontend Tests
```bash
cd frontend
npm test         # Watch mode
npm run test:run # Single run (CI mode)
```

### Test Configuration
- Framework: Vitest
- Environment: jsdom
- Setup: `src/test-setup.ts` (jest-dom matchers)

### Expected Results
- All tests PASS
- Coverage target: 80%+ business logic

## PBT (Property-Based Testing) Notes

### Backend (jqwik)
- Seed 로깅: jqwik 기본 지원 (실패 시 seed 자동 출력)
- 재현: 테스트 실패 시 출력된 seed를 `@Property(seed = "...")` 에 지정
- Shrinking: 기본 활성화 (최소 재현 케이스 자동 탐색)
- Default tries: 100 (설정 변경 가능)

### Frontend (fast-check)
- Seed 로깅: 실패 시 자동 출력
- 재현: `fc.assert(fc.property(...), { seed: 12345 })`
- Shrinking: 기본 활성화

## CI Integration

```bash
# CI에서 전체 테스트 실행
cd backend && ./gradlew test && cd ..
cd frontend && npm run test:run && cd ..
```

Exit code 0 = 모든 테스트 통과

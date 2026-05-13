# Code Generation Plan - Resource Service

## Unit Context
- **Unit**: Resource Service
- **Path**: `backend/resource-service/`
- **Stories**: US-3.1 (검색), US-4.1 (등록 - 중복/유사), US-4.2 (변경), US-4.3 (삭제)
- **Dependencies**: Shared Library (이벤트, DTO, exceptions)
- **Database**: MongoDB (resource-db) — resources collection
- **Messaging**: RabbitMQ (mkfmm.resource exchange)
- **Key Feature**: Levenshtein 유사도 탐지 (threshold ≤ 3, max 5 results)

## Code Generation Steps

### Phase A: Project Structure
- [x] Step 1: `build.gradle.kts` — 의존성
- [x] Step 2: `Dockerfile`
- [x] Step 3: `src/main/resources/application.yml`
- [x] Step 4: `ResourceServiceApplication.java`

### Phase B: Domain Layer
- [x] Step 5: `domain/model/Resource.java` — Resource 엔티티 (Soft Delete 지원)
- [x] Step 6: `domain/model/ResourceType.java` — enum
- [x] Step 7: `domain/model/SimilarityResult.java` — Value Object
- [x] Step 8: `domain/model/DuplicateCheckResult.java` — Value Object
- [x] Step 9: `domain/service/LevenshteinCalculator.java` — 편집 거리 계산 Domain Service

### Phase C: Application Layer (Ports)
- [x] Step 10: `application/port/inbound/ResourceUseCase.java`
- [x] Step 11: `application/port/inbound/ResourceSearchUseCase.java`
- [x] Step 12: `application/port/inbound/SimilarityUseCase.java`
- [x] Step 13: `application/port/outbound/ResourceRepository.java`
- [x] Step 14: `application/port/outbound/EventPublisherPort.java`

### Phase D: Application Layer (Services)
- [x] Step 15: `application/service/ResourceService.java` — CRUD + 중복/유사 연동
- [x] Step 16: `application/service/ResourceSearchService.java` — 검색
- [x] Step 17: `application/service/SimilarityService.java` — 유사도 탐지 로직
- [x] Step 18: `application/service/HardDeleteService.java` — 30일 스케줄러

### Phase E: Adapters (Controllers + DTOs)
- [x] Step 19: `adapter/inbound/controller/ResourceController.java` — CRUD API
- [x] Step 20: `adapter/inbound/controller/SearchController.java` — 검색 API
- [x] Step 21: `adapter/inbound/dto/*.java` — Request/Response DTOs

### Phase F: Adapters (Persistence + Messaging)
- [x] Step 22: `adapter/outbound/persistence/document/ResourceDocument.java`
- [x] Step 23: `adapter/outbound/persistence/repository/SpringDataResourceRepository.java`
- [x] Step 24: `adapter/outbound/persistence/MongoResourceRepositoryAdapter.java`
- [x] Step 25: `adapter/outbound/messaging/RabbitEventPublisher.java`
- [x] Step 26: `config/RabbitConfig.java`
- [x] Step 27: `config/MongoConfig.java` — 인덱스 초기화

### Phase G: Shared Library 이벤트 추가
- [x] Step 28: `shared/.../event/ResourceEvents.java`

### Phase H: Tests (PBT + Example-based)
- [x] Step 29: `test/LevenshteinPropertyTest.java` — PBT: 대칭, 항등, 삼각부등식, Oracle
- [x] Step 30: `test/ResourceServiceTest.java` — 핵심 비즈니스 로직 단위 테스트
- [x] Step 31: `test/SimilarityServiceTest.java` — 유사도 탐지 테스트
- [x] Step 32: `test/ResourceInvariantPropertyTest.java` — PBT: 중복 차단, Soft Delete 제외, max 5

---

## Story Traceability

| Story | Steps |
|---|---|
| US-3.1 검색 | Step 16, 20 |
| US-4.1 등록 (중복/유사) | Step 15, 17, 19 |
| US-4.2 변경 | Step 15, 19 |
| US-4.3 삭제 | Step 15, 18, 19 |

## PBT Compliance

| Property | Test | Rule |
|---|---|---|
| Levenshtein symmetric | Step 29 | PBT-03 (Invariant) |
| Levenshtein identity | Step 29 | PBT-03 (Invariant) |
| Levenshtein triangle inequality | Step 29 | PBT-03 (Invariant) |
| Levenshtein vs brute force | Step 29 | PBT-05 (Oracle) |
| Duplicate blocks create | Step 32 | PBT-03 (Invariant) |
| Soft delete hides from search | Step 32 | PBT-03 (Invariant) |
| Similarity max 5 results | Step 32 | PBT-03 (Invariant) |
| Similarity threshold ≤ 3 | Step 32 | PBT-03 (Invariant) |

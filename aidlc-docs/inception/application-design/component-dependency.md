# MKFMM Component Dependencies

## Dependency Matrix

| Component | Depends On | Depended By |
|---|---|---|
| Auth | UserRepository, SessionRepository, LoginHistoryRepository, HistoryUseCase | SecurityFilter |
| User | UserRepository, HistoryUseCase | Auth (사용자 조회) |
| Permission | UserRepository, AuditLogRepository | — |
| Resource | ResourceRepository, SimilarityUseCase, HistoryUseCase | DataIO, Deploy, Search |
| Similarity | ResourceRepository, HistoryUseCase | Resource, DataIO |
| ResourceSearch | ResourceRepository, HistoryUseCase | DataIO (EXPORT 쿼리) |
| DataIO | FileProcessor, ResourceRepository, SimilarityUseCase, HistoryUseCase | Deploy |
| Deploy | ExportUseCase, DeploymentRepository, HistoryUseCase | — |
| History | WorkLogRepository | Auth, User, Resource, Similarity, DataIO, Deploy, Search |

---

## Dependency Diagram

```
+-------+     +------+     +------------+
| Auth  |---->| User |     | Permission |
+-------+     +------+     +------------+
    |              |              |
    v              v              v
+--------------------------------------------------+
|              HistoryService                       |
+--------------------------------------------------+
    ^              ^              ^
    |              |              |
+----------+  +-----------+  +--------+
| Resource |->| Similarity|  | Search |
+----------+  +-----------+  +--------+
    ^                              |
    |                              |
+--------+                         |
| DataIO |<------------------------+
+--------+
    ^
    |
+--------+
| Deploy |
+--------+
```

---

## Communication Patterns

### Frontend → Backend
- **Protocol**: HTTP REST (JSON)
- **Specification**: OpenAPI 3.0 (Swagger)
- **Authentication**: Session Cookie (JSESSIONID)
- **CORS**: 프론트엔드 도메인만 허용

### Backend Internal (Component → Component)
- **Pattern**: Direct method call via Port interfaces
- **DI**: Spring IoC Container (Constructor Injection)
- **Coupling**: Inbound Port 인터페이스를 통한 loose coupling

### Backend → MongoDB
- **Driver**: Spring Data MongoDB
- **Session Store**: Spring Session MongoDB
- **Connection**: MongoDB URI (Docker 네트워크 내)

---

## Data Flow Patterns

### 1. Resource Registration Flow
```
Frontend → ResourceController → ResourceService
    → SimilarityService.checkDuplicate()
    → SimilarityService.findSimilar()
    → [Response: duplicates/suggestions]
    → [User choice]
    → ResourceRepository.save()
    → HistoryService.recordWork()
```

### 2. Login Flow
```
Frontend → AuthController → AuthService
    → UserRepository.findByUserId()
    → AccountLock check (fail count)
    → Password verification (bcrypt)
    → SessionRepository.save()
    → LoginHistoryRepository.save()
```

### 3. Import Flow
```
Frontend → DataIOController → ImportService
    → FileProcessor.parse() (Excel/TSV/JSON)
    → For each row:
        → SimilarityService.checkDuplicate()
        → Apply conflict policy
        → ResourceRepository.save()
    → Generate ImportResult
    → HistoryService.recordWork()
```

### 4. Deploy Flow
```
Frontend → DeployController → DeployService
    → ExportService.exportAll(format)
    → DeploymentRepository.save(metadata)
    → HistoryService.recordWork()
    → [Return file download]
```

---

## Layer Rules (Hexagonal)

| Rule | Description |
|---|---|
| Domain은 외부 의존 없음 | Entity, Value Object는 어떤 프레임워크에도 의존하지 않음 |
| Port는 인터페이스 | Inbound Port = Use Case interface, Outbound Port = Repository interface |
| Adapter는 Port를 구현 | Controller → Inbound Adapter, MongoRepository → Outbound Adapter |
| 의존성 방향은 안쪽 | Adapter → Application → Domain (항상 안쪽을 향함) |
| Domain Service는 순수 로직 | Levenshtein 계산, 중복 판정 등 순수 도메인 로직 |

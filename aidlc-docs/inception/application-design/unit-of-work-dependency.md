# MKFMM Unit of Work Dependencies

## Dependency Matrix

| Unit | Depends On | Communication | Notes |
|---|---|---|---|
| Auth Service | Shared Library | — | 독립적 (사용자 인증 정보 자체 DB 보유) |
| User Service | Shared Library | — | 독립적 (사용자 계정 자체 관리) |
| History Service | Shared Library | Events (Consumer) | 이벤트 수신만 (다른 서비스에 의존하지 않음) |
| Resource Service | Shared Library | Events (Publisher) | History에 이벤트 발행 |
| DataIO Service | Resource Service, Shared Library | REST + Events | 중복 검증 시 Resource Service REST 호출 |
| Deploy Service | DataIO Service, Shared Library | REST + Events | 전체 EXPORT 시 DataIO Service 호출 |
| API Gateway | Auth Service (JWT 검증), Shared Library | REST | JWT 검증을 위해 Auth public key 참조 |
| Frontend | API Gateway | HTTP/REST | 모든 요청은 Gateway를 통해 |

---

## Event Flow Diagram

```
+------------------+     Events      +------------------+
| Resource Service |---------------->| History Service  |
+------------------+                 +------------------+
  ResourceCreated                         ^    ^    ^
  ResourceUpdated                         |    |    |
  ResourceDeleted                         |    |    |
  ResourceSearched                        |    |    |
  SimilarityChoiceMade                    |    |    |
                                          |    |    |
+------------------+     Events           |    |    |
| DataIO Service   |---------------------+    |    |
+------------------+                          |    |
  ImportCompleted                              |    |
  ExportCompleted                              |    |
                                               |    |
+------------------+     Events                |    |
| Deploy Service   |-------------------------+    |
+------------------+                              |
  DeployCompleted                                  |
                                                   |
+------------------+     Events                    |
| Auth Service     |------------------------------+
+------------------+
  UserLoggedIn
  UserLoggedOut
  AccountLocked
  AccountUnlocked

+------------------+     Events
| User Service     |-------------------------------> History Service
+------------------+
  UserCreated
  UserDeleted
  PermissionChanged
```

---

## REST Dependencies (동기 호출)

```
Frontend ---> API Gateway ---> Auth Service     (JWT 검증)
                          ---> User Service     (사용자 CRUD)
                          ---> Resource Service (리소스 CRUD/검색)
                          ---> DataIO Service   (IMPORT/EXPORT)
                          ---> Deploy Service   (배포)
                          ---> History Service  (이력 조회)

DataIO Service --REST--> Resource Service  (중복 검증, 리소스 저장)
Deploy Service --REST--> DataIO Service    (전체 EXPORT)
```

---

## Implementation Dependency Graph

```
Phase 1: [Shared Library] + [Auth Service] + [API Gateway]
              |
              v
Phase 2: [User Service]
              |
              v
Phase 3: [History Service]
              |
              v
Phase 4: [Resource Service] + [Frontend] (병렬)
              |
              v
Phase 5: [DataIO Service]
              |
              v
Phase 6: [Deploy Service]
```

**Critical Path**: Shared → Auth + Gateway → User → History → Resource → DataIO → Deploy

---

## Shared Library Contents

| Package | Content | Used By |
|---|---|---|
| `com.mkfmm.shared.event` | 이벤트 클래스 정의 (ResourceCreated, UserLoggedIn 등) | All services |
| `com.mkfmm.shared.dto` | 공유 DTO (UserInfo, ResourceSummary 등) | All services |
| `com.mkfmm.shared.security` | JWT 유틸리티 (토큰 파싱, 검증) | API Gateway, All services |
| `com.mkfmm.shared.exception` | 공통 예외 클래스 | All services |

---

## Message Queue Configuration

| Exchange/Topic | Publisher | Consumer | Event |
|---|---|---|---|
| mkfmm.auth | Auth Service | History Service | UserLoggedIn, UserLoggedOut, AccountLocked, AccountUnlocked |
| mkfmm.user | User Service | History Service | UserCreated, UserDeleted, PermissionChanged |
| mkfmm.resource | Resource Service | History Service | ResourceCreated, ResourceUpdated, ResourceDeleted, ResourceSearched, SimilarityChoiceMade |
| mkfmm.dataio | DataIO Service | History Service | ImportCompleted, ExportCompleted |
| mkfmm.deploy | Deploy Service | History Service | DeployCompleted |

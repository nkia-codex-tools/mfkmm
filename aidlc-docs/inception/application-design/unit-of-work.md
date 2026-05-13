# MKFMM Units of Work

## Architecture: Microservices (Event-Driven)

**Deployment Model**: Microservices (각 도메인 독립 배포)
**Communication**: Event-based (메시지 큐)
**Authentication**: API Gateway + JWT
**Repository Layout**: Monorepo

---

## Project Directory Structure

```
mkfmm/
├── backend/
│   ├── api-gateway/          # Unit 7: API Gateway
│   ├── auth-service/         # Unit 1: Auth Service
│   ├── user-service/         # Unit 2: User Service
│   ├── history-service/      # Unit 3: History Service
│   ├── resource-service/     # Unit 4: Resource Service
│   ├── dataio-service/       # Unit 5: DataIO Service
│   ├── deploy-service/       # Unit 6: Deploy Service
│   └── shared/               # 공유 라이브러리 (이벤트 스키마, DTO 등)
├── frontend/                 # Unit 8: Frontend
│   ├── src/
│   │   ├── features/
│   │   ├── shared/
│   │   └── ...
│   └── package.json
├── docker-compose.yml        # 전체 서비스 오케스트레이션
└── aidlc-docs/               # 문서 (코드 아님)
```

---

## Unit Definitions

### Unit 1: Auth Service
| Attribute | Value |
|---|---|
| **Service Name** | auth-service |
| **Path** | `backend/auth-service/` |
| **Technology** | Spring Boot + Spring Security |
| **Responsibility** | 로그인 인증, JWT 발급/갱신, 계정 잠금/해제, 로그인 이력 |
| **Database** | MongoDB (auth-db) — users 인증 정보, login_history, account_locks |
| **Exposed API** | POST /auth/login, POST /auth/logout, POST /auth/refresh, GET /auth/login-history |
| **Events Published** | UserLoggedIn, UserLoggedOut, AccountLocked, AccountUnlocked |
| **Events Consumed** | — |
| **Priority** | 1 (최우선 — 모든 서비스의 기반) |

### Unit 2: User Service
| Attribute | Value |
|---|---|
| **Service Name** | user-service |
| **Path** | `backend/user-service/` |
| **Technology** | Spring Boot |
| **Responsibility** | 사용자 CRUD, 권한(Role) 관리, 감사 로그 |
| **Database** | MongoDB (user-db) — users, audit_logs |
| **Exposed API** | CRUD /users, PUT /users/{id}/permission, GET /audit-logs |
| **Events Published** | UserCreated, UserDeleted, PermissionChanged |
| **Events Consumed** | — |
| **Priority** | 2 (Auth 다음 — Auth가 사용자 정보 조회에 의존) |

### Unit 3: History Service
| Attribute | Value |
|---|---|
| **Service Name** | history-service |
| **Path** | `backend/history-service/` |
| **Technology** | Spring Boot |
| **Responsibility** | 작업 이력 기록(이벤트 수신), 이력 조회, 6개월 검색 이력 삭제 스케줄 |
| **Database** | MongoDB (history-db) — work_logs |
| **Exposed API** | GET /work-logs, GET /work-logs/my |
| **Events Published** | — |
| **Events Consumed** | ResourceCreated, ResourceUpdated, ResourceDeleted, ResourceSearched, ImportCompleted, ExportCompleted, DeployCompleted |
| **Priority** | 3 (다른 서비스들이 이벤트를 발행하면 수신하는 구조) |

### Unit 4: Resource Service
| Attribute | Value |
|---|---|
| **Service Name** | resource-service |
| **Path** | `backend/resource-service/` |
| **Technology** | Spring Boot |
| **Responsibility** | 리소스(메시지키/기능ID/메뉴ID) CRUD, 검색, 중복 검사, 유사도 탐지, Soft Delete |
| **Database** | MongoDB (resource-db) — resources |
| **Exposed API** | CRUD /resources, GET /resources/search, POST /resources/check-duplicate, POST /resources/find-similar |
| **Events Published** | ResourceCreated, ResourceUpdated, ResourceDeleted, ResourceSearched, SimilarityChoiceMade |
| **Events Consumed** | — |
| **Priority** | 4 (핵심 비즈니스 — Auth/User/History 기반 위에 구축) |

### Unit 5: DataIO Service
| Attribute | Value |
|---|---|
| **Service Name** | dataio-service |
| **Path** | `backend/dataio-service/` |
| **Technology** | Spring Boot |
| **Responsibility** | IMPORT(Excel/TSV/JSON 파싱, 중복 검증), EXPORT(부분/전체, 형식 변환) |
| **Database** | MongoDB (dataio-db) — import_jobs (비동기 작업 상태) |
| **Exposed API** | POST /import, GET /import/{jobId}/status, POST /export/partial, POST /export/all |
| **Events Published** | ImportCompleted, ExportCompleted |
| **Events Consumed** | — |
| **Dependencies** | Resource Service (REST 호출 — 중복 검증, 리소스 저장) |
| **Priority** | 5 (Resource Service 의존) |

### Unit 6: Deploy Service
| Attribute | Value |
|---|---|
| **Service Name** | deploy-service |
| **Path** | `backend/deploy-service/` |
| **Technology** | Spring Boot |
| **Responsibility** | 전체 데이터 배포, 배포 이력, 재다운로드 |
| **Database** | MongoDB (deploy-db) — deployments |
| **Exposed API** | POST /deploy, GET /deploy/history, GET /deploy/{id}/download |
| **Events Published** | DeployCompleted |
| **Events Consumed** | — |
| **Dependencies** | DataIO Service (전체 EXPORT 재사용) |
| **Priority** | 6 (DataIO Service 의존) |

### Unit 7: API Gateway
| Attribute | Value |
|---|---|
| **Service Name** | api-gateway |
| **Path** | `backend/api-gateway/` |
| **Technology** | Spring Cloud Gateway |
| **Responsibility** | 라우팅, JWT 검증, CORS, Rate Limiting |
| **Database** | — (Stateless) |
| **Exposed API** | 모든 외부 요청의 단일 진입점 |
| **Events Published** | — |
| **Events Consumed** | — |
| **Priority** | 1 (Auth와 동시 — Frontend 접근점) |

### Unit 8: Frontend
| Attribute | Value |
|---|---|
| **Service Name** | frontend |
| **Path** | `frontend/` |
| **Technology** | React + TypeScript + Tailwind CSS + Headless UI |
| **Responsibility** | 전체 사용자 인터페이스 (Auth, Resource, DataIO, Admin) |
| **API Client** | OpenAPI 자동 생성 클라이언트 → API Gateway 호출 |
| **State Management** | React Context + useReducer |
| **Priority** | 4 (Backend API 확정 후 병렬 개발 가능) |

---

## Implementation Order (의존성 기반)

```
Phase 1 (기반):  Auth Service + API Gateway + Shared Library
Phase 2 (계정):  User Service
Phase 3 (이력):  History Service
Phase 4 (핵심):  Resource Service + Frontend (병렬)
Phase 5 (부가):  DataIO Service
Phase 6 (배포):  Deploy Service
```

---

## Code Organization Strategy

### Backend (각 서비스 공통 구조 - Hexagonal)
```
backend/{service-name}/
├── src/main/java/com/mkfmm/{service}/
│   ├── adapter/
│   │   ├── inbound/          # REST Controllers
│   │   └── outbound/         # MongoDB Repositories, Event Publishers
│   ├── application/
│   │   ├── port/
│   │   │   ├── inbound/      # Use Case interfaces
│   │   │   └── outbound/     # Repository interfaces, Event Port
│   │   └── service/          # Use Case implementations
│   ├── domain/
│   │   ├── model/            # Entities, Value Objects
│   │   └── service/          # Domain Services (pure logic)
│   └── config/               # Spring Configuration
├── src/main/resources/
│   └── application.yml
├── src/test/java/
├── Dockerfile
└── pom.xml
```

### Shared Library
```
backend/shared/
├── src/main/java/com/mkfmm/shared/
│   ├── event/                # Event schemas (published/consumed)
│   ├── dto/                  # Shared DTOs
│   ├── security/             # JWT utilities
│   └── exception/            # Common exceptions
└── pom.xml
```

### Frontend
```
frontend/
├── src/
│   ├── features/
│   │   ├── auth/             # Login, AuthContext
│   │   ├── resource/         # Resource CRUD, Search, Similarity
│   │   ├── dataio/           # Import, Export
│   │   └── admin/            # Users, Permissions, Deploy, History
│   ├── shared/
│   │   ├── components/       # Layout, Navigation, Pagination, Modal
│   │   ├── hooks/            # Common hooks
│   │   ├── api/              # Generated OpenAPI client
│   │   └── utils/            # Formatters, validators
│   ├── App.tsx
│   └── main.tsx
├── package.json
└── Dockerfile
```

---

## Infrastructure Components (Docker Compose)

| Service | Image/Build | Port | Purpose |
|---|---|---|---|
| api-gateway | build: backend/api-gateway | 8080 | 외부 진입점 |
| auth-service | build: backend/auth-service | 8081 | 인증 |
| user-service | build: backend/user-service | 8082 | 사용자 관리 |
| history-service | build: backend/history-service | 8083 | 이력 관리 |
| resource-service | build: backend/resource-service | 8084 | 리소스 관리 |
| dataio-service | build: backend/dataio-service | 8085 | IMPORT/EXPORT |
| deploy-service | build: backend/deploy-service | 8086 | 배포 관리 |
| frontend | build: frontend | 3000 | UI |
| mongodb | mongo:7 | 27017 | 데이터베이스 |
| rabbitmq | rabbitmq:3-management | 5672/15672 | 메시지 큐 |

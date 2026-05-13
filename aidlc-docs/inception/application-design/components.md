# MKFMM Components

## Architecture: Hexagonal (Ports & Adapters)

```
+--------------------------------------------------+
|                  FRONTEND (React)                 |
|  Pages / Components / Hooks / Context            |
+--------------------------------------------------+
                       |  REST API (OpenAPI)
                       v
+--------------------------------------------------+
|               BACKEND (Spring Boot)              |
|                                                  |
|  +--------------------------------------------+  |
|  |          ADAPTERS (Inbound)                |  |
|  |  REST Controllers (Primary Adapters)       |  |
|  +--------------------------------------------+  |
|                       |                          |
|  +--------------------------------------------+  |
|  |          APPLICATION (Ports)               |  |
|  |  Use Cases / Application Services          |  |
|  +--------------------------------------------+  |
|                       |                          |
|  +--------------------------------------------+  |
|  |          DOMAIN                            |  |
|  |  Entities / Domain Services / Value Objects|  |
|  +--------------------------------------------+  |
|                       |                          |
|  +--------------------------------------------+  |
|  |          ADAPTERS (Outbound)               |  |
|  |  MongoDB Repositories / File Processors    |  |
|  +--------------------------------------------+  |
|                                                  |
+--------------------------------------------------+
                       |
                       v
+--------------------------------------------------+
|              MONGODB (Data Store)                 |
+--------------------------------------------------+
```

---

## Backend Components

### 1. Auth Component
| Attribute | Value |
|---|---|
| **Package** | `com.mkfmm.auth` |
| **Responsibility** | 인증, 세션 관리, 로그인 이력, 계정 잠금 |
| **Domain Entities** | Session, LoginHistory, AccountLock |
| **Inbound Port** | AuthUseCase |
| **Outbound Port** | SessionRepository, LoginHistoryRepository |

### 2. User Component
| Attribute | Value |
|---|---|
| **Package** | `com.mkfmm.user` |
| **Responsibility** | 사용자 계정 CRUD, 권한 관리, 감사 로그 |
| **Domain Entities** | User, Role, AuditLog |
| **Inbound Port** | UserManagementUseCase, PermissionUseCase |
| **Outbound Port** | UserRepository, AuditLogRepository |

### 3. Resource Component
| Attribute | Value |
|---|---|
| **Package** | `com.mkfmm.resource` |
| **Responsibility** | 리소스(메시지키/기능ID/메뉴ID) CRUD, 검색, Soft Delete |
| **Domain Entities** | Resource, ResourceType |
| **Inbound Port** | ResourceUseCase, ResourceSearchUseCase |
| **Outbound Port** | ResourceRepository |

### 4. Similarity Component
| Attribute | Value |
|---|---|
| **Package** | `com.mkfmm.similarity` |
| **Responsibility** | 중복 검사, 유사도 탐지(문자열 일치 + Levenshtein), 유사 후보 제안 |
| **Domain Entities** | SimilarityResult, DuplicateCheck |
| **Inbound Port** | SimilarityUseCase |
| **Outbound Port** | ResourceRepository (조회용) |

### 5. DataIO Component
| Attribute | Value |
|---|---|
| **Package** | `com.mkfmm.dataio` |
| **Responsibility** | IMPORT/EXPORT 파일 처리, 형식 변환(Excel/TSV/JSON), 동기/비동기 판단 |
| **Domain Entities** | ImportResult, ExportRequest |
| **Inbound Port** | ImportUseCase, ExportUseCase |
| **Outbound Port** | ResourceRepository, FileProcessor |

### 6. Deploy Component
| Attribute | Value |
|---|---|
| **Package** | `com.mkfmm.deploy` |
| **Responsibility** | 전체 데이터 배포, 배포 이력 관리, 재다운로드 |
| **Domain Entities** | Deployment, DeploymentHistory |
| **Inbound Port** | DeployUseCase |
| **Outbound Port** | DeploymentRepository, ExportUseCase(재사용) |

### 7. History Component
| Attribute | Value |
|---|---|
| **Package** | `com.mkfmm.history` |
| **Responsibility** | 작업 이력 기록/조회, 이력 보존 정책(6개월 검색 이력 삭제) |
| **Domain Entities** | WorkLog, WorkLogType |
| **Inbound Port** | HistoryUseCase |
| **Outbound Port** | WorkLogRepository |

---

## Frontend Components

### 1. Auth Module
| Attribute | Value |
|---|---|
| **Path** | `src/features/auth/` |
| **Responsibility** | 로그인/로그아웃 화면, 세션 관리 Context |
| **Pages** | LoginPage |
| **Context** | AuthContext (사용자 정보, 권한, 세션 상태) |

### 2. Resource Module
| Attribute | Value |
|---|---|
| **Path** | `src/features/resource/` |
| **Responsibility** | 리소스 검색/목록/등록/수정/삭제 화면 |
| **Pages** | ResourceListPage, ResourceDetailPage, ResourceFormPage |
| **Components** | SearchBar, ResourceTable, SimilarityPanel, DuplicateAlert |

### 3. DataIO Module
| Attribute | Value |
|---|---|
| **Path** | `src/features/dataio/` |
| **Responsibility** | IMPORT 업로드, EXPORT 다운로드 화면 |
| **Pages** | ImportPage, ExportPage |
| **Components** | FileUploader, ImportResultReport, ExportFormatSelector |

### 4. Admin Module
| Attribute | Value |
|---|---|
| **Path** | `src/features/admin/` |
| **Responsibility** | 사용자 관리, 권한 관리, 배포 관리, 이력 조회 |
| **Pages** | UserManagementPage, PermissionPage, DeployPage, HistoryPage, LoginHistoryPage |
| **Components** | UserTable, PermissionEditor, DeployHistory, WorkLogTable |

### 5. Shared Module
| Attribute | Value |
|---|---|
| **Path** | `src/shared/` |
| **Responsibility** | 공통 UI 컴포넌트, 유틸리티, API 클라이언트 |
| **Components** | Layout, Navigation, Pagination, Modal, Toast |
| **Utils** | apiClient (OpenAPI 생성), formatters, validators |

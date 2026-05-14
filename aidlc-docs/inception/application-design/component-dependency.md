# Component Dependency

## Dependency Matrix

```
+------------------+------+------+----------+----------+---------+---------+--------+
| Component        | Auth | User | Function | Menu     | MsgRes  | History | Version|
+------------------+------+------+----------+----------+---------+---------+--------+
| AuthController   |  X   |      |          |          |         |         |        |
| UserController   |      |  X   |          |          |         |         |        |
| FunctionCtrl     |      |      |    X     |          |         |         |        |
| MenuController   |      |      |          |    X     |         |         |        |
| MsgResController |      |      |          |          |    X    |         |        |
| TransferCtrl     |      |      |    X     |    X     |    X    |         |        |
| HistoryCtrl      |      |      |          |          |         |    X    |        |
| VersionCtrl      |      |      |          |          |         |         |   X    |
+------------------+------+------+----------+----------+---------+---------+--------+
| AuthService      |      |  R   |          |          |         |         |        |
| UserService      |      |  R   |          |          |         |    W    |        |
| FunctionService  |      |      |    R     |          |         |    W    |        |
| MenuService      |      |      |    R     |    R     |         |    W    |        |
| MsgResService    |      |      |          |          |    R    |    W    |        |
| TsvExportService |      |      |    R     |    R     |    R    |         |        |
| TsvImportService |      |      |    R/W   |    R/W   |    R/W  |    W    |        |
| HistoryService   |      |      |          |          |         |    R    |        |
| VersionService   |      |      |    R/W   |    R/W   |    R/W  |    W    |   R    |
+------------------+------+------+----------+----------+---------+---------+--------+

R = Read dependency, W = Write dependency, X = Direct dependency
```

## Communication Patterns

### Request Flow
```
Client (React)
    |
    | HTTP (REST + JWT)
    v
+-------------------+
| Spring Security   |  <-- JwtAuthenticationFilter
| Filter Chain      |  <-- RateLimitFilter
|                   |  <-- SecurityHeadersFilter
+-------------------+
    |
    v
+-------------------+
| Controller Layer  |  <-- Request validation (@Valid)
+-------------------+
    |
    v
+-------------------+
| Service Layer     |  <-- Business logic
|                   |  <-- AuditAspect (이력 자동 기록)
+-------------------+
    |
    v
+-------------------+
| Repository Layer  |  <-- Spring Data JPA
+-------------------+
    |
    v
+-------------------+
| SQLite Database   |
+-------------------+
```

### Cross-Domain Dependencies

```
+----------+         +----------+
|   Menu   | ------> | Function |  (기능 ID 참조, 기능명 조회)
|  Service |         |  Service |
+----------+         +----------+
     |                     |
     v                     v
+----------+         +----------+
| History  | <------ | History  |
| Service  |         | Service  |
+----------+         +----------+
     ^                     ^
     |                     |
+----------+         +----------+
| Version  | ------> | All Res  |  (스냅샷 생성/롤백 시 리소스 접근)
| Service  |         | Repos    |
+----------+         +----------+

+----------+
| Transfer | ------> All Resource Repositories (import/export)
| Service  | ------> History Service (import 시 이력 기록)
+----------+
```

### Frontend Component Hierarchy

```
App
+-- AppLayout
    +-- Header (useAuthStore)
    +-- Sidebar (navigation)
    +-- Routes
        +-- LoginPage
        +-- RegisterPage
        +-- ResourcePage
        |   +-- ResourceTabs
        |   +-- GridToolbar (useResourceStore)
        |   +-- FunctionGrid / MenuGrid / MessageResourceGrid
        |   +-- ImportDialog
        |   +-- ExportDialog
        +-- AdminPage
        |   +-- UserList
        |   +-- PendingUserList
        |   +-- PermissionDialog
        +-- HistoryPage
        +-- VersionPage
            +-- VersionList
            +-- VersionDiff
            +-- RollbackConfirmDialog
            +-- TagCreateDialog
```

## Data Flow

### Resource CRUD Flow
```
User Action (AG Grid)
    |
    v
React Component --> Axios API call --> Spring Controller
                                            |
                                            v
                                       Service Layer
                                       (validation + business logic)
                                            |
                                            +---> Repository (DB write)
                                            |
                                            +---> HistoryService (이력 기록)
                                            |
                                            v
                                       Response DTO
                                            |
    v                                       |
UI Update (Zustand store) <--- JSON Response
```

### Import Flow
```
File Upload --> TsvParser (parse + validate)
                    |
                    v
              ImportPreview (변경/추가/충돌 분류)
                    |
                    v
              User Decision (덮어쓰기/건너뛰기)
                    |
                    v
              ImportApply --> Repository (bulk write)
                         --> HistoryService (bulk 이력)
                    |
                    v
              ImportResult (성공/실패 건수)
```

### Version Rollback Flow
```
Admin selects version tag
    |
    v
VersionService.rollback()
    |
    +---> Load snapshot JSON
    +---> Delete current data
    +---> Restore from snapshot
    +---> HistoryService.recordChange("ROLLBACK")
    |
    v
All resource data restored to tagged version state
```

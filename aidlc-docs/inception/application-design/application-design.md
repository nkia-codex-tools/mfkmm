# Application Design - Consolidated

## 1. Design Decisions Summary

| Decision | Choice | Rationale |
|----------|--------|-----------|
| Backend Package Structure | 도메인 기반 | 명확한 도메인 경계 (auth, resource, transfer, history, version) |
| Frontend State Management | Zustand | 경량, 간단한 API, 이 규모에 적합 |
| API Communication | REST API + Axios | 단순하고 직관적, 추가 라이브러리 최소화 |
| Resource Backend Design | 독립 엔티티/테이블 | 3개 리소스 컬럼 구조가 완전히 다름 |
| History Storage | 필드 레벨 diff | 정확한 변경 추적, 요구사항 부합 |
| Version Storage | 전체 스냅샷 JSON | 롤백 시 단순 복원 가능 |
| Frontend Routing | SPA (React Router) | URL 공유/북마크, 브라우저 내비게이션 지원 |

## 2. System Architecture

```
+---------------------------------------------------+
|                EC2 Instance                        |
|                                                   |
|  +---------------------------------------------+ |
|  |           Docker Compose                     | |
|  |                                              | |
|  |  +----------------+    +------------------+  | |
|  |  |   Frontend     |    |    Backend       |  | |
|  |  |  (React/TS)    |    | (Spring Boot)    |  | |
|  |  |   Nginx:80     |    |   Port:8080      |  | |
|  |  |                |    |                  |  | |
|  |  |  - React Router|    |  - Auth Domain   |  | |
|  |  |  - AG Grid     |    |  - Resource Dom  |  | |
|  |  |  - Zustand     |    |  - Transfer Dom  |  | |
|  |  |  - Axios       |    |  - History Dom   |  | |
|  |  |                |    |  - Version Dom   |  | |
|  |  |                |    |  - SQLite DB     |  | |
|  |  +-------+--------+    +--------+---------+  | |
|  |          |                       |            | |
|  |          +--- REST API (JWT) ----+            | |
|  |                                              | |
|  +---------------------------------------------+ |
|                                                   |
|  +---------------------------------------------+ |
|  |  Docker Volume: /data/resource-manager.db    | |
|  +---------------------------------------------+ |
+---------------------------------------------------+
```

## 3. Backend Domain Structure

```
com.resourcemanager/
+-- auth/
|   +-- controller/   (AuthController, UserController)
|   +-- service/      (AuthService, UserService)
|   +-- repository/   (UserRepository)
|   +-- entity/       (User)
|   +-- dto/          (LoginRequest, TokenResponse, etc.)
|   +-- security/     (JwtTokenProvider, JwtAuthenticationFilter, SecurityConfig)
|
+-- resource/
|   +-- controller/   (FunctionController, MenuController, MessageResourceController)
|   +-- service/      (FunctionService, MenuService, MessageResourceService)
|   +-- repository/   (FunctionRepository, MenuRepository, MessageResourceRepository)
|   +-- entity/       (FunctionResource, MenuResource, MessageResource)
|   +-- dto/          (Request/Response DTOs per resource type)
|
+-- transfer/
|   +-- controller/   (TransferController)
|   +-- service/      (TsvExportService, TsvImportService)
|   +-- util/         (TsvParser)
|   +-- dto/          (ImportPreviewResponse, ImportResultResponse, etc.)
|
+-- history/
|   +-- controller/   (HistoryController)
|   +-- service/      (HistoryService)
|   +-- repository/   (HistoryRepository)
|   +-- entity/       (ChangeHistory)
|   +-- dto/          (HistoryResponse, HistoryFilter)
|
+-- version/
|   +-- controller/   (VersionController)
|   +-- service/      (VersionService)
|   +-- repository/   (VersionRepository)
|   +-- entity/       (VersionTag)
|   +-- dto/          (VersionResponse, VersionDiffResponse, etc.)
|
+-- common/
    +-- exception/    (GlobalExceptionHandler, custom exceptions)
    +-- filter/       (RateLimitFilter, SecurityHeadersFilter)
    +-- aspect/       (AuditAspect)
    +-- config/       (CorsConfig, WebConfig)
    +-- dto/          (ErrorResponse, PageResponse)
```

## 4. Frontend Structure

```
src/
+-- pages/
|   +-- LoginPage.tsx
|   +-- RegisterPage.tsx
|   +-- ResourcePage.tsx
|   +-- AdminPage.tsx
|   +-- HistoryPage.tsx
|   +-- VersionPage.tsx
|
+-- components/
|   +-- resource/
|   |   +-- ResourceTabs.tsx
|   |   +-- FunctionGrid.tsx
|   |   +-- MenuGrid.tsx
|   |   +-- MessageResourceGrid.tsx
|   |   +-- GridToolbar.tsx
|   |   +-- ImportDialog.tsx
|   |   +-- ExportDialog.tsx
|   |
|   +-- admin/
|   |   +-- UserList.tsx
|   |   +-- PendingUserList.tsx
|   |   +-- PermissionDialog.tsx
|   |
|   +-- version/
|   |   +-- VersionList.tsx
|   |   +-- VersionDiff.tsx
|   |   +-- RollbackConfirmDialog.tsx
|   |   +-- TagCreateDialog.tsx
|   |
|   +-- common/
|       +-- AppLayout.tsx
|       +-- ProtectedRoute.tsx
|       +-- LoadingSpinner.tsx
|       +-- ErrorBoundary.tsx
|
+-- stores/
|   +-- useAuthStore.ts
|   +-- useResourceStore.ts
|   +-- useNotificationStore.ts
|
+-- api/
|   +-- apiClient.ts
|   +-- authApi.ts
|   +-- functionApi.ts
|   +-- menuApi.ts
|   +-- messageResourceApi.ts
|   +-- transferApi.ts
|   +-- historyApi.ts
|   +-- versionApi.ts
|
+-- types/
|   +-- auth.ts
|   +-- resource.ts
|   +-- history.ts
|   +-- version.ts
|
+-- App.tsx
+-- main.tsx
```

## 5. Database Schema Overview

### Tables

| Table | Purpose |
|-------|---------|
| users | 사용자 계정 (인증, 역할) |
| functions | 기능 리소스 데이터 |
| menus | 메뉴 리소스 데이터 |
| message_resources | 리소스 키 데이터 |
| change_history | 필드 레벨 변경 이력 |
| version_tags | 버전 태그 + 스냅샷 JSON |

### Key Relationships
- `menus.function_id` → `functions.function_id` (논리적 참조)
- `change_history.changed_by` → `users.id`
- `version_tags.created_by` → `users.id`
- 모든 리소스 테이블에 `row_order` (행 순서), `created_by`, `updated_by`

## 6. API Route Summary

| Domain | Base Path | Endpoints |
|--------|-----------|-----------|
| Auth | /api/auth | login, register, refresh |
| Admin | /api/admin | users, users/pending, users/{id}/role |
| Functions | /api/resources/functions | CRUD, batch, reorder |
| Menus | /api/resources/menus | CRUD, batch, reorder, function-ids |
| Message Resources | /api/resources/message-resources | CRUD, batch, reorder |
| Transfer | /api/transfer/{type} | export, import/preview, import/apply |
| History | /api/history | getHistory (filtered, paged) |
| Versions | /api/versions | list, create tag, diff, rollback |

## 7. Security Design

| Layer | Mechanism |
|-------|-----------|
| Authentication | JWT (Access + Refresh Token) |
| Authorization | Role-based (ADMIN, WRITER, READER, PENDING) |
| Password Storage | BCrypt adaptive hashing |
| API Protection | Rate limiting, input validation |
| HTTP Headers | CSP, HSTS, X-Content-Type-Options, X-Frame-Options, Referrer-Policy |
| CORS | Restricted to allowed origins |
| Token Validation | Server-side on every request (signature, expiration, audience) |

## 8. Cross-Cutting Concerns

| Concern | Implementation |
|---------|---------------|
| Logging | SLF4J + Logback, structured JSON format |
| Error Handling | GlobalExceptionHandler, generic user-facing messages |
| Audit Trail | AOP Aspect on service methods, automatic change recording |
| Rate Limiting | Filter-based, per-IP or per-user throttling |
| Input Validation | Bean Validation (@Valid), custom validators |

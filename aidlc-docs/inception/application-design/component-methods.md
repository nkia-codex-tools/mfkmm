# Component Methods

## Backend Methods

### Auth Domain

#### AuthController
```
POST /api/auth/login          → login(LoginRequest): TokenResponse
POST /api/auth/register       → register(RegisterRequest): UserResponse
POST /api/auth/refresh        → refreshToken(RefreshRequest): TokenResponse
```

#### UserController
```
GET    /api/admin/users          → getUsers(Pageable): Page<UserResponse>
GET    /api/admin/users/pending  → getPendingUsers(): List<UserResponse>
PATCH  /api/admin/users/{id}/role → updateUserRole(Long id, RoleUpdateRequest): UserResponse
```

#### AuthService
```
login(LoginRequest): TokenResponse
register(RegisterRequest): UserResponse
refreshToken(String refreshToken): TokenResponse
validateToken(String token): Authentication
```

#### UserService
```
getUsers(Pageable): Page<UserResponse>
getPendingUsers(): List<UserResponse>
updateUserRole(Long id, RoleUpdateRequest): UserResponse
getUserById(Long id): UserResponse
```

---

### Resource Domain

#### FunctionController
```
GET    /api/resources/functions            → getAll(SearchFilter): List<FunctionResponse>
POST   /api/resources/functions            → create(FunctionRequest): FunctionResponse
PUT    /api/resources/functions/{id}       → update(Long id, FunctionRequest): FunctionResponse
DELETE /api/resources/functions/{id}       → delete(Long id): void
DELETE /api/resources/functions/batch      → deleteBatch(List<Long> ids): void
PATCH  /api/resources/functions/reorder    → reorder(ReorderRequest): void
```

#### MenuController
```
GET    /api/resources/menus                → getAll(SearchFilter): List<MenuResponse>
POST   /api/resources/menus                → create(MenuRequest): MenuResponse
PUT    /api/resources/menus/{id}           → update(Long id, MenuRequest): MenuResponse
DELETE /api/resources/menus/{id}           → delete(Long id): void
DELETE /api/resources/menus/batch          → deleteBatch(List<Long> ids): void
PATCH  /api/resources/menus/reorder        → reorder(ReorderRequest): void
GET    /api/resources/menus/function-ids   → searchFunctionIds(String query): List<String>
```

#### MessageResourceController
```
GET    /api/resources/message-resources              → getAll(SearchFilter): List<MessageResourceResponse>
POST   /api/resources/message-resources              → create(MessageResourceRequest): MessageResourceResponse
PUT    /api/resources/message-resources/{id}         → update(Long id, MessageResourceRequest): MessageResourceResponse
DELETE /api/resources/message-resources/{id}         → delete(Long id): void
DELETE /api/resources/message-resources/batch        → deleteBatch(List<Long> ids): void
PATCH  /api/resources/message-resources/reorder      → reorder(ReorderRequest): void
```

#### FunctionService
```
getAll(SearchFilter): List<FunctionResponse>
create(FunctionRequest, Long userId): FunctionResponse
update(Long id, FunctionRequest, Long userId): FunctionResponse
delete(Long id, Long userId): void
deleteBatch(List<Long> ids, Long userId): void
reorder(ReorderRequest, Long userId): void
generateResourceKey(String functionId): String  // 자동생성 로직
```

#### MenuService
```
getAll(SearchFilter): List<MenuResponse>
create(MenuRequest, Long userId): MenuResponse
update(Long id, MenuRequest, Long userId): MenuResponse
delete(Long id, Long userId): void
deleteBatch(List<Long> ids, Long userId): void
reorder(ReorderRequest, Long userId): void
searchFunctionIds(String query): List<String>  // 자동완성 검색
resolveFunctionName(String functionId): String  // 기능명 자동 조회
```

#### MessageResourceService
```
getAll(SearchFilter): List<MessageResourceResponse>
create(MessageResourceRequest, Long userId): MessageResourceResponse
update(Long id, MessageResourceRequest, Long userId): MessageResourceResponse
delete(Long id, Long userId): void
deleteBatch(List<Long> ids, Long userId): void
reorder(ReorderRequest, Long userId): void
computeFullResourceKey(String module, String resourceKey): String  // 자동계산
computeDuplicateStatus(String korean, String resourceKey): String  // 중복/대문자 검증
computeFieldCounts(Long id): FieldCountResponse  // 각종 개수 계산
```

---

### Transfer Domain

#### TransferController
```
POST   /api/transfer/{resourceType}/export         → exportTsv(String resourceType, ExportRequest): ResponseEntity<byte[]>
POST   /api/transfer/{resourceType}/import/preview → importPreview(String resourceType, MultipartFile): ImportPreviewResponse
POST   /api/transfer/{resourceType}/import/apply   → importApply(String resourceType, ImportApplyRequest): ImportResultResponse
```

#### TsvExportService
```
exportAll(String resourceType): byte[]
exportSelected(String resourceType, List<Long> ids): byte[]
```

#### TsvImportService
```
preview(String resourceType, MultipartFile file): ImportPreviewResponse
apply(String resourceType, ImportApplyRequest request, Long userId): ImportResultResponse
```

#### TsvParser
```
parse(InputStream input, String resourceType): List<Map<String, String>>
validate(List<Map<String, String>> rows, String resourceType): ValidationResult
```

---

### History Domain

#### HistoryController
```
GET /api/history → getHistory(HistoryFilter, Pageable): Page<HistoryResponse>
```

#### HistoryService
```
getHistory(HistoryFilter, Pageable): Page<HistoryResponse>
recordChange(ChangeRecord): void
recordBatchChange(List<ChangeRecord>): void
```

---

### Version Domain

#### VersionController
```
GET    /api/versions                    → getVersions(String resourceType): List<VersionResponse>
POST   /api/versions                    → createTag(VersionTagRequest): VersionResponse
GET    /api/versions/{id}/diff          → getVersionDiff(Long id): VersionDiffResponse
POST   /api/versions/{id}/rollback      → rollback(Long id): RollbackResponse
```

#### VersionService
```
getVersions(String resourceType): List<VersionResponse>
createTag(VersionTagRequest, Long userId): VersionResponse
createSnapshot(String resourceType): String  // JSON 스냅샷 생성
getVersionDiff(Long id): VersionDiffResponse
rollback(Long id, Long userId): RollbackResponse
```

---

## Frontend Methods (Key Store Actions)

### useAuthStore
```
login(email, password): Promise<void>
register(name, email, password): Promise<void>
logout(): void
refreshToken(): Promise<void>
isAdmin(): boolean
hasWritePermission(): boolean
```

### useResourceStore
```
setActiveTab(tab: ResourceType): void
setSelectedRows(ids: number[]): void
clearSelection(): void
```

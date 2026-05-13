# NFR Design - DataIO Service + Deploy Service + Frontend

## 1. File Processing Design (DataIO Service)

### File Parser Strategy Pattern
```
FileProcessor (interface)
  ├── ExcelFileProcessor   → Apache POI (.xlsx)
  ├── TsvFileProcessor     → BufferedReader (.tsv)
  └── JsonFileProcessor    → Jackson ObjectMapper (.json)

Selection: FileFormat enum → FileProcessorFactory.getProcessor(format)
```

### Excel Processing (Apache POI)
```java
// Read:
// 1. Open InputStream → XSSFWorkbook
// 2. Get first sheet (index 0)
// 3. Row 0 = headers (resourceType, key, content, description)
// 4. Row 1..N = data rows → List<ImportRow>
// 5. Close workbook (try-with-resources)

// Write:
// 1. Create XSSFWorkbook + XSSFSheet
// 2. Row 0 = headers (bold style)
// 3. Row 1..N = data from resources
// 4. Auto-size columns
// 5. Write to ByteArrayOutputStream
```

### TSV Processing
```java
// Read:
// 1. BufferedReader → readLine() per row
// 2. Line 0 = headers (split by \t)
// 3. Line 1..N = data (split by \t) → List<ImportRow>
// 4. Handle: escaped tabs, empty fields, line breaks in content

// Write:
// 1. BufferedWriter
// 2. Line 0 = header fields joined by \t
// 3. Line 1..N = resource fields joined by \t
// 4. Content with tabs/newlines → escape or quote
```

### JSON Processing
```java
// Read:
// 1. ObjectMapper.readValue(inputStream, new TypeReference<List<ImportRowDto>>(){})
// 2. Map each DTO → ImportRow

// Write:
// 1. ObjectMapper.writerWithDefaultPrettyPrinter()
// 2. writeValue(outputStream, List<ResourceExportDto>)
```

### Temp File Management
```
Upload flow:
  1. MultipartFile → save to /tmp/mkfmm/import/{uuid}.{ext}
  2. Process file
  3. Delete temp file (finally block)

Export flow:
  1. Generate file → /tmp/mkfmm/export/{uuid}.{ext}
  2. Stream to HTTP response
  3. Delete temp file (finally block)
```

---

## 2. Import Pipeline Design

### Processing Pipeline
```
MultipartFile
  → Validation (size, extension, MIME)
  → Save temp file
  → FileProcessor.parse() → List<ImportRow>
  → Row Validation (required fields, enum values)
  → Duplicate Check (REST → Resource Service)
  → Conflict Resolution (SKIP / OVERWRITE)
  → Save/Update (REST → Resource Service)
  → Result Aggregation
  → Event Publish (ImportCompleted)
  → Cleanup temp file
```

### Resource Service REST Client
```java
// Base URL: http://resource-service:8084 (Docker) / localhost:8084 (local)
// WebClient with timeout 30s

// Endpoints called:
// POST /internal/resources/check-duplicate
//   Request: { key, resourceType }
//   Response: { duplicate: boolean, existingId?: string }

// POST /internal/resources
//   Request: { resourceType, key, content, description, createdBy }
//   Response: { id, ... }

// PUT /internal/resources/{id}
//   Request: { content, description, updatedBy }
//   Response: { id, ... }
```

### Internal vs External API Convention
```
/api/**        → 외부 요청 (API Gateway 경유, JWT 검증 대상)
/internal/**   → 서비스 간 내부 호출 (Docker network only, JWT 불필요)
```

---

## 3. Deploy File Storage Design

### Directory Structure
```
/data/deployments/                     (Docker named volume: deploy-data)
  ├── 20260513-143000/
  │   └── export_all_20260513_143000.xlsx
  ├── 20260513-160000/
  │   └── export_all_20260513_160000.json
  └── ...
```

### Volume Configuration (docker-compose.yml)
```yaml
volumes:
  deploy-data:
    driver: local

services:
  deploy-service:
    volumes:
      - deploy-data:/data/deployments
```

### File Lifecycle
```
1. Deploy request → DataIO exportAll 호출 → 파일 수신
2. Copy file to /data/deployments/{version}/{filename}
3. Save Deployment metadata (filePath 포함) to MongoDB
4. Redownload: Read file from filePath → HTTP response
5. Retention: 영구 보존 (수동 삭제만 가능)
```

### Disk Space Health Check
```yaml
management:
  health:
    diskspace:
      enabled: true
      path: /data/deployments
      threshold: 104857600  # 100MB minimum free space
```

---

## 4. RabbitMQ Event Design (DataIO + Deploy)

### Additional Routing Keys
```
Exchange: mkfmm.events (topic, durable) — Dev 1에서 생성됨

New routing keys:
  ├── dataio.import.completed   → Queue: history.dataio-events
  ├── dataio.export.completed   → Queue: history.dataio-events
  └── deploy.completed          → Queue: history.deploy-events
```

### Event Payloads
```json
// ImportCompleted
{
  "eventId": "uuid-v4",
  "eventType": "dataio.import.completed",
  "timestamp": "2026-05-13T14:00:00Z",
  "userId": "dev01",
  "payload": {
    "jobId": "import-job-id",
    "fileName": "resources.xlsx",
    "totalRows": 150,
    "successCount": 140,
    "failedCount": 5,
    "skippedCount": 5
  }
}

// ExportCompleted
{
  "eventId": "uuid-v4",
  "eventType": "dataio.export.completed",
  "timestamp": "2026-05-13T14:05:00Z",
  "userId": "dev01",
  "payload": {
    "format": "EXCEL",
    "exportType": "PARTIAL",
    "recordCount": 50
  }
}

// DeployCompleted
{
  "eventId": "uuid-v4",
  "eventType": "deploy.completed",
  "timestamp": "2026-05-13T14:10:00Z",
  "userId": "admin01",
  "payload": {
    "deploymentId": "deployment-id",
    "version": "20260513-141000",
    "format": "JSON",
    "totalRecords": 1200
  }
}
```

### Publisher Configuration (DataIO + Deploy)
```yaml
spring:
  rabbitmq:
    host: ${RABBITMQ_HOST:localhost}
    port: ${RABBITMQ_PORT:5672}
    username: ${RABBITMQ_USER:guest}
    password: ${RABBITMQ_PASS:guest}
    publisher-confirm-type: correlated
    publisher-returns: true
```

---

## 5. Frontend Architecture Design

### Application Structure
```
src/
├── App.tsx                    # Router + AuthProvider
├── main.tsx                   # Entry point
├── features/
│   ├── auth/
│   │   ├── AuthContext.tsx    # Context + Provider + useAuth hook
│   │   ├── LoginPage.tsx
│   │   └── ProtectedRoute.tsx
│   ├── resource/
│   │   ├── ResourceListPage.tsx
│   │   ├── ResourceDetailPage.tsx
│   │   ├── ResourceFormPage.tsx
│   │   ├── components/
│   │   │   ├── SearchBar.tsx
│   │   │   ├── ResourceTable.tsx
│   │   │   ├── SimilarityPanel.tsx
│   │   │   └── DuplicateAlert.tsx
│   │   └── hooks/
│   │       └── useResources.ts
│   ├── dataio/
│   │   ├── ImportPage.tsx
│   │   ├── ExportPage.tsx
│   │   └── components/
│   │       ├── FileUploader.tsx
│   │       ├── ImportResultReport.tsx
│   │       └── ExportFormatSelector.tsx
│   └── admin/
│       ├── UserManagementPage.tsx
│       ├── DeployPage.tsx
│       ├── HistoryPage.tsx
│       └── components/
│           ├── UserTable.tsx
│           ├── DeployHistory.tsx
│           └── WorkLogTable.tsx
├── shared/
│   ├── api/
│   │   ├── client.ts          # Axios instance + interceptors
│   │   └── endpoints.ts       # API endpoint constants
│   ├── components/
│   │   ├── Layout.tsx         # Sidebar + Main content
│   │   ├── Navigation.tsx     # Role-based menu
│   │   ├── Pagination.tsx
│   │   ├── Modal.tsx          # Headless UI Dialog wrapper
│   │   └── Toast.tsx          # 알림 컴포넌트
│   ├── hooks/
│   │   └── usePagination.ts
│   └── utils/
│       ├── formatters.ts      # 날짜, 파일 크기 포맷
│       └── validators.ts      # 클라이언트 유효성 검증
└── types/
    └── index.ts               # 공통 TypeScript 타입
```

### Axios Interceptor Design (Token Refresh)
```typescript
// Request interceptor:
// 1. If accessToken exists → add Authorization: Bearer {token}

// Response interceptor:
// 1. If response.status === 401 AND refreshToken exists:
//    a. Call POST /api/auth/refresh { refreshToken }
//    b. Success → update accessToken, retry original request
//    c. Failure → clear auth state, redirect to /login
// 2. If response.status === 401 AND no refreshToken:
//    → redirect to /login
// 3. Prevent infinite retry: flag on request config
```

### Routing & Code Splitting
```typescript
// React.lazy for route-based splitting
const ResourceListPage = lazy(() => import('./features/resource/ResourceListPage'));
const ImportPage = lazy(() => import('./features/dataio/ImportPage'));
const AdminPage = lazy(() => import('./features/admin/UserManagementPage'));

// Routes:
// /login            → LoginPage (public)
// /resources        → ResourceListPage (READ+)
// /resources/:id    → ResourceDetailPage (READ+)
// /resources/new    → ResourceFormPage (WRITE+)
// /import           → ImportPage (WRITE+)
// /export           → ExportPage (READ+)
// /admin/users      → UserManagementPage (ADMIN+)
// /admin/deploy     → DeployPage (ADMIN+)
// /admin/history    → HistoryPage (ADMIN+)
```

### Permission-Based Navigation
```typescript
// Navigation menu items filtered by role:
// READ:       리소스 검색, EXPORT
// WRITE:      READ + 리소스 등록, IMPORT
// ADMIN+:     WRITE + 사용자 관리, 배포, 이력

// ProtectedRoute component:
// - Check isAuthenticated → redirect /login if false
// - Check role >= requiredRole → show 403 page if insufficient
```

---

## 6. Docker Health Check Design (DataIO + Deploy + Frontend)

### DataIO Service Healthcheck
```dockerfile
HEALTHCHECK --interval=30s --timeout=10s --retries=3 --start-period=40s \
  CMD curl -f http://localhost:8085/actuator/health || exit 1
```

### Deploy Service Healthcheck
```dockerfile
HEALTHCHECK --interval=30s --timeout=10s --retries=3 --start-period=40s \
  CMD curl -f http://localhost:8086/actuator/health || exit 1
```

### Frontend Healthcheck (nginx)
```dockerfile
HEALTHCHECK --interval=30s --timeout=5s --retries=3 --start-period=10s \
  CMD curl -f http://localhost:3000/ || exit 1
```

### Health Indicators
| Service | Checks |
|---|---|
| DataIO Service | MongoDB connection, RabbitMQ connection, disk space |
| Deploy Service | MongoDB connection, RabbitMQ connection, disk space (/data/deployments) |
| Frontend | nginx process alive, static files accessible |

---

## 7. Error Code Design (DataIO + Deploy)

### Additional Error Codes
| Code | Description |
|---|---|
| DATAIO_001 | File too large (>5MB) |
| DATAIO_002 | Unsupported file format |
| DATAIO_003 | Empty file |
| DATAIO_004 | File parse error |
| DATAIO_005 | Import row validation error |
| DATAIO_006 | Resource service unavailable |
| DATAIO_007 | No data to export |
| DEPLOY_001 | Insufficient permissions (not admin) |
| DEPLOY_002 | Export failed during deploy |
| DEPLOY_003 | Deployment not found |
| DEPLOY_004 | Deploy file not found (redownload) |

---

## 8. Frontend Error Handling Design

### Toast Notification System
```typescript
// Toast types: success, error, warning, info
// Auto-dismiss: 5 seconds (success/info), manual dismiss (error/warning)
// Position: top-right
// Stack: max 3 visible

// Usage:
// const { showToast } = useToast();
// showToast({ type: 'error', message: '파일 크기가 5MB를 초과합니다.' });
```

### API Error Mapping
```typescript
// Axios error → user-friendly message
// 400 → server response.data.message
// 401 → auto-refresh (handled by interceptor)
// 403 → "권한이 없습니다"
// 404 → "요청한 리소스를 찾을 수 없습니다"
// 413 → "파일 크기가 제한을 초과합니다"
// 500 → "서버 오류가 발생했습니다. 잠시 후 다시 시도해주세요."
// Network Error → "서버에 연결할 수 없습니다"
```

---

## 9. PBT (Property-Based Testing) Design

### DataIO PBT Tests (jqwik)
```java
// Import count invariant
@Property
void importCountsAlwaysSum(@ForAll @IntRange(min=1, max=500) int totalRows) {
    // Given: ImportResult with any combination of success/fail/skip
    // Then: successCount + failedCount + skippedCount == totalRows
}

// File size rejection
@Property
void oversizedFilesAlwaysRejected(@ForAll @LongRange(min=5_242_881) long fileSize) {
    // Given: file with size > 5MB
    // Then: always returns 413
}

// Conflict policy SKIP preserves data
@Property
void skipPolicyNeverModifiesExisting(@ForAll List<ImportRow> rows) {
    // Given: rows with duplicates, policy=SKIP
    // Then: existing resources unchanged after import
}

// Export format consistency
@Property
void allFormatsProduceSameRecordCount(@ForAll FileFormat format) {
    // Given: same resource set
    // Then: export in any format produces same record count
}
```

### Frontend PBT Tests (fast-check)
```typescript
// Pagination bounds
fc.property(
  fc.integer({ min: 0, max: 1000 }),  // total
  fc.integer({ min: 1, max: 100 }),   // pageSize
  (total, pageSize) => {
    const totalPages = Math.ceil(total / pageSize);
    // All page numbers in [1, totalPages]
    // Items on last page <= pageSize
  }
);

// File validation
fc.property(
  fc.string(),  // random filename
  fc.integer({ min: 0, max: 10_000_000 }),  // file size
  (name, size) => {
    const valid = isValidImportFile(name, size);
    if (!name.match(/\.(xlsx|tsv|json)$/)) expect(valid).toBe(false);
    if (size > 5_242_880) expect(valid).toBe(false);
  }
);

// Role-based menu visibility
fc.property(
  fc.constantFrom('READ', 'WRITE', 'ADMIN', 'ROOT_ADMIN'),
  (role) => {
    const menuItems = getVisibleMenuItems(role);
    // READ always sees search + export
    // WRITE always sees everything READ sees + more
    // ADMIN always sees everything WRITE sees + more
  }
);
```

---

## 10. Configuration Management (DataIO + Deploy + Frontend)

### Environment Variables
| Variable | Service | Purpose |
|---|---|---|
| MONGODB_URI | DataIO, Deploy | MongoDB 연결 문자열 |
| RABBITMQ_HOST | DataIO, Deploy | RabbitMQ 호스트 |
| RABBITMQ_PORT | DataIO, Deploy | RabbitMQ 포트 |
| RABBITMQ_USER | DataIO, Deploy | RabbitMQ 사용자 |
| RABBITMQ_PASS | DataIO, Deploy | RabbitMQ 비밀번호 |
| RESOURCE_SERVICE_URL | DataIO | Resource Service 내부 URL |
| DATAIO_SERVICE_URL | Deploy | DataIO Service 내부 URL |
| DEPLOY_FILE_PATH | Deploy | 배포 파일 저장 경로 |
| VITE_API_BASE_URL | Frontend (build-time) | API Gateway URL |

### Spring Profiles
```yaml
# DataIO Service - application.yml
spring:
  profiles:
    active: ${SPRING_PROFILES_ACTIVE:default}

# default profile: localhost connections
# docker profile: service-name connections (Docker network)
```

### Nginx Configuration (Frontend)
```nginx
server {
    listen 3000;
    root /usr/share/nginx/html;
    index index.html;

    # SPA fallback
    location / {
        try_files $uri $uri/ /index.html;
    }

    # API proxy (개발 시에만, 프로덕션은 직접 Gateway 호출)
    # location /api/ {
    #     proxy_pass http://api-gateway:8080;
    # }

    # gzip
    gzip on;
    gzip_types text/plain text/css application/json application/javascript text/xml;
}
```

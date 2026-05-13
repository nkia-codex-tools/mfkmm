# Business Logic Model - DataIO Service + Deploy Service + Frontend

## 1. Import Flow

```
Input: MultipartFile file, ImportOptions { conflictPolicy, fileFormat }

1. Validate file
   - size > 5MB → return 413 (FILE_TOO_LARGE)
   - size == 0 → return 400 (EMPTY_FILE)
   - extension not in [xlsx, tsv, json] → return 400 (UNSUPPORTED_FORMAT)
2. Create ImportJob { status=PROCESSING, fileName, fileFormat, userId }
3. Parse file → List<ImportRow>
   - Parse error → set status=FAILED, return error
4. For each ImportRow:
   a. Validate row (resourceType, key, content 필수)
      - Invalid → add to errors, increment failedCount, continue
   b. Call Resource Service: checkDuplicate(key, resourceType)
      - Duplicate found:
        - SKIP → increment skippedCount, continue
        - OVERWRITE → call Resource Service: updateResource(...)
          - Success → increment successCount
          - Failure → add to errors, increment failedCount
      - No duplicate:
        - Call Resource Service: createResource(...)
          - Success → increment successCount
          - Failure → add to errors, increment failedCount
5. Update ImportJob { status=COMPLETED, totalRows, successCount, failedCount, skippedCount, completedAt }
6. Publish ImportCompleted event { jobId, userId, totalRows, successCount, failedCount, skippedCount }
7. Return ImportResult { jobId, totalRows, successCount, failedCount, skippedCount, errors }
```

## 2. Partial Export Flow

```
Input: ExportRequest { format, searchQuery }

1. Call Resource Service: search(searchQuery)
   - Empty result → return 204 (NO_CONTENT)
2. Convert results to target format:
   - EXCEL → generate .xlsx with headers
   - TSV → generate .tsv with headers
   - JSON → serialize to JSON array
3. Generate fileName: export_partial_{yyyyMMdd_HHmmss}.{ext}
4. Publish ExportCompleted event { userId, format, recordCount, exportType=PARTIAL }
5. Return file as HTTP download response
```

## 3. Full Export Flow

```
Input: ExportRequest { format }

1. Call Resource Service: findAll(active=true)
   - Empty result → return 204 (NO_CONTENT)
2. Convert all results to target format
3. Generate fileName: export_all_{yyyyMMdd_HHmmss}.{ext}
4. Publish ExportCompleted event { userId, format, recordCount, exportType=ALL }
5. Return file as HTTP download response
```

## 4. Deploy Flow

```
Input: DeployRequest { format, userId }

1. Verify role: userId must have ADMIN or ROOT_ADMIN
   - Insufficient → return 403
2. Call DataIO Service: exportAll(format)
   - Failure → return 500 (EXPORT_FAILED)
3. Save file permanently to /data/deployments/{version}/{filename}
4. Create Deployment {
     version: yyyyMMdd-HHmmss,
     format, totalRecords, fileSize, filePath, userId, createdAt
   }
5. Save Deployment to DB
6. Publish DeployCompleted event { deploymentId, version, userId, totalRecords }
7. Return Deployment metadata + file download
```

## 5. Deploy History Flow

```
Input: DeployFilter { page, size, startDate?, endDate? }

1. Verify role: ADMIN or ROOT_ADMIN
   - Insufficient → return 403
2. Query DeploymentRepository with filter (sorted by createdAt DESC)
3. Return Page<Deployment>
```

## 6. Redownload Flow

```
Input: deploymentId

1. Verify role: ADMIN or ROOT_ADMIN
   - Insufficient → return 403
2. Find Deployment by ID
   - NOT FOUND → return 404
3. Read file from filePath
   - File missing → return 404 (FILE_NOT_FOUND)
4. Return file as HTTP download response
```

## 7. Frontend - Login Flow

```
1. User enters userId + password
2. Call POST /api/auth/login { userId, password }
   - 401 → show "아이디 또는 비밀번호가 올바르지 않습니다"
   - 423 → show "계정이 잠겼습니다. 관리자에게 문의하세요"
   - 200 → store { accessToken, refreshToken, user } in AuthContext
3. Redirect to main page (Resource Search)
```

## 8. Frontend - Token Refresh Flow

```
Trigger: API call returns 401

1. Check refreshToken exists
   - NOT EXISTS → redirect to login
2. Call POST /api/auth/refresh { refreshToken }
   - 401 → clear AuthContext, redirect to login
   - 200 → update accessToken in AuthContext
3. Retry original API call with new accessToken
```

## 9. Frontend - Import Flow

```
1. User selects file + conflict policy
2. Client-side validation:
   - Extension check (.xlsx, .tsv, .json)
   - Size check (≤ 5MB)
   - Fail → show inline error, block upload
3. Call POST /api/dataio/import (multipart) { file, conflictPolicy }
4. Show loading spinner
5. Response received:
   - Success → show ImportResultReport (success/fail/skip counts + error details)
   - Error → show Toast with error message
```

## 10. Frontend - Export Flow

```
1. User selects format (EXCEL/TSV/JSON) + type (PARTIAL/ALL)
   - PARTIAL: use current search query
   - ALL: no query needed
2. Call POST /api/dataio/export/partial or POST /api/dataio/export/all
3. Receive file blob → trigger browser download
4. Show success Toast
```

## 11. Frontend - Deploy Flow

```
1. Admin selects format
2. Confirmation dialog: "전체 데이터를 배포하시겠습니까?"
3. Call POST /api/deploy { format }
4. Receive file + show success Toast
5. Deploy history table refreshes automatically
```

## 12. Frontend - Resource CRUD Flow

```
[Search]
1. User enters keyword / selects filter
2. Call GET /api/resources/search?q=...&type=...&page=...&size=...
3. Display results in ResourceTable with pagination

[Create]
1. User fills ResourceForm (type, key, content, description)
2. On submit: Call POST /api/resources/check-duplicate { key, resourceType }
   - Duplicate → show DuplicateAlert, block save
3. Call POST /api/resources/find-similar { key, content }
   - Similar found → show SimilarityPanel (candidates + scores)
   - User chooses: use existing OR create new
4. Call POST /api/resources { ... }
5. Success → navigate to detail page

[Update]
1. Load resource detail → pre-fill form
2. User edits fields
3. Call PUT /api/resources/{id} { ... }
4. Success → show Toast, refresh detail

[Delete]
1. Confirmation dialog: "삭제하시겠습니까? (30일 후 영구 삭제됩니다)"
2. Call DELETE /api/resources/{id}
3. Success → navigate to list, show Toast
```

---

## Testable Properties (PBT-01)

### Round-Trip Properties
| Property | Description | Category |
|---|---|---|
| Export/Import round-trip | 전체 EXPORT 후 IMPORT하면 동일한 리소스 수 복원 | Round-trip |
| File format consistency | Excel/TSV/JSON 중 어떤 형식으로 EXPORT해도 동일한 레코드 수 | Invariant |
| Parse/Serialize round-trip | JSON serialize → parse 하면 원본 데이터 복원 | Round-trip |

### Invariant Properties
| Property | Description | Category |
|---|---|---|
| Import count invariant | successCount + failedCount + skippedCount == totalRows | Invariant |
| File size limit | 5MB 초과 파일은 항상 거부 | Invariant |
| Deploy requires admin | ADMIN/ROOT_ADMIN 아닌 사용자는 항상 403 | Invariant |
| Active only export | EXPORT에 soft-deleted 리소스 포함 안됨 | Invariant |

### Idempotence Properties
| Property | Description | Category |
|---|---|---|
| Export idempotent | 동일 조건으로 EXPORT 시 동일 건수 (데이터 변경 없을 경우) | Idempotence |
| Duplicate skip idempotent | SKIP 정책으로 같은 파일 2회 IMPORT 시 2회차 모두 skip | Idempotence |

### Business Rule Invariants
| Property | Description | Category |
|---|---|---|
| Conflict policy honored | SKIP이면 기존 데이터 변경 없음, OVERWRITE면 최신값 반영 | Invariant |
| Deploy version unique | 동시 배포가 아닌 한 version은 항상 유니크 | Invariant |
| Redownload returns same file | 같은 deploymentId로 재다운로드하면 동일 파일 반환 | Invariant |

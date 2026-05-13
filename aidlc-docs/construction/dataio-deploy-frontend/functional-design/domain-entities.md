# Domain Entities - DataIO Service + Deploy Service + Frontend

## DataIO Service Entities

### ImportJob
| Field | Type | Description |
|---|---|---|
| id | String (ObjectId) | 고유 식별자 |
| fileName | String | 업로드된 파일명 |
| fileFormat | FileFormat (enum) | EXCEL, TSV, JSON |
| fileSize | Long | 파일 크기 (bytes) |
| status | ImportStatus (enum) | PENDING, PROCESSING, COMPLETED, FAILED |
| conflictPolicy | ConflictPolicy (enum) | SKIP, OVERWRITE, ASK |
| totalRows | Integer | 전체 행 수 |
| successCount | Integer | 성공 건수 |
| failedCount | Integer | 실패 건수 |
| skippedCount | Integer | 건너뛴 건수 |
| errors | List\<ImportError\> | 행별 오류 목록 |
| userId | String | 요청자 ID |
| createdAt | Instant | 생성 시각 |
| completedAt | Instant | 완료 시각 |

### ImportError (Value Object)
| Field | Type | Description |
|---|---|---|
| rowNumber | Integer | 오류 발생 행 번호 |
| field | String | 오류 필드명 |
| value | String | 입력된 값 |
| reason | String | 오류 사유 (DUPLICATE, INVALID_FORMAT, MISSING_REQUIRED) |

### ImportRow (Value Object - 파싱 후 검증 전 중간 객체)
| Field | Type | Description |
|---|---|---|
| rowNumber | Integer | 행 번호 |
| resourceType | ResourceType | MESSAGE_KEY, FUNCTION_ID, MENU_ID |
| key | String | 리소스 키/ID |
| content | String | 내용 |
| description | String | 설명/비고 (optional) |

### ExportRequest (Value Object)
| Field | Type | Description |
|---|---|---|
| exportType | ExportType (enum) | PARTIAL, ALL |
| format | FileFormat (enum) | EXCEL, TSV, JSON |
| searchQuery | ResourceSearchQuery | 부분 EXPORT 시 검색 조건 (nullable) |
| userId | String | 요청자 ID |

### ExportResult (Value Object)
| Field | Type | Description |
|---|---|---|
| fileName | String | 생성된 파일명 |
| format | FileFormat | 파일 형식 |
| fileSize | Long | 파일 크기 (bytes) |
| totalRecords | Integer | 내보낸 건수 |
| filePath | String | 임시 저장 경로 |
| createdAt | Instant | 생성 시각 |

---

## Deploy Service Entities

### Deployment
| Field | Type | Description |
|---|---|---|
| id | String (ObjectId) | 고유 식별자 |
| version | String | 배포 버전 (자동 생성: yyyyMMdd-HHmmss) |
| format | FileFormat (enum) | EXCEL, TSV, JSON |
| totalRecords | Integer | 배포된 리소스 건수 |
| fileSize | Long | 배포 파일 크기 (bytes) |
| filePath | String | 배포 파일 저장 경로 |
| userId | String | 배포 수행자 ID |
| createdAt | Instant | 배포 시각 |

---

## Enumerations

### FileFormat
```
EXCEL   — .xlsx (Apache POI)
TSV     — .tsv (Tab-Separated Values)
JSON    — .json (배열 형태)
```

### ImportStatus
```
PENDING     — 작업 대기 중
PROCESSING  — 처리 중
COMPLETED   — 완료
FAILED      — 실패 (파싱 오류 등)
```

### ConflictPolicy
```
SKIP       — 중복 발견 시 건너뛰기
OVERWRITE  — 중복 발견 시 덮어쓰기
ASK        — 중복 결과 반환 후 사용자 확인 대기 (동기 모드에서만)
```

### ExportType
```
PARTIAL  — 검색 결과 또는 선택 데이터
ALL      — 전체 리소스
```

### ResourceType (Shared)
```
MESSAGE_KEY   — 메시지 리소스 키
FUNCTION_ID   — 기능 ID
MENU_ID       — 메뉴 ID
```

---

## Frontend State Models

### AuthState (Context)
| Field | Type | Description |
|---|---|---|
| user | UserInfo \| null | 현재 로그인 사용자 |
| accessToken | String \| null | JWT Access Token |
| refreshToken | String \| null | JWT Refresh Token |
| isAuthenticated | Boolean | 인증 여부 |

### ResourceSearchState
| Field | Type | Description |
|---|---|---|
| query | String | 검색 키워드 |
| resourceType | ResourceType \| null | 유형 필터 |
| page | Integer | 현재 페이지 |
| size | Integer | 페이지 크기 |
| sortField | String | 정렬 필드 |
| sortDirection | 'asc' \| 'desc' | 정렬 방향 |

### ImportState
| Field | Type | Description |
|---|---|---|
| file | File \| null | 업로드할 파일 |
| conflictPolicy | ConflictPolicy | 충돌 정책 |
| status | 'idle' \| 'uploading' \| 'processing' \| 'done' \| 'error' | 진행 상태 |
| result | ImportResult \| null | IMPORT 결과 |

### ExportState
| Field | Type | Description |
|---|---|---|
| format | FileFormat | 내보내기 형식 |
| exportType | ExportType | 부분/전체 |
| status | 'idle' \| 'generating' \| 'done' \| 'error' | 진행 상태 |

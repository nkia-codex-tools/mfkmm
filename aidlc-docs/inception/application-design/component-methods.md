# MKFMM Component Methods

## Backend - Inbound Ports (Use Cases)

### AuthUseCase
| Method | Input | Output | Purpose |
|---|---|---|---|
| `login(credentials)` | LoginRequest(userId, password) | LoginResponse(sessionId, user, role) | 로그인 인증 |
| `logout(sessionId)` | String sessionId | void | 세션 무효화 |
| `validateSession(sessionId)` | String sessionId | SessionInfo | 세션 유효성 확인 |
| `getLoginHistory(filter)` | LoginHistoryFilter | Page<LoginHistory> | 로그인 이력 조회 |
| `unlockAccount(userId)` | String userId | void | 계정 잠금 해제 (Admin) |

### UserManagementUseCase
| Method | Input | Output | Purpose |
|---|---|---|---|
| `createUser(request)` | CreateUserRequest | User | 사용자 등록 |
| `updateUser(id, request)` | String id, UpdateUserRequest | User | 사용자 정보 변경 |
| `deleteUser(id)` | String id | void | 사용자 삭제 |
| `getUsers(filter)` | UserFilter | Page<User> | 사용자 목록 조회 |
| `getUserById(id)` | String id | User | 사용자 상세 조회 |

### PermissionUseCase
| Method | Input | Output | Purpose |
|---|---|---|---|
| `grantPermission(userId, role)` | String userId, Role role | void | 권한 부여 |
| `revokePermission(userId)` | String userId | void | 권한 회수 |
| `getAuditLogs(filter)` | AuditLogFilter | Page<AuditLog> | 감사 로그 조회 |

### ResourceUseCase
| Method | Input | Output | Purpose |
|---|---|---|---|
| `createResource(request)` | CreateResourceRequest | Resource | 리소스 등록 |
| `updateResource(id, request)` | String id, UpdateResourceRequest | Resource | 리소스 수정 |
| `deleteResource(id)` | String id | void | 리소스 Soft Delete |
| `getResourceById(id)` | String id | Resource | 리소스 상세 조회 |

### ResourceSearchUseCase
| Method | Input | Output | Purpose |
|---|---|---|---|
| `search(query)` | ResourceSearchQuery | Page<Resource> | 리소스 검색 |

### SimilarityUseCase
| Method | Input | Output | Purpose |
|---|---|---|---|
| `checkDuplicate(request)` | DuplicateCheckRequest | DuplicateCheckResult | 중복 검사 (exact match) |
| `findSimilar(request)` | SimilarityRequest | List<SimilarityResult> | 유사 정보 탐지 |
| `confirmSimilarityChoice(choice)` | SimilarityChoice | void | 유사 제안 선택 결과 기록 |

### ImportUseCase
| Method | Input | Output | Purpose |
|---|---|---|---|
| `importFile(file, options)` | MultipartFile, ImportOptions | ImportResult | 파일 IMPORT |
| `getImportStatus(jobId)` | String jobId | ImportStatus | 비동기 IMPORT 상태 조회 |

### ExportUseCase
| Method | Input | Output | Purpose |
|---|---|---|---|
| `exportPartial(query, format)` | ResourceSearchQuery, ExportFormat | ExportResult | 부분 EXPORT |
| `exportAll(format)` | ExportFormat | ExportResult | 전체 EXPORT |

### DeployUseCase
| Method | Input | Output | Purpose |
|---|---|---|---|
| `deploy(format)` | ExportFormat | Deployment | 전체 데이터 배포 |
| `getDeployHistory(filter)` | DeployFilter | Page<Deployment> | 배포 이력 조회 |
| `redownload(deploymentId)` | String deploymentId | ExportResult | 배포 파일 재다운로드 |

### HistoryUseCase
| Method | Input | Output | Purpose |
|---|---|---|---|
| `recordWork(log)` | WorkLogEntry | void | 작업 이력 기록 |
| `getWorkLogs(filter)` | WorkLogFilter | Page<WorkLog> | 작업 이력 조회 |
| `getMyWorkLogs(userId, filter)` | String userId, WorkLogFilter | Page<WorkLog> | 본인 이력 조회 |

---

## Backend - Outbound Ports (Repositories)

### SessionRepository
| Method | Purpose |
|---|---|
| `save(session)` | 세션 저장 |
| `findById(sessionId)` | 세션 조회 |
| `deleteById(sessionId)` | 세션 삭제 |
| `deleteExpired()` | 만료 세션 정리 |

### UserRepository
| Method | Purpose |
|---|---|
| `save(user)` | 사용자 저장 |
| `findById(id)` | ID로 조회 |
| `findByUserId(userId)` | 사용자 ID로 조회 |
| `findAll(filter, pageable)` | 목록 조회 |
| `deleteById(id)` | 삭제 |
| `existsByUserId(userId)` | 중복 ID 확인 |

### ResourceRepository
| Method | Purpose |
|---|---|
| `save(resource)` | 리소스 저장 |
| `findById(id)` | ID로 조회 |
| `findByKeyOrId(key)` | 키/ID로 정확 조회 (중복 검사용) |
| `search(query, pageable)` | 검색 (키워드, 유형, 메타데이터) |
| `findSimilarByContent(content, threshold)` | 유사 내용 조회 |
| `softDelete(id)` | Soft Delete |
| `hardDeleteExpired(days)` | 30일 경과 Hard Delete |

### WorkLogRepository
| Method | Purpose |
|---|---|
| `save(log)` | 이력 저장 |
| `findAll(filter, pageable)` | 이력 조회 |
| `findByUserId(userId, filter, pageable)` | 사용자별 이력 |
| `deleteSearchLogsOlderThan(months)` | 6개월 경과 검색 이력 삭제 |

### DeploymentRepository
| Method | Purpose |
|---|---|
| `save(deployment)` | 배포 저장 |
| `findAll(filter, pageable)` | 배포 이력 조회 |
| `findById(id)` | 배포 상세 조회 |

---

## Frontend - Key Hooks & API Functions

### Auth Hooks
| Hook/Function | Purpose |
|---|---|
| `useAuth()` | AuthContext 접근 (user, role, login, logout) |
| `useLoginMutation()` | 로그인 API 호출 |
| `useLogoutMutation()` | 로그아웃 API 호출 |

### Resource Hooks
| Hook/Function | Purpose |
|---|---|
| `useResourceSearch(query)` | 리소스 검색 |
| `useResourceDetail(id)` | 리소스 상세 조회 |
| `useCreateResource()` | 리소스 등록 |
| `useUpdateResource()` | 리소스 수정 |
| `useDeleteResource()` | 리소스 삭제 |
| `useSimilarityCheck(input)` | 유사도 검사 요청 |

### DataIO Hooks
| Hook/Function | Purpose |
|---|---|
| `useImportFile()` | 파일 IMPORT |
| `useExportData(format)` | 데이터 EXPORT |

### Admin Hooks
| Hook/Function | Purpose |
|---|---|
| `useUsers(filter)` | 사용자 목록 |
| `useCreateUser()` | 사용자 등록 |
| `usePermissionUpdate()` | 권한 변경 |
| `useDeployHistory()` | 배포 이력 |
| `useWorkLogs(filter)` | 작업 이력 |

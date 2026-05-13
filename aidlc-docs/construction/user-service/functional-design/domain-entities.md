# Domain Entities - User Service

## User
| Field | Type | Description |
|---|---|---|
| id | String (ObjectId) | 고유 식별자 |
| userId | String | 사용자 로그인 ID (unique) |
| name | String | 사용자 이름 |
| email | String | 이메일 주소 |
| department | String | 부서명 |
| role | Role (enum) | ROOT_ADMIN, ADMIN, WRITE, READ |
| memo | String | 메모/비고 (nullable) |
| isRootAdmin | Boolean | 시스템 최상위 관리자 여부 |
| isLocked | Boolean | 계정 잠금 여부 |
| mustChangePassword | Boolean | 첫 로그인 시 비밀번호 변경 필요 여부 |
| createdAt | Instant | 생성 시각 |
| createdBy | String | 등록한 관리자 ID |
| updatedAt | Instant | 최종 수정 시각 |
| updatedBy | String | 최종 수정한 사용자 ID |

---

## AuditLog
| Field | Type | Description |
|---|---|---|
| id | String (ObjectId) | 고유 식별자 |
| action | AuditAction (enum) | 수행된 작업 유형 |
| targetUserId | String | 대상 사용자 ID |
| performedBy | String | 수행한 관리자 ID |
| previousValue | String | 변경 전 값 (JSON) |
| newValue | String | 변경 후 값 (JSON) |
| reason | String | 변경 사유 (nullable) |
| performedAt | Instant | 수행 시각 |

---

## Enumerations

### Role
```
ROOT_ADMIN  — 시스템 최상위 관리자 (변경/삭제 불가)
ADMIN       — 관리자 (모든 권한 + 사용자 관리)
WRITE       — 리소스 CRUD + IMPORT/EXPORT
READ        — 조회 + EXPORT만 가능
```

### AuditAction
```
USER_CREATED        — 사용자 생성
USER_UPDATED        — 사용자 정보 수정
USER_DELETED        — 사용자 삭제
PERMISSION_GRANTED  — 권한 부여
PERMISSION_REVOKED  — 권한 회수
ACCOUNT_UNLOCKED    — 계정 잠금 해제
PASSWORD_RESET      — 비밀번호 초기화 (관리자)
```

---

## Events (Published to RabbitMQ)

### UserCreated
| Field | Type | Description |
|---|---|---|
| eventId | String (UUID) | 이벤트 고유 ID |
| eventType | String | "UserCreated" |
| timestamp | Instant | 이벤트 시각 |
| userId | String | 생성된 사용자 ID |
| name | String | 사용자 이름 |
| role | String | 부여된 권한 |
| createdBy | String | 등록한 관리자 ID |

### UserDeleted
| Field | Type | Description |
|---|---|---|
| eventId | String (UUID) | 이벤트 고유 ID |
| eventType | String | "UserDeleted" |
| timestamp | Instant | 이벤트 시각 |
| userId | String | 삭제된 사용자 ID |
| deletedBy | String | 삭제한 관리자 ID |

### PermissionChanged
| Field | Type | Description |
|---|---|---|
| eventId | String (UUID) | 이벤트 고유 ID |
| eventType | String | "PermissionChanged" |
| timestamp | Instant | 이벤트 시각 |
| targetUserId | String | 대상 사용자 ID |
| previousRole | String | 이전 권한 |
| newRole | String | 새 권한 |
| changedBy | String | 변경한 관리자 ID |

---

## Request/Response DTOs

### CreateUserRequest
| Field | Type | Required | Description |
|---|---|---|---|
| userId | String | Yes | 로그인 ID |
| name | String | Yes | 이름 |
| email | String | Yes | 이메일 |
| department | String | No | 부서 |
| role | Role | Yes | 부여할 권한 |
| password | String | Yes | 초기 비밀번호 (관리자 지정) |
| memo | String | No | 메모 |

### UpdateUserRequest
| Field | Type | Required | Description |
|---|---|---|---|
| name | String | No | 이름 |
| email | String | No | 이메일 |
| department | String | No | 부서 |
| memo | String | No | 메모 |

### UserResponse
| Field | Type | Description |
|---|---|---|
| id | String | 고유 식별자 |
| userId | String | 로그인 ID |
| name | String | 이름 |
| email | String | 이메일 |
| department | String | 부서 |
| role | Role | 권한 |
| memo | String | 메모 |
| isLocked | Boolean | 잠금 상태 |
| mustChangePassword | Boolean | 비밀번호 변경 필요 |
| createdAt | Instant | 생성일 |
| createdBy | String | 등록자 |
| updatedAt | Instant | 수정일 |

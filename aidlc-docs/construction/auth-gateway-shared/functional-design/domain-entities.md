# Domain Entities - Auth Service + API Gateway + Shared

## Auth Service Entities

### User (Authentication View)
| Field | Type | Description |
|---|---|---|
| id | String (ObjectId) | 고유 식별자 |
| userId | String | 사용자 로그인 ID (unique) |
| passwordHash | String | bcrypt 해싱된 비밀번호 |
| role | Role (enum) | ROOT_ADMIN, ADMIN, WRITE, READ |
| isLocked | Boolean | 계정 잠금 여부 |
| failedLoginAttempts | Integer | 연속 로그인 실패 횟수 |
| isRootAdmin | Boolean | 시스템 최상위 관리자 여부 (변경/삭제 불가) |
| createdAt | Instant | 생성 시각 |
| updatedAt | Instant | 최종 수정 시각 |

### RefreshToken
| Field | Type | Description |
|---|---|---|
| id | String (ObjectId) | 고유 식별자 |
| userId | String | 사용자 ID (FK) |
| token | String | Refresh Token 값 (UUID) |
| expiresAt | Instant | 만료 시각 (발급 + 7일) |
| createdAt | Instant | 발급 시각 |
| revoked | Boolean | 무효화 여부 |

### LoginHistory
| Field | Type | Description |
|---|---|---|
| id | String (ObjectId) | 고유 식별자 |
| userId | String | 시도한 사용자 ID |
| loginAt | Instant | 로그인 시각 |
| ipAddress | String | 접속 IP |
| success | Boolean | 성공/실패 여부 |
| failureReason | String | 실패 사유 (null if success) |

### AccountLockEvent
| Field | Type | Description |
|---|---|---|
| id | String (ObjectId) | 고유 식별자 |
| userId | String | 잠긴 사용자 ID |
| lockedAt | Instant | 잠금 시각 |
| unlockedAt | Instant | 해제 시각 (null if still locked) |
| unlockedBy | String | 해제한 관리자 ID |

---

## JWT Claims (Value Object)

### AccessToken Claims
| Claim | Type | Description |
|---|---|---|
| sub | String | 사용자 ID |
| role | String | 권한 (ROOT_ADMIN/ADMIN/WRITE/READ) |
| iat | Long | 발급 시각 (epoch seconds) |
| exp | Long | 만료 시각 (iat + 15분) |
| jti | String | 토큰 고유 ID (UUID) |

### RefreshToken Claims
| Claim | Type | Description |
|---|---|---|
| sub | String | 사용자 ID |
| iat | Long | 발급 시각 |
| exp | Long | 만료 시각 (iat + 7일) |
| jti | String | 토큰 고유 ID (UUID) |
| type | String | "refresh" |

---

## Shared Library Entities

### Event Base
| Field | Type | Description |
|---|---|---|
| eventId | String (UUID) | 이벤트 고유 ID |
| eventType | String | 이벤트 유형 |
| timestamp | Instant | 이벤트 발생 시각 |
| userId | String | 이벤트 발생 주체 |
| payload | Object | 이벤트별 상세 데이터 |

### Auth Events
| Event | Payload |
|---|---|
| UserLoggedIn | { userId, ipAddress, loginAt } |
| UserLoggedOut | { userId, logoutAt } |
| AccountLocked | { userId, lockedAt, failedAttempts } |
| AccountUnlocked | { userId, unlockedAt, unlockedBy } |
| PasswordChanged | { userId, changedBy, changedAt } |

### Shared DTOs
| DTO | Fields | Used By |
|---|---|---|
| UserInfo | id, userId, role, isLocked | API Gateway (JWT 검증 후 전달) |
| AuthResponse | accessToken, refreshToken, expiresIn, user | Auth Service → Frontend |
| ErrorResponse | code, message, timestamp, details | All services |

---

## Enumerations

### Role
```
ROOT_ADMIN  — 시스템 최상위 관리자 (변경/삭제 불가)
ADMIN       — 관리자 (모든 권한 + 사용자 관리)
WRITE       — 리소스 CRUD + IMPORT/EXPORT
READ        — 조회 + EXPORT만 가능
```

### LoginFailureReason
```
INVALID_PASSWORD    — 비밀번호 불일치
ACCOUNT_LOCKED      — 계정 잠김
USER_NOT_FOUND      — 사용자 없음
```

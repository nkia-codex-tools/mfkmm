# Domain Entities - Unit 1: Auth

## Entity: User

| Field | Type | Constraints | Description |
|-------|------|------------|-------------|
| id | Long | PK, auto-increment | 고유 식별자 |
| email | String | UNIQUE, NOT NULL, max 255 | 로그인 이메일 |
| password_hash | String | NOT NULL | BCrypt 해시된 비밀번호 |
| name | String | NOT NULL, max 100 | 사용자 이름 |
| role | Enum(Role) | NOT NULL, default PENDING | 사용자 역할 |
| status | Enum(Status) | NOT NULL, default PENDING | 계정 상태 |
| failed_login_attempts | Integer | NOT NULL, default 0 | 연속 로그인 실패 횟수 |
| locked_until | Timestamp | NULLABLE | 계정 잠금 해제 시각 |
| created_at | Timestamp | NOT NULL | 생성 시각 |
| updated_at | Timestamp | NOT NULL | 최종 수정 시각 |

## Enum: Role

| Value | Description | Permissions |
|-------|-------------|-------------|
| ADMIN | 관리자 | 모든 리소스 읽기/쓰기, 사용자 관리, 버전 태깅/롤백 |
| WRITER | 쓰기 권한 사용자 | 리소스 읽기/쓰기, import/export |
| READER | 읽기 권한 사용자 | 리소스 읽기, export만 가능 |
| PENDING | 승인 대기 사용자 | 로그인 가능하나 리소스 접근 불가 |

## Enum: Status

| Value | Description |
|-------|-------------|
| PENDING | 가입 완료, 관리자 승인 대기 |
| ACTIVE | 활성 (권한 부여됨) |
| LOCKED | 잠금 (로그인 실패 초과) |

## Entity Relationships

```
User (1) ----> Role (enum, embedded)
User (1) ----> Status (enum, embedded)
```

## Token Model (Non-persistent)

### Access Token (JWT Claims)

| Claim | Type | Description |
|-------|------|-------------|
| sub | String | 사용자 ID |
| email | String | 사용자 이메일 |
| role | String | 사용자 역할 |
| iat | Long | 발급 시각 |
| exp | Long | 만료 시각 (15분) |

### Refresh Token (JWT Claims)

| Claim | Type | Description |
|-------|------|-------------|
| sub | String | 사용자 ID |
| type | String | "refresh" |
| iat | Long | 발급 시각 |
| exp | Long | 만료 시각 (7일) |

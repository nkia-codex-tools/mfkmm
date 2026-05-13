# Business Logic Model - Auth Service + API Gateway + Shared

## 1. Login Flow

```
Input: LoginRequest { userId, password, ipAddress }

1. Find user by userId
   - NOT FOUND → record failure (USER_NOT_FOUND) → return error
2. Check isLocked
   - LOCKED → record failure (ACCOUNT_LOCKED) → return error
3. Verify password (bcrypt compare)
   - MISMATCH → increment failedLoginAttempts
     - IF failedLoginAttempts >= 3 → lock account (BR-LOCK-01)
     - record failure (INVALID_PASSWORD) → return error
4. SUCCESS:
   - Reset failedLoginAttempts = 0
   - Generate Access Token (15min)
   - Generate Refresh Token (7 days) → save to DB
   - Record LoginHistory (success=true)
   - Publish UserLoggedIn event
   - Return AuthResponse { accessToken, refreshToken, expiresIn, user }
```

## 2. Logout Flow

```
Input: LogoutRequest { userId, refreshToken }

1. Find RefreshToken by token value
   - NOT FOUND → return success (idempotent)
2. Set revoked = true → save
3. Record LoginHistory (logout)
4. Publish UserLoggedOut event
5. Return success
```

## 3. Token Refresh Flow

```
Input: RefreshRequest { refreshToken }

1. Parse refresh token → extract jti
2. Find RefreshToken in DB by jti
   - NOT FOUND → return 401
3. Check revoked
   - REVOKED → return 401
4. Check expiresAt
   - EXPIRED → return 401
5. Extract userId from token
6. Find user → verify exists and not locked
   - LOCKED or NOT FOUND → revoke token → return 401
7. Generate new Access Token (15min)
8. Return { accessToken, expiresIn }
```

## 4. Account Lock Flow

```
Trigger: failedLoginAttempts >= 3

1. Set user.isLocked = true → save
2. Create AccountLockEvent { userId, lockedAt=now }
3. Publish AccountLocked event
```

## 5. Account Unlock Flow

```
Input: UnlockRequest { targetUserId, adminUserId }

1. Verify admin has ADMIN or ROOT_ADMIN role
   - INSUFFICIENT → return 403
2. Find target user
   - NOT FOUND → return 404
3. Check isLocked
   - NOT LOCKED → return success (idempotent)
4. Set user.isLocked = false → save
   (NOTE: failedLoginAttempts 유지 — 첫 성공 로그인 시 초기화)
5. Update AccountLockEvent { unlockedAt=now, unlockedBy=adminUserId }
6. Publish AccountUnlocked event
7. Return success
```

## 6. Password Change Flow (Self)

```
Input: PasswordChangeRequest { userId, currentPassword, newPassword }

1. Find user by userId
2. Verify currentPassword against stored hash (bcrypt)
   - MISMATCH → return error
3. Hash newPassword with bcrypt (cost=10)
4. Update user.passwordHash → save
5. Revoke all RefreshTokens for this user
6. Publish PasswordChanged event
7. Return success (user must re-login)
```

## 7. Password Reset Flow (Admin)

```
Input: PasswordResetRequest { targetUserId, newPassword, adminUserId }

1. Verify admin has ADMIN or ROOT_ADMIN role
2. Find target user
   - NOT FOUND → return 404
3. Check: target is ROOT_ADMIN?
   - YES → return error (ROOT_ADMIN 비밀번호는 본인만 변경 가능)
4. Hash newPassword with bcrypt (cost=10)
5. Update target.passwordHash → save
6. Revoke all RefreshTokens for target user
7. Publish PasswordChanged event
8. Return success
```

## 8. Root Admin Initialization Flow

```
Trigger: Application startup

1. Check if ROOT_ADMIN user exists in DB
   - EXISTS → skip (no-op)
2. Read env vars: MKFMM_ROOT_USER, MKFMM_ROOT_PASSWORD
   - MISSING → throw startup error (application cannot start)
3. Hash MKFMM_ROOT_PASSWORD with bcrypt (cost=10)
4. Create User {
     userId: MKFMM_ROOT_USER,
     passwordHash: hashed,
     role: ROOT_ADMIN,
     isRootAdmin: true,
     isLocked: false,
     failedLoginAttempts: 0
   }
5. Save to DB
6. Log: "Root administrator initialized"
```

## 9. API Gateway Request Flow

```
Input: HTTP Request from Frontend

1. Check if path is public (/api/auth/login, /api/auth/refresh)
   - PUBLIC → route directly to auth-service
2. Extract Authorization header
   - MISSING → return 401
3. Parse Bearer token
   - MALFORMED → return 401
4. Validate JWT signature
   - INVALID → return 401
5. Check token expiration
   - EXPIRED → return 401
6. Extract claims (sub, role)
7. Add headers: X-User-Id, X-User-Role
8. Route to target service based on URL path
9. Return service response to client
```

## 10. Login History Query Flow

```
Input: LoginHistoryFilter { userId?, startDate?, endDate?, success?, page, size }

1. Verify requester has ADMIN or ROOT_ADMIN role
2. Build query from filter parameters
3. Execute paginated query on LoginHistory collection
4. Return Page<LoginHistory>
```

---

## Testable Properties (PBT-01)

### Round-Trip Properties
| Property | Description | Category |
|---|---|---|
| JWT encode/decode | 토큰 생성 후 파싱하면 동일한 claims 복원 | Round-trip |
| bcrypt hash/verify | 해싱 후 verify하면 항상 true | Round-trip |

### Invariant Properties
| Property | Description | Category |
|---|---|---|
| Lock threshold | failedLoginAttempts >= 3 이면 항상 isLocked=true | Invariant |
| Role hierarchy | ROOT_ADMIN은 항상 모든 권한 보유 | Invariant |
| Token expiry | Access Token exp = iat + 900 (15분) | Invariant |
| Refresh Token expiry | Refresh Token exp = iat + 604800 (7일) | Invariant |

### Idempotence Properties
| Property | Description | Category |
|---|---|---|
| Logout idempotent | 동일 토큰으로 여러 번 logout해도 결과 동일 | Idempotence |
| Unlock idempotent | 이미 잠금 해제된 계정을 다시 해제해도 오류 없음 | Idempotence |
| Root init idempotent | 이미 존재하면 생성하지 않음 | Idempotence |

### Business Rule Invariants
| Property | Description | Category |
|---|---|---|
| Root immutable | ROOT_ADMIN은 어떤 경우에도 삭제/역할변경 불가 | Invariant |
| Locked blocks login | isLocked=true이면 올바른 비밀번호도 로그인 불가 | Invariant |
| Success resets count | 성공 로그인 후 failedLoginAttempts는 항상 0 | Invariant |

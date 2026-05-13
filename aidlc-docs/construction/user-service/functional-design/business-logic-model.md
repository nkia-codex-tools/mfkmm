# Business Logic Model - User Service

## 1. Create User Flow

```
Input: CreateUserRequest { userId, name, email, department, role, password, memo }
Actor: Admin (X-User-Id, X-User-Role from Gateway)

1. Verify requester role is ADMIN or ROOT_ADMIN
   - INSUFFICIENT → return 403
2. Validate input fields (userId required, email format, role valid)
   - INVALID → return 400
3. Check role != ROOT_ADMIN (수동 ROOT_ADMIN 생성 차단)
   - ROOT_ADMIN 시도 → return 400 "ROOT_ADMIN cannot be assigned manually"
4. Check userId uniqueness
   - DUPLICATE → return 409 "User ID already exists"
5. Create User entity:
   - userId, name, email, department, role, memo
   - isRootAdmin = false
   - isLocked = false
   - mustChangePassword = true
   - createdAt = now, createdBy = requester
6. Save User → UserRepository
7. Delegate password creation to Auth Service (via REST or event)
   - Send: { userId, password } → Auth Service hashes and stores
8. Create AuditLog (USER_CREATED)
9. Publish UserCreated event
10. Return UserResponse
```

## 2. Update User Flow

```
Input: UpdateUserRequest { name?, email?, department?, memo? }
Actor: Self (본인) or Admin

1. Find user by id
   - NOT FOUND → return 404
2. Determine authorization:
   - Self updating own info → allowed
   - Admin updating others → allowed
   - Non-admin updating others → return 403
   - Anyone updating ROOT_ADMIN (except ROOT_ADMIN themselves) → return 403
3. Apply changes (only non-null fields)
4. Set updatedAt = now, updatedBy = requester
5. Save User
6. Create AuditLog (USER_UPDATED, previousValue, newValue)
7. Return UserResponse
```

## 3. Delete User Flow

```
Input: { targetUserId }
Actor: Admin

1. Verify requester role is ADMIN or ROOT_ADMIN
   - INSUFFICIENT → return 403
2. Find target user by id
   - NOT FOUND → return 404
3. Check target.isRootAdmin
   - TRUE → return 403 "Root administrator cannot be deleted"
4. Delete User from DB (hard delete)
5. Publish UserDeleted event
   - History Service: 해당 사용자 작업 이력 삭제
   - Auth Service: 해당 사용자 인증 정보 + Refresh Token 삭제
6. Create AuditLog (USER_DELETED)
7. Return 204 No Content
```

## 4. Get Users (List) Flow

```
Input: UserFilter { userId?, name?, email?, department?, role?, page, size, sort }
Actor: Admin

1. Verify requester role is ADMIN or ROOT_ADMIN
   - INSUFFICIENT → return 403
2. Build query from filter parameters
3. Execute paginated query
4. Return Page<UserResponse> (전체 정보: ID, 이름, 이메일, 부서, 권한, 생성일, 잠금 상태)
```

## 5. Get User (Self) Flow

```
Input: userId (from JWT claims)
Actor: Any authenticated user

1. Find user by userId (from X-User-Id header)
   - NOT FOUND → return 404
2. Return UserResponse (본인 정보)
```

## 6. Grant Permission Flow

```
Input: { targetUserId, newRole }
Actor: Admin

1. Verify requester role is ADMIN or ROOT_ADMIN
   - INSUFFICIENT → return 403
2. Find target user
   - NOT FOUND → return 404
3. Check target.isRootAdmin
   - TRUE → return 403 "Root administrator role cannot be changed"
4. Validate newRole (READ, WRITE, ADMIN만 허용, ROOT_ADMIN 불가)
   - INVALID → return 400
5. Store previousRole = target.role
6. Set target.role = newRole
7. Save User
8. Create AuditLog (PERMISSION_GRANTED, previousRole → newRole)
9. Publish PermissionChanged event
10. Return UserResponse
```

## 7. Revoke Permission Flow

```
Input: { targetUserId }
Actor: Admin

1. Verify requester role is ADMIN or ROOT_ADMIN
   - INSUFFICIENT → return 403
2. Find target user
   - NOT FOUND → return 404
3. Check target.isRootAdmin
   - TRUE → return 403 "Root administrator permission cannot be revoked"
4. Check if requester == target (셀프 강등 방지)
   - SAME → return 400 "Cannot revoke own admin permission"
5. Store previousRole = target.role
6. Set target.role = READ (기본 권한으로 하향)
7. Save User
8. Create AuditLog (PERMISSION_REVOKED, previousRole → READ)
9. Publish PermissionChanged event
10. Return UserResponse
```

## 8. Unlock Account Flow

```
Input: { targetUserId }
Actor: Admin

1. Verify requester role is ADMIN or ROOT_ADMIN
   - INSUFFICIENT → return 403
2. Find target user
   - NOT FOUND → return 404
3. Check target.isLocked
   - NOT LOCKED → return success (idempotent)
4. Set target.isLocked = false
5. Save User
6. Create AuditLog (ACCOUNT_UNLOCKED)
7. Publish AccountUnlocked event (Auth Service가 수신하여 동기화)
8. Return success
```

## 9. Get Audit Logs Flow

```
Input: AuditLogFilter { action?, targetUserId?, performedBy?, startDate?, endDate?, page, size }
Actor: Admin

1. Verify requester role is ADMIN or ROOT_ADMIN
   - INSUFFICIENT → return 403
2. Build query from filter
3. Execute paginated query
4. Return Page<AuditLog>
```

---

## Testable Properties (PBT-01)

### Invariant Properties
| Property | Description | Category |
|---|---|---|
| Root immutability | ROOT_ADMIN 삭제/권한변경 시도 시 항상 거부 | Invariant |
| Self-demotion block | Admin이 자신의 권한을 revoke하면 항상 거부 | Invariant |
| UserId uniqueness | 중복 userId 생성 시도 시 항상 409 반환 | Invariant |
| Permission hierarchy | 부여된 role은 항상 READ/WRITE/ADMIN 중 하나 | Invariant |
| mustChangePassword on create | 새 사용자 생성 시 항상 mustChangePassword=true | Invariant |

### Idempotence Properties
| Property | Description | Category |
|---|---|---|
| Unlock idempotent | 이미 잠금 해제된 계정을 다시 해제해도 동일 결과 | Idempotence |
| Delete idempotent | 이미 삭제된 사용자 삭제 시도 시 404 (부작용 없음) | Idempotence |

### Business Rule Invariants
| Property | Description | Category |
|---|---|---|
| Audit completeness | 모든 생성/수정/삭제/권한변경 후 AuditLog 레코드가 항상 존재 | Invariant |
| Revoke defaults to READ | 권한 회수 후 role은 항상 READ | Invariant |
| Admin-only operations | WRITE/READ 사용자의 사용자 관리 API 호출 시 항상 403 | Invariant |

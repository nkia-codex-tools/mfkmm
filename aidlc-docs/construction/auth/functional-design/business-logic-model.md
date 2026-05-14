# Business Logic Model - Unit 1: Auth

## 1. Registration Flow

```
Input: email, password, name
    |
    v
[Validate Input]
    - email format valid?
    - password >= 8 chars?
    - name not blank, <= 100 chars?
    |
    +--(invalid)--> Return 400 + validation errors
    |
    v
[Check Duplicate]
    - email already exists?
    |
    +--(exists)--> Return 409 Conflict
    |
    v
[Create User]
    - hash password (BCrypt)
    - set role = PENDING
    - set status = PENDING
    - set failed_login_attempts = 0
    - save to DB
    |
    v
Return 201 + UserResponse (id, email, name, role, status)
```

## 2. Login Flow

```
Input: email, password
    |
    v
[Find User by Email]
    |
    +--(not found)--> Return 401 "Invalid credentials"
    |
    v
[Check Account Lock]
    - locked_until > now?
    |
    +--(locked)--> Return 423 "Account locked until {time}"
    |
    v
[Verify Password]
    - BCrypt.verify(password, user.password_hash)
    |
    +--(mismatch)--> [Increment Failed Attempts]
    |                     - failed_login_attempts += 1
    |                     - if >= 5: set locked_until = now + 15min
    |                     - Return 401 "Invalid credentials"
    |
    v
[Login Success]
    - reset failed_login_attempts = 0
    - clear locked_until
    - generate Access Token (15min)
    - generate Refresh Token (7days)
    |
    v
Return 200 + TokenResponse (accessToken, refreshToken, expiresIn, user)
```

## 3. Token Refresh Flow

```
Input: refreshToken
    |
    v
[Validate Refresh Token]
    - verify signature
    - check expiration
    - check type = "refresh"
    |
    +--(invalid)--> Return 401 "Invalid refresh token"
    |
    v
[Load User]
    - find by token.sub (user ID)
    |
    +--(not found)--> Return 401 "User not found"
    |
    v
[Check User Status]
    - status == LOCKED?
    |
    +--(locked)--> Return 423 "Account locked"
    |
    v
[Generate New Access Token]
    - use current user role (may have changed)
    |
    v
Return 200 + TokenResponse (new accessToken, same refreshToken, expiresIn)
```

## 4. Role Update Flow (Admin)

```
Input: targetUserId, newRole (from Admin)
    |
    v
[Validate Admin Permission]
    - current user role == ADMIN?
    |
    +--(not admin)--> Return 403 Forbidden
    |
    v
[Self-Modification Check]
    - targetUserId == currentUserId?
    |
    +--(same)--> Return 400 "Cannot modify own role"
    |
    v
[Last Admin Check]
    - if target.role == ADMIN and newRole != ADMIN:
        count remaining ADMINs
    |
    +--(only 1 admin left)--> Return 400 "Cannot remove last admin"
    |
    v
[Update Role]
    - set target.role = newRole
    - if target.status == PENDING: set status = ACTIVE
    - save to DB
    |
    v
Return 200 + UserResponse (updated user)
```

## 5. Initial Admin Seeding Flow

```
[Application Startup]
    |
    v
[Check Existing Admin]
    - count users with role == ADMIN
    |
    +--(admin exists)--> Skip seeding
    |
    v
[Read Environment Variables]
    - ADMIN_EMAIL (required)
    - ADMIN_PASSWORD (required)
    |
    +--(missing)--> Log warning, skip seeding
    |
    v
[Create Admin User]
    - email = ADMIN_EMAIL
    - password_hash = BCrypt(ADMIN_PASSWORD)
    - name = "System Admin"
    - role = ADMIN
    - status = ACTIVE
    - save to DB
    |
    v
Log: "Initial admin account created: {email}"
```

## 6. JWT Authentication Filter Flow

```
[Every HTTP Request]
    |
    v
[Check Authorization Header]
    - "Bearer {token}" present?
    |
    +--(absent)--> Continue (anonymous request)
    |
    v
[Validate Token]
    - verify signature
    - check expiration
    |
    +--(invalid)--> Continue (anonymous, will fail at authorization)
    |
    v
[Extract Claims]
    - userId, email, role
    |
    v
[Set Security Context]
    - create Authentication object
    - set in SecurityContextHolder
    |
    v
[Continue Filter Chain]
```

## 7. Rate Limiting Logic

```
[Request Received]
    |
    v
[Identify Caller]
    - authenticated: use userId
    - anonymous: use IP address
    |
    v
[Check Rate Window]
    - get request count in current 1-minute window
    |
    v
[Apply Limit]
    - login endpoint: 10/min per IP
    - register endpoint: 5/min per IP
    - general API: 100/min per user
    |
    +--(exceeded)--> Return 429 + Retry-After header
    |
    v
[Increment Counter]
    - increment request count for this window
    |
    v
[Continue]
```

## 8. Pending Users Query Flow (Admin)

```
Input: (Admin request)
    |
    v
[Query Users]
    - WHERE role = PENDING
    - ORDER BY created_at DESC
    |
    v
Return List<UserResponse> (id, email, name, role, status, created_at)
```

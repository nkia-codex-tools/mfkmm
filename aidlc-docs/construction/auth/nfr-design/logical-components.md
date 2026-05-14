# Logical Components - Unit 1: Auth

## Component Diagram

```
+---------------------------------------------------------------+
|                    Spring Boot Application                      |
|                                                                |
|  +----------------------------------------------------------+ |
|  |                  Filter Chain                              | |
|  |                                                           | |
|  |  +-------------------+  +----------------+  +-----------+ | |
|  |  | SecurityHeaders   |  | RateLimit      |  | JwtAuth   | | |
|  |  | Filter            |  | Filter         |  | Filter    | | |
|  |  | (CSP, HSTS, etc.) |  | (Bucket4j)     |  | (Token)   | | |
|  |  +-------------------+  +----------------+  +-----------+ | |
|  +----------------------------------------------------------+ |
|                                                                |
|  +----------------------------------------------------------+ |
|  |                  Controller Layer                          | |
|  |  +----------------+  +------------------+                 | |
|  |  | AuthController |  | UserController   |                 | |
|  |  | (login,signup) |  | (admin ops)      |                 | |
|  |  +----------------+  +------------------+                 | |
|  +----------------------------------------------------------+ |
|                                                                |
|  +----------------------------------------------------------+ |
|  |                  Service Layer                             | |
|  |  +--------------+  +--------------+  +----------------+  | |
|  |  | AuthService  |  | UserService  |  | JwtTokenProv.  |  | |
|  |  | (auth logic) |  | (user mgmt)  |  | (token ops)    |  | |
|  |  +--------------+  +--------------+  +----------------+  | |
|  +----------------------------------------------------------+ |
|                                                                |
|  +----------------------------------------------------------+ |
|  |                  Repository Layer                          | |
|  |  +--------------------------------------------------+    | |
|  |  | UserRepository (Spring Data JPA)                  |    | |
|  |  +--------------------------------------------------+    | |
|  +----------------------------------------------------------+ |
|                                                                |
|  +----------------------------------------------------------+ |
|  |                  Cross-Cutting                             | |
|  |  +------------------+  +-------------------+              | |
|  |  | GlobalException  |  | RequestId MDC     |              | |
|  |  | Handler          |  | Filter            |              | |
|  |  +------------------+  +-------------------+              | |
|  +----------------------------------------------------------+ |
+---------------------------------------------------------------+
         |
         v
+-------------------+
|  SQLite Database  |
|  (Docker Volume)  |
+-------------------+
```

## Logical Component Specifications

### 1. SecurityHeadersFilter

**Type**: HTTP Filter (OncePerRequestFilter)
**Order**: 1 (가장 먼저 실행)

**Responsibility**: 모든 응답에 보안 헤더 추가

**Output Headers**:
```
Content-Security-Policy: default-src 'self'; script-src 'self'; style-src 'self' 'unsafe-inline'
Strict-Transport-Security: max-age=31536000; includeSubDomains
X-Content-Type-Options: nosniff
X-Frame-Options: DENY
Referrer-Policy: strict-origin-when-cross-origin
```

---

### 2. RateLimitFilter

**Type**: HTTP Filter (OncePerRequestFilter)
**Order**: 2

**Responsibility**: 요청 속도 제한

**Internal State**:
```
ConcurrentHashMap<String, Bucket> buckets
  - key: "{endpoint}:{identifier}" (예: "login:192.168.1.1")
  - value: Bucket4j Bucket 인스턴스
```

**Logic**:
1. 요청 경로에서 endpoint 타입 결정 (login/register/api)
2. 식별자 결정 (인증됨 → userId, 미인증 → IP)
3. 해당 버킷에서 토큰 소비 시도
4. 실패 → 429 응답 + Retry-After 헤더
5. 성공 → 다음 필터로 진행

**Cleanup**: 스케줄러로 1시간마다 만료 버킷 제거

---

### 3. JwtAuthenticationFilter

**Type**: HTTP Filter (OncePerRequestFilter)
**Order**: 3

**Responsibility**: JWT 토큰 검증 및 SecurityContext 설정

**Logic**:
1. Authorization 헤더에서 "Bearer " 접두어 제거 → 토큰 추출
2. JwtTokenProvider.validateToken(token)
3. 유효 → Claims에서 userId, role 추출 → UsernamePasswordAuthenticationToken 생성 → SecurityContextHolder에 설정
4. 무효/없음 → SecurityContext 비움 (anonymous로 진행)

---

### 4. JwtTokenProvider

**Type**: Spring Component (@Component)

**Responsibility**: JWT 토큰 생성, 검증, 파싱

**Methods**:
- `generateAccessToken(User)`: Access Token 생성 (sub, email, role, exp=15min)
- `generateRefreshToken(User)`: Refresh Token 생성 (sub, type="refresh", exp=7days)
- `validateToken(String token)`: 서명 + 만료 검증 → boolean
- `getClaims(String token)`: Claims 추출

**Configuration**:
- Secret Key: `${JWT_SECRET}` (환경 변수, 최소 256-bit)
- Algorithm: HS256

---

### 5. SecurityConfig

**Type**: Spring Security Configuration (@Configuration)

**Responsibility**: 전체 보안 설정 정의

**URL Authorization Rules**:
```
POST /api/auth/login       → permitAll
POST /api/auth/register    → permitAll
POST /api/auth/refresh     → permitAll
GET  /api/admin/**         → hasRole(ADMIN)
PATCH /api/admin/**        → hasRole(ADMIN)
GET  /api/resources/**     → hasAnyRole(READER, WRITER, ADMIN)
POST /api/resources/**     → hasAnyRole(WRITER, ADMIN)
PUT  /api/resources/**     → hasAnyRole(WRITER, ADMIN)
DELETE /api/resources/**   → hasAnyRole(WRITER, ADMIN)
POST /api/versions/*/rollback → hasRole(ADMIN)
POST /api/versions         → hasRole(ADMIN)
/**                        → denyAll (기본 거부)
```

**Other Config**:
- CSRF: disabled (stateless JWT)
- Session: STATELESS
- CORS: configured from `cors.allowed-origins`
- Filter chain: SecurityHeaders → RateLimit → JwtAuth

---

### 6. GlobalExceptionHandler

**Type**: @RestControllerAdvice

**Responsibility**: 모든 예외를 일관된 에러 응답으로 변환

**Response Format**:
```json
{
  "status": 401,
  "error": "Unauthorized",
  "message": "Invalid credentials",
  "timestamp": "2026-05-13T10:30:00Z",
  "requestId": "uuid-xxx"
}
```

**Rules**:
- 절대 stack trace 노출하지 않음
- 절대 내부 경로/클래스명 노출하지 않음
- 모든 예외 로깅 (ERROR 레벨)
- 예상치 못한 예외 → "Internal server error" 일반 메시지

---

### 7. RequestIdFilter

**Type**: HTTP Filter (OncePerRequestFilter)
**Order**: 0 (가장 먼저)

**Responsibility**: 요청별 고유 ID 생성 및 MDC 설정

**Logic**:
1. UUID 생성
2. MDC.put("requestId", uuid)
3. 응답 헤더 X-Request-Id 추가
4. 다음 필터 진행
5. finally: MDC.clear()

---

### 8. AdminInitializer

**Type**: ApplicationRunner

**Responsibility**: 앱 시작 시 초기 관리자 계정 생성

**Logic**:
1. UserRepository.countByRole(ADMIN) > 0 → skip
2. 환경 변수 ADMIN_EMAIL, ADMIN_PASSWORD 읽기
3. 없으면 WARN 로그 후 skip
4. User 생성 (role=ADMIN, status=ACTIVE)
5. INFO 로그: "Initial admin created"

---

## Data Flow: Login Request

```
Client
  |
  | POST /api/auth/login {email, password}
  v
[RequestIdFilter] → MDC(requestId=uuid)
  |
[SecurityHeadersFilter] → 응답에 헤더 추가 예약
  |
[RateLimitFilter] → login 버킷 확인 → 통과
  |
[JwtAuthFilter] → Authorization 헤더 없음 → skip
  |
[AuthController.login()] → @Valid 입력 검증
  |
[AuthService.login()]
  ├── UserRepository.findByEmail()
  ├── Check locked_until
  ├── BCrypt.verify(password, hash)
  ├── Reset/Increment failedLoginAttempts
  ├── JwtTokenProvider.generateAccessToken()
  └── JwtTokenProvider.generateRefreshToken()
  |
  v
Response: 200 {accessToken, refreshToken, expiresIn, user}
  + Security Headers (CSP, HSTS, etc.)
  + X-Request-Id
```

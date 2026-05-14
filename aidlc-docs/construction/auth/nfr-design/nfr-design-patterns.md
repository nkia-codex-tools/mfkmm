# NFR Design Patterns - Unit 1: Auth

## 1. Security Patterns

### 1.1 Filter Chain Pattern (Spring Security)

```
Request → SecurityHeadersFilter → RateLimitFilter → JwtAuthenticationFilter → Controller
```

**구현**:
- `SecurityHeadersFilter` (Order 1): 모든 응답에 보안 헤더 추가
- `RateLimitFilter` (Order 2): Bucket4j 기반 요청 속도 제한
- `JwtAuthenticationFilter` (Order 3): Bearer 토큰 추출 → 검증 → SecurityContext 설정
- Spring Security `SecurityFilterChain`: URL 패턴별 인가 규칙

### 1.2 Stateless Authentication (JWT)

**패턴**: 서버에 세션을 저장하지 않고, JWT 자체에 인증 정보 포함

```
Client                        Server
  |--- POST /login -------->   |
  |<-- {accessToken, refresh}  |
  |                            |
  |--- GET /api/xxx -------->  |
  |    Authorization: Bearer   |
  |    [verify token locally]  |
  |<-- Response               |
```

**장점**: 수평 확장 용이, 서버 상태 없음
**이 프로젝트에서**: 단일 인스턴스이나, 아키텍처적 단순성을 위해 stateless 채택

### 1.3 Token Refresh Pattern

**패턴**: 짧은 수명의 Access Token + 긴 수명의 Refresh Token

```
Access Token (15min)   → API 인증용, 탈취 시 피해 제한
Refresh Token (7days)  → Access Token 재발급용
```

**클라이언트 동작**:
1. API 호출 시 Access Token 사용
2. 401 응답 수신 → Refresh Token으로 새 Access Token 요청
3. Refresh도 만료 → 로그인 페이지로 리다이렉트

### 1.4 Fail-Closed Pattern

**패턴**: 인증/인가 실패 시 항상 접근 거부 (절대 fail-open 하지 않음)

```
Token 검증 실패 → 401 (인증 안됨)
권한 부족       → 403 (접근 거부)
예외 발생       → 500 (서비스 에러, 접근 거부 유지)
```

**구현**:
- Spring Security: `denyAll()` 기본 정책
- 명시적으로 허용하는 URL만 `permitAll()`

---

## 2. Resilience Patterns

### 2.1 Account Lockout with Exponential Backoff Consideration

**패턴**: 연속 실패 시 계정 잠금 (현재 고정 15분)

```
실패 1-4회: 즉시 재시도 가능
실패 5회:   15분 잠금 (locked_until 설정)
잠금 해제 후: 카운터 유지 (다음 실패 시 즉시 재잠금)
로그인 성공:  카운터 리셋
```

### 2.2 Graceful Degradation

**패턴**: 부분 장애 시 가능한 서비스 유지

```
DB 연결 실패 → 503 (모든 기능 불가, 명확한 에러)
JWT Secret 미설정 → 앱 시작 거부 (빠른 실패)
Rate limit 스토리지 손상 → 제한 없이 통과 (가용성 우선) 
```

### 2.3 Global Exception Handler Pattern

**패턴**: 모든 미처리 예외를 한 곳에서 포착

```java
@RestControllerAdvice
GlobalExceptionHandler
    ├── handleValidationException → 400 + 필드별 에러
    ├── handleAuthenticationException → 401
    ├── handleAccessDeniedException → 403
    ├── handleNotFoundException → 404
    ├── handleConflictException → 409
    ├── handleRateLimitException → 429
    └── handleException → 500 + 일반 메시지 (내부 정보 숨김)
```

---

## 3. Performance Patterns

### 3.1 In-Memory Rate Limiting (Bucket4j)

**패턴**: Token Bucket 알고리즘으로 인메모리 rate limiting

```
각 버킷: {IP 또는 userId} → {남은 토큰 수, 마지막 리필 시각}
요청 도착 → 토큰 소모 시도 → 성공이면 통과, 실패면 429
```

**구성**:
- 로그인: Bucket(capacity=10, refill=10/min, key=IP)
- 가입: Bucket(capacity=5, refill=5/min, key=IP)
- API: Bucket(capacity=100, refill=100/min, key=userId)

**메모리 관리**: ConcurrentHashMap + 만료된 엔트리 주기적 정리 (1시간)

### 3.2 Lazy Initialization

**패턴**: 비용이 큰 리소스는 필요 시 초기화

- BCrypt encoder: 앱 시작 시 단 한 번 생성 (Spring Bean)
- JWT parser: 앱 시작 시 단 한 번 생성

---

## 4. Logging & Monitoring Patterns

### 4.1 Structured Logging (JSON)

**패턴**: logstash-logback-encoder를 사용한 JSON 구조적 로깅

```json
{
  "timestamp": "2026-05-13T10:30:00Z",
  "level": "WARN",
  "logger": "c.r.auth.service.AuthService",
  "message": "Login failed - account locked",
  "requestId": "uuid-xxx",
  "userId": null,
  "email": "user@example.com",
  "ip": "192.168.1.100",
  "action": "LOGIN_FAILED_LOCKED"
}
```

**민감 정보 제외**: password, token 값은 절대 로깅하지 않음

### 4.2 Security Event Logging

**패턴**: 보안 관련 이벤트를 구분하여 로깅

| Event | Level | Fields |
|-------|-------|--------|
| 로그인 성공 | INFO | userId, email, ip |
| 로그인 실패 | WARN | email, ip, failCount |
| 계정 잠금 | WARN | email, ip, lockedUntil |
| 권한 변경 | INFO | adminId, targetId, oldRole, newRole |
| Rate limit 초과 | WARN | ip/userId, endpoint, limit |
| 토큰 검증 실패 | DEBUG | ip, reason |

### 4.3 Request Correlation (MDC)

**패턴**: 요청별 고유 ID로 로그 추적

```
Request → MDC.put("requestId", UUID) → 모든 로그에 requestId 포함 → MDC.clear()
```

---

## 5. Configuration Patterns

### 5.1 Externalized Configuration

**패턴**: 환경별 설정 외부화

```yaml
# application.yml (기본값)
jwt:
  secret: ${JWT_SECRET}
  access-token-expiry: 900000   # 15분 (ms)
  refresh-token-expiry: 604800000  # 7일 (ms)

admin:
  email: ${ADMIN_EMAIL}
  password: ${ADMIN_PASSWORD}

rate-limit:
  login: 10
  register: 5
  api: 100

cors:
  allowed-origins: ${CORS_ORIGINS:http://localhost:5173}
```

### 5.2 Fail-Fast Configuration Validation

**패턴**: 필수 설정이 없으면 앱 시작 거부

```java
@PostConstruct
void validateConfig() {
    if (jwtSecret == null || jwtSecret.length() < 32) {
        throw new IllegalStateException("JWT_SECRET must be at least 32 chars");
    }
}
```

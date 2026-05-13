# NFR Design - Auth Service + API Gateway + Shared Library

## 1. JWT RS256 Key Management Design

### Key Generation & Storage
```
[Auth Service]
  └── resources/keys/
       ├── private.pem    (RSA 2048-bit, PKCS#8)
       └── public.pem     (RSA public key)

[API Gateway]
  └── resources/keys/
       └── public.pem     (same public key - 검증 전용)
```

### Configuration
```yaml
# Auth Service - application.yml
jwt:
  private-key-path: classpath:keys/private.pem
  public-key-path: classpath:keys/public.pem
  access-token-ttl: 900        # 15분 (초)
  refresh-token-ttl: 604800    # 7일 (초)
  issuer: mkfmm-auth

# API Gateway - application.yml
jwt:
  public-key-path: classpath:keys/public.pem
  issuer: mkfmm-auth
```

### Token Signing Flow
```
Auth Service:
  1. Load RSA PrivateKey from PEM on startup (cache in memory)
  2. Sign JWT with RS256 algorithm using private key
  3. Include claims: sub, role, iat, exp, jti, iss

API Gateway:
  1. Load RSA PublicKey from PEM on startup (cache in memory)
  2. Verify JWT signature with public key
  3. Validate: exp not expired, iss matches expected issuer
  4. Extract sub (userId), role → forward as headers
```

### Key Rotation Strategy (MVP)
- MVP에서는 key rotation 미구현
- 키 변경 시: 새 키 생성 → 모든 서비스 재배포 → 기존 토큰 만료 대기 (최대 15분)
- Docker volume mount로 키 파일 공유 가능

---

## 2. RabbitMQ At-Least-Once Delivery Design

### Exchange & Queue Topology
```
Exchange: mkfmm.events (topic, durable)
  ├── Routing Key: auth.user.logged-in    → Queue: history.auth-events
  ├── Routing Key: auth.user.logged-out   → Queue: history.auth-events
  ├── Routing Key: auth.account.locked    → Queue: history.auth-events
  ├── Routing Key: auth.account.unlocked  → Queue: history.auth-events
  └── Routing Key: auth.password.changed  → Queue: history.auth-events
```

### Publisher (Auth Service) Design
```java
// Publisher confirms 활성화
spring.rabbitmq.publisher-confirm-type=correlated
spring.rabbitmq.publisher-returns=true

// Publishing flow:
// 1. Execute business logic + save to DB (within transaction)
// 2. Publish event to RabbitMQ
// 3. If confirm fails → log error (event lost)
//    - MVP: log + alert, no retry queue
//    - Future: outbox pattern for guaranteed delivery
```

### Consumer (History Service) Design
```java
// Manual acknowledgement
spring.rabbitmq.listener.simple.acknowledge-mode=manual

// Consuming flow:
// 1. Receive message
// 2. Check eventId in processed_events collection (deduplication)
//    - If exists → ack + skip (duplicate)
// 3. Process event → save to work_log collection
// 4. Save eventId to processed_events collection
// 5. Ack message
// 6. If processing fails → nack + requeue (retry)
```

### Message Format (Shared Library)
```json
{
  "eventId": "uuid-v4",
  "eventType": "auth.user.logged-in",
  "timestamp": "2026-05-13T12:00:00Z",
  "userId": "admin01",
  "payload": {
    "ipAddress": "192.168.1.100",
    "loginAt": "2026-05-13T12:00:00Z"
  }
}
```

### Dead Letter Queue
```
DLQ: mkfmm.events.dlq (durable)
  - Max retry: 3회 (x-death header count)
  - 3회 초과 시 DLQ로 이동
  - DLQ 메시지는 수동 조사 후 처리
```

---

## 3. Docker Health Check Design

### Auth Service Healthcheck
```dockerfile
HEALTHCHECK --interval=30s --timeout=10s --retries=3 --start-period=40s \
  CMD curl -f http://localhost:8081/actuator/health || exit 1
```

### API Gateway Healthcheck
```dockerfile
HEALTHCHECK --interval=30s --timeout=10s --retries=3 --start-period=30s \
  CMD curl -f http://localhost:8080/actuator/health || exit 1
```

### Spring Boot Actuator Configuration
```yaml
management:
  endpoints:
    web:
      exposure:
        include: health
  endpoint:
    health:
      show-details: never  # 외부 노출 최소화
      probes:
        enabled: true
  health:
    mongo:
      enabled: true
    rabbit:
      enabled: true
```

### Health Indicators
| Service | Checks |
|---|---|
| Auth Service | MongoDB connection, RabbitMQ connection |
| API Gateway | Downstream services reachable (optional) |

### Docker Compose Restart Policy
```yaml
services:
  auth-service:
    restart: on-failure
    deploy:
      restart_policy:
        condition: on-failure
        max_attempts: 5
        delay: 10s
```

---

## 4. Security Design Patterns

### Password Hashing (Auth Service)
```java
// BCryptPasswordEncoder with strength=10
// Spring Security Crypto module
// Encode: on registration, password change, password reset
// Verify: on login attempt
```

### API Gateway JWT Filter Chain
```
Request → CorsFilter → JwtAuthenticationFilter → RouteFilter → Backend

JwtAuthenticationFilter:
  1. Skip for public paths (/api/auth/login, /api/auth/refresh)
  2. Extract Bearer token from Authorization header
  3. Validate with RSA public key (signature + expiry + issuer)
  4. On success: add X-User-Id, X-User-Role headers → continue
  5. On failure: return 401 Unauthorized (no body details)
```

### Header Propagation
```
Gateway → Backend services:
  X-User-Id: {userId from JWT sub claim}
  X-User-Role: {role from JWT role claim}

Backend services MUST:
  - Trust these headers (gateway already validated)
  - Never accept these headers from external requests directly
```

---

## 5. Error Response Design (Shared Library)

### Standard Error Response
```json
{
  "code": "AUTH_001",
  "message": "Invalid credentials",
  "timestamp": "2026-05-13T12:00:00Z",
  "details": null
}
```

### Error Code Scheme
| Code | Description |
|---|---|
| AUTH_001 | Invalid credentials (login failure) |
| AUTH_002 | Account locked |
| AUTH_003 | Token expired |
| AUTH_004 | Token invalid |
| AUTH_005 | Insufficient permissions |
| AUTH_006 | Refresh token revoked |
| AUTH_007 | Root admin constraint violation |
| GW_001 | Service unavailable (downstream) |
| GW_002 | Missing authorization header |
| COMMON_001 | Validation error |
| COMMON_002 | Resource not found |

---

## 6. Logging Design

### Log Output
- Format: Spring Boot default (plain text with timestamp)
- Output: stdout/stderr → captured by Docker json-file driver
- Correlation: Include userId in MDC for request-scoped logging

### Log Levels by Component
| Component | Level | Notes |
|---|---|---|
| Application code | INFO | 비즈니스 이벤트 |
| Security events | WARN | 로그인 실패, 잠금 |
| Errors | ERROR | 예외, 연결 실패 |
| Framework | WARN | Spring/Netty 내부 |

### MDC Context (Request-scoped)
```
userId: {from X-User-Id header or JWT}
requestId: {generated UUID per request}
```

---

## 7. Configuration Management Design

### Environment Variables
| Variable | Service | Purpose |
|---|---|---|
| MKFMM_ROOT_USER | Auth | Root admin 초기 ID |
| MKFMM_ROOT_PASSWORD | Auth | Root admin 초기 비밀번호 |
| MONGODB_URI | Auth, Gateway | MongoDB 연결 문자열 |
| RABBITMQ_HOST | Auth | RabbitMQ 호스트 |
| RABBITMQ_PORT | Auth | RabbitMQ 포트 |
| RABBITMQ_USER | Auth | RabbitMQ 사용자 |
| RABBITMQ_PASS | Auth | RabbitMQ 비밀번호 |
| JWT_PRIVATE_KEY_PATH | Auth | RSA private key 경로 |
| JWT_PUBLIC_KEY_PATH | Auth, Gateway | RSA public key 경로 |
| CORS_ALLOWED_ORIGINS | Gateway | 허용 도메인 |

### Spring Profiles
```
- default: 개발 환경 (localhost 연결)
- docker: Docker Compose 환경 (service name 연결)
```

---

## 8. PBT (Property-Based Testing) Design

### Test Properties per NFR
| NFR | Test Property | Framework |
|---|---|---|
| RS256 signing | Any valid claims → sign → verify = success | jqwik |
| RS256 tamper | Modified token → verify = failure | jqwik |
| bcrypt | Any password → hash → verify = true | jqwik |
| bcrypt timing | Hash time ∈ [50ms, 500ms] for cost=10 | jqwik |
| Token TTL | Generated token exp = iat + configured TTL | jqwik |
| Message format | Any event → serialize → deserialize = equal | jqwik |
| Error codes | All defined codes follow pattern [A-Z]+_[0-9]{3} | jqwik |

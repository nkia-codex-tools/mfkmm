# NFR Requirements - Auth Service + API Gateway + Shared Library

## NFR-SEC: Security Requirements

### NFR-SEC-01: JWT Signing Algorithm
- **Algorithm**: RS256 (RSA + SHA-256)
- **Rationale**: 비대칭키 방식으로 Auth Service만 private key 보유, 다른 서비스는 public key로 검증
- **Key Management**:
  - Private key: Auth Service만 보유 (서명용)
  - Public key: API Gateway + 필요한 서비스에 배포 (검증용)
  - Key format: PEM (PKCS#8)
  - Key size: 2048-bit RSA minimum

### NFR-SEC-02: Password Hashing
- **Algorithm**: bcrypt
- **Cost factor**: 10
- **Rationale**: 사내 도구 규모(10~50명)에 적합한 보안/성능 균형

### NFR-SEC-03: Token Lifetime
- Access Token: 15분 (900초)
- Refresh Token: 7일 (604,800초)
- Refresh Token Rotation: 미적용 (MVP)

### NFR-SEC-04: Account Lock Policy
- 연속 3회 실패 시 자동 잠금
- 관리자(ADMIN/ROOT_ADMIN)만 해제 가능

---

## NFR-PERF: Performance Requirements

### NFR-PERF-01: Login Response Time
- **Target**: 특별한 기준 없음 (합리적 범위)
- **Context**: 사내 도구이므로 bcrypt 연산 포함하여 합리적 시간 내 응답이면 충분
- **Expected range**: 200ms ~ 1.5s (bcrypt cost=10 기준)

### NFR-PERF-02: JWT Validation (Gateway)
- **Target**: < 10ms (서명 검증 only)
- **Rationale**: RS256 public key 검증은 매우 빠름, 매 요청마다 수행되므로 가벼워야 함

### NFR-PERF-03: Concurrent Users
- **Target**: 10~50명 동시 사용
- **Rationale**: 사내 도구 규모

---

## NFR-REL: Reliability Requirements

### NFR-REL-01: Message Delivery (RabbitMQ)
- **Guarantee**: At-least-once delivery
- **Implementation**:
  - Publisher confirms 활성화
  - Consumer manual acknowledgement
  - 메시지 persistent (durable queue)
- **Deduplication**: History Service에서 eventId 기반 중복 제거 처리
- **Rationale**: 이력 유실 방지가 중요하되, exactly-once의 복잡도는 불필요

### NFR-REL-02: Service Availability
- **Strategy**: Docker Health Check + 자동 재시작
- **Implementation**:
  - Docker healthcheck 설정 (interval: 30s, timeout: 10s, retries: 3)
  - restart policy: on-failure (max 5 retries)
  - Health endpoint: `GET /actuator/health`
- **Rationale**: 적은 설정으로 사내 도구 수준의 가용성 확보

---

## NFR-OPS: Operational Requirements

### NFR-OPS-01: Logging
- **Format**: stdout/stderr → Docker logs
- **Structure**: Plain text (Spring Boot default)
- **Access**: `docker logs <container>` 명령으로 확인
- **Retention**: Docker default (json-file driver, 100MB rotation)
- **Rationale**: 사내 도구이므로 단순하게 시작, 필요시 JSON + 중앙 집중으로 확장 가능

### NFR-OPS-02: Health Monitoring
- Spring Boot Actuator `/actuator/health` endpoint
- Docker healthcheck에서 주기적 호출
- 실패 시 자동 재시작

---

## NFR-TEST: Testing Requirements (PBT Extension)

### NFR-TEST-01: Property-Based Testing Framework
- **Framework**: jqwik (Java PBT framework)
- **Integration**: JUnit 5 platform
- **Coverage targets**:
  - JWT encode/decode round-trip
  - bcrypt hash/verify round-trip
  - Account lock invariants
  - Token expiry invariants
  - Idempotence properties (logout, unlock, root init)

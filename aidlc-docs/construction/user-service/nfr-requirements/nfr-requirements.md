# NFR Requirements - User Service

## 1. Data Validation

| ID | Requirement | Detail |
|---|---|---|
| NFR-USR-VAL-01 | 필수 필드 검증 | userId, name, role은 null/blank 불가 |
| NFR-USR-VAL-02 | userId 형식 | 영숫자만 허용 (정규식: `^[a-zA-Z0-9]+$`) |
| NFR-USR-VAL-03 | email 형식 | 기본 email 형식 검증 (optional 필드이므로 값이 있을 때만) |
| NFR-USR-VAL-04 | 문자열 길이 | 명시적 제한 없음 (MongoDB 문서 크기 제한에 의존) |

---

## 2. Data Retention

| ID | Requirement | Detail |
|---|---|---|
| NFR-USR-RET-01 | 감사 로그 보존 | 2년 보존 후 자동 삭제 |
| NFR-USR-RET-02 | 삭제 스케줄 | 스케줄 작업으로 2년 경과 AuditLog 삭제 |
| NFR-USR-RET-03 | 사용자 데이터 | 삭제 시 즉시 제거 (soft delete 없음, hard delete) |

---

## 3. Reliability & Consistency

| ID | Requirement | Detail |
|---|---|---|
| NFR-USR-REL-01 | 이벤트 전달 | At-least-once 보장 (RabbitMQ 재시도) |
| NFR-USR-REL-02 | 사용자 삭제 일관성 | Eventually consistent — 이벤트 재발행으로 타 서비스 정리 보장 |
| NFR-USR-REL-03 | 재시도 정책 | 실패 시 exponential backoff (1s, 2s, 4s, 최대 3회) |
| NFR-USR-REL-04 | Dead Letter Queue | 3회 재시도 실패 시 DLQ로 이동, 관리자 수동 확인 |

---

## 4. Performance

| ID | Requirement | Detail |
|---|---|---|
| NFR-USR-PERF-01 | 응답 시간 | 특별한 기준 없음 (합리적 범위) |
| NFR-USR-PERF-02 | 동시 사용자 | 10~50명 규모 (관리자가 주로 사용하는 서비스) |
| NFR-USR-PERF-03 | 페이지네이션 | 목록 조회 시 기본 20건, 최대 100건 |

---

## 5. Security

| ID | Requirement | Detail |
|---|---|---|
| NFR-USR-SEC-01 | 접근 제어 | API Gateway에서 JWT 검증 후 X-User-Role로 역할 전달 |
| NFR-USR-SEC-02 | Admin-only API | 사용자 CRUD, 권한 관리, 감사 로그 조회는 ADMIN/ROOT_ADMIN만 |
| NFR-USR-SEC-03 | 데이터 노출 | 비밀번호 해시는 User Service에 저장하지 않음 (Auth Service 관할) |
| NFR-USR-SEC-04 | AuditLog 무결성 | AuditLog는 수정/삭제 불가 (보존 기간 만료 자동 삭제만 허용) |

---

## 6. Monitoring & Logging

| ID | Requirement | Detail |
|---|---|---|
| NFR-USR-LOG-01 | 로그 형식 | 구조화된 JSON 로그 (stdout) |
| NFR-USR-LOG-02 | 로그 수준 | INFO: 정상 작업, WARN: 권한 부족 시도, ERROR: 예외 |
| NFR-USR-LOG-03 | 이벤트 발행 로그 | 모든 이벤트 발행/실패를 로그로 기록 |

---

## 7. Testing

| ID | Requirement | Detail |
|---|---|---|
| NFR-USR-TEST-01 | PBT 적용 | jqwik 프레임워크로 Property-Based Testing 적용 |
| NFR-USR-TEST-02 | PBT 대상 | 권한 검증 로직, userId 유효성 검증, Role hierarchy 검증 |
| NFR-USR-TEST-03 | 예제 기반 테스트 | 핵심 비즈니스 규칙 (ROOT_ADMIN 보호, 셀프 강등 방지) |

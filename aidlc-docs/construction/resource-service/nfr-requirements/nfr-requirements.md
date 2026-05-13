# NFR Requirements - Resource Service

## 1. Performance

| ID | Requirement | Detail |
|---|---|---|
| NFR-RES-PERF-01 | 검색 응답 | 특별 기준 없음 (합리적 범위, 1~10K 데이터) |
| NFR-RES-PERF-02 | 유사도 탐지 | 전체 리소스 대상 비교 (1~10K 규모에서 허용 가능) |
| NFR-RES-PERF-03 | 페이지네이션 | 기본 20건, 최대 100건 |
| NFR-RES-PERF-04 | Levenshtein 계산 | 1차 regex 후보 필터링 후 Java에서 계산 (DB 부하 최소화) |

## 2. Data Management

| ID | Requirement | Detail |
|---|---|---|
| NFR-RES-DATA-01 | 예상 규모 | 1,000~10,000건 (중규모) |
| NFR-RES-DATA-02 | Soft Delete 보존 | 30일간 deleted=true 유지 후 Hard Delete |
| NFR-RES-DATA-03 | Hard Delete 스케줄 | 매일 03:00 (cron) |
| NFR-RES-DATA-04 | 인덱스 전략 | resourceKey(unique), resourceType, content(text), createdAt |

## 3. Reliability

| ID | Requirement | Detail |
|---|---|---|
| NFR-RES-REL-01 | 이벤트 전달 | At-least-once (RabbitMQ, 재시도 3회) |
| NFR-RES-REL-02 | 재시도 정책 | Exponential backoff (1s, 2s, 4s) |
| NFR-RES-REL-03 | DLQ | 3회 실패 시 mkfmm.resource.dlq로 이동 |
| NFR-RES-REL-04 | 중복 등록 방지 | unique index on resourceKey (DB 레벨 보장) |

## 4. Security

| ID | Requirement | Detail |
|---|---|---|
| NFR-RES-SEC-01 | 접근 제어 | READ+: 검색/조회, WRITE+: CRUD |
| NFR-RES-SEC-02 | 인증 | API Gateway JWT 검증 (X-User-Id, X-User-Role) |

## 5. Testing

| ID | Requirement | Detail |
|---|---|---|
| NFR-RES-TEST-01 | PBT 프레임워크 | jqwik |
| NFR-RES-TEST-02 | PBT 대상 | Levenshtein 알고리즘 속성, 중복 차단 invariant, Soft Delete invariant |
| NFR-RES-TEST-03 | Oracle 테스트 | Levenshtein 최적화 구현 vs 단순 재귀 비교 |

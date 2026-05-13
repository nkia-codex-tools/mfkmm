# NFR Requirements - History Service

## 1. Event Processing

| ID | Requirement | Detail |
|---|---|---|
| NFR-HIST-EVT-01 | 메시지 전달 | At-least-once (RabbitMQ manual ack) |
| NFR-HIST-EVT-02 | 중복 처리 | Idempotent consumer (sourceEvent로 deduplication) |
| NFR-HIST-EVT-03 | 처리 실패 | 재시도 3회 (exponential backoff) → DLQ |
| NFR-HIST-EVT-04 | 처리량 | 사내 도구 (이벤트 빈도 낮음, 특별 기준 없음) |

## 2. Data Retention

| ID | Requirement | Detail |
|---|---|---|
| NFR-HIST-RET-01 | SEARCH 이력 | 6개월(180일) 보존 후 자동 삭제 |
| NFR-HIST-RET-02 | 기타 이력 | 무기한 보존 |
| NFR-HIST-RET-03 | 삭제된 사용자 이력 | 비동기 batch 삭제 (markedForDeletion 플래그) |
| NFR-HIST-RET-04 | 스케줄 | SEARCH 삭제: 04:00, User 삭제 batch: 04:30 |

## 3. Performance

| ID | Requirement | Detail |
|---|---|---|
| NFR-HIST-PERF-01 | 조회 응답 | 특별 기준 없음 |
| NFR-HIST-PERF-02 | 페이지네이션 | 기본 20건, 최대 100건 |
| NFR-HIST-PERF-03 | 인덱스 | workLogType, userId, performedAt, sourceEvent(unique) |

## 4. Security

| ID | Requirement | Detail |
|---|---|---|
| NFR-HIST-SEC-01 | 전체 조회 | ADMIN/ROOT_ADMIN만 |
| NFR-HIST-SEC-02 | 본인 조회 | 모든 인증 사용자 (userId 필터 강제) |
| NFR-HIST-SEC-03 | EXPORT | ADMIN만 |

## 5. Testing

| ID | Requirement | Detail |
|---|---|---|
| NFR-HIST-TEST-01 | PBT | jqwik — idempotent processing, self query isolation |
| NFR-HIST-TEST-02 | 단위 테스트 | 이벤트 매핑, 조회 권한, 보존 정책 |

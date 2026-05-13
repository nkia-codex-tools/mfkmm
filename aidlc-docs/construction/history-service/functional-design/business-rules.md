# Business Rules - History Service

## BR-HIST: History Recording Rules

### BR-HIST-01: Event Consumption
- 모든 이벤트는 RabbitMQ 큐를 통해 수신
- 이벤트 수신 시 WorkLog 엔티티로 변환 후 저장
- sourceEvent(eventId)로 중복 처리 방지 (idempotent consumer)
- 동일 eventId가 이미 존재하면 무시 (at-least-once 대응)

### BR-HIST-02: Event-to-WorkLog Mapping
- 각 이벤트 유형에 따라 WorkLogType 결정
- payload에서 필요한 필드 추출 (resourceId, previousValue, newValue 등)
- performedAt = 이벤트 timestamp (이벤트 발생 시각 기준)
- success = true (이벤트가 발행되었다는 것은 작업이 성공했다는 의미)

### BR-HIST-03: User Deletion Handling
- UserDeleted 이벤트 수신 시:
  1. WorkLog에 USER_DELETED 이력 기록
  2. 해당 userId의 모든 기존 이력을 비동기 batch 삭제 표시
  3. Batch 삭제 스케줄러가 실제 삭제 수행 (즉시 삭제 아님)

---

## BR-QUERY: History Query Rules

### BR-QUERY-01: Admin Query (전체)
- ADMIN / ROOT_ADMIN만 전체 이력 조회 가능
- 필터: workLogType, userId, resourceKey, 기간
- 페이지네이션 및 정렬 지원
- previousValue / newValue 단순 텍스트로 표시 (diff)

### BR-QUERY-02: Self Query (본인)
- 모든 인증 사용자가 본인 이력 조회 가능
- userId = 요청자 userId로 고정 필터
- 타인의 이력은 접근 불가

### BR-QUERY-03: Export
- ADMIN만 이력 데이터 EXPORT 가능 (감사 목적)
- JSON 형식으로 다운로드

---

## BR-RETENTION: Data Retention Rules

### BR-RETENTION-01: Search History
- WorkLogType=SEARCH인 이력은 6개월(180일) 보존
- 매일 새벽 4시 스케줄러가 180일 경과 SEARCH 이력 삭제

### BR-RETENTION-02: Other History
- SEARCH 외 모든 이력은 무기한 보존

### BR-RETENTION-03: User Deletion Batch
- UserDeleted로 삭제 대상이 된 이력은 별도 플래그(markedForDeletion=true)
- 매일 새벽 4시 30분 batch 삭제 수행

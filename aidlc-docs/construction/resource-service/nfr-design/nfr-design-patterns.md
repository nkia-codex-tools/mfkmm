# NFR Design Patterns - Resource Service

## 1. Similarity Detection Optimization Pattern

**적용 대상**: 리소스 등록/수정 시 유사도 탐지

**패턴**: Two-Phase Filter
```
Phase 1 (DB 레벨): MongoDB regex로 부분 일치 후보 추출
    → resourceKey LIKE "%input%" OR content LIKE "%input%"
    → deleted=false 필터
    → 결과: 후보 목록 (수십~수백 건)

Phase 2 (App 레벨): Java에서 Levenshtein 계산
    → 각 후보에 대해 편집 거리 계산
    → threshold ≤ 3 필터
    → 거리 오름차순 정렬
    → 최대 5개 반환
```

**성능 고려**:
- 1~10K 데이터에서 Phase 1 regex는 수십ms 이내
- Phase 2 Levenshtein은 O(m*n) per candidate, 후보 수십 건이면 무시 가능
- 인덱스: resourceKey에 대한 단일 필드 인덱스 활용

---

## 2. Soft Delete Pattern

**적용 대상**: 리소스 삭제

**패턴**: Flag + TTL Scheduler
```
Delete Request → Set deleted=true, deletedAt=now → Save
                                                      ↓
Search Query → WHERE deleted=false (항상)       ← 검색에서 제외
                                                      ↓
Scheduler (Daily 03:00) → WHERE deleted=true AND deletedAt < (now-30d) → Hard Delete
```

**구현**:
- 모든 조회/검색 쿼리에 `deleted: false` 조건 자동 포함
- MongoDB 쿼리에 compound index: `{ deleted: 1, resourceType: 1, resourceKey: 1 }`
- Hard Delete 스케줄: batch 1000건씩 삭제

---

## 3. Retry + DLQ Pattern (User Service와 동일)

**적용 대상**: 이벤트 발행 (ResourceCreated, ResourceUpdated, ResourceDeleted, ResourceSearched, SimilarityChoiceMade)

**패턴**:
```
Publish → Success → Done
       → Fail → Retry (1s, 2s, 4s, max 3)
              → All failed → DLQ (mkfmm.resource.dlq)
```

---

## 4. Unique Constraint Pattern

**적용 대상**: resourceKey 중복 방지

**패턴**: DB-Level Unique Index + Application-Level Check
```
Application: findByResourceKey(key) → exists? → reject
Database: unique index on { resourceKey: 1 } where deleted=false
    → 레이스 컨디션 방지 (동시 등록 시 DB가 최종 방어)
```

**MongoDB Partial Index**:
```javascript
db.resources.createIndex(
    { "resourceKey": 1 },
    { unique: true, partialFilterExpression: { deleted: false } }
)
```
- deleted=true인 리소스는 unique 제약에서 제외 (같은 key로 재등록 가능)

---

## 5. Event Volume Management

**적용 대상**: ResourceSearched 이벤트 (검색마다 발행되므로 빈도 높음)

**패턴**: Fire-and-Forget with Async
- 검색 이벤트는 비동기 발행 (검색 응답 속도에 영향 없도록)
- `@Async` + RabbitTemplate으로 non-blocking 발행
- 검색 이벤트 실패 시 로그만 남기고 무시 (이력 누락 허용 — 검색 이력은 6개월 후 삭제될 데이터)

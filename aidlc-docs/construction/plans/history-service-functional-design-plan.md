# Functional Design Plan - History Service

## Unit Context
- **Unit**: History Service (Phase 3)
- **Stories**: US-7.1 (작업 이력 조회 - 전체), US-7.2 (본인 작업 이력 조회)
- **Responsibility**: 이벤트 수신 → 작업 이력 기록, 이력 조회, 6개월 검색 이력 자동 삭제
- **Events Consumed**: ResourceCreated, ResourceUpdated, ResourceDeleted, ResourceSearched, ImportCompleted, ExportCompleted, DeployCompleted, UserCreated, UserDeleted, PermissionChanged, UserLoggedIn, UserLoggedOut, AccountLocked, AccountUnlocked, SimilarityChoiceMade

## Planning Questions

---

## Question 1
이력 조회 시 변경 전/후 비교(diff)를 어떻게 보여주시겠습니까? (MVP 범위)

A) 단순 텍스트 비교 — previousValue / newValue를 그대로 표시
B) MVP 이후로 미룸 — diff 표시 없이 이력 목록만 제공
X) Other (please describe after [Answer]: tag below)

[Answer]: A

## Question 2
사용자 삭제 시(UserDeleted 이벤트 수신) 해당 사용자의 이력 처리는?

A) 즉시 삭제 (이벤트 수신 시 해당 userId의 모든 이력 삭제)
B) 비동기 삭제 (이벤트 수신 후 batch로 삭제)
X) Other (please describe after [Answer]: tag below)

[Answer]: B

---

## Functional Design Execution Plan

- [x] Step 1: Domain Entities 설계 (WorkLog, WorkLogType)
- [x] Step 2: Business Rules 정의 (이벤트 수신 규칙, 조회 권한, 보존 정책)
- [x] Step 3: Business Logic Model (이벤트 수신 흐름, 조회 흐름, 삭제 흐름)
- [x] Step 4: PBT-01 Testable Properties 식별

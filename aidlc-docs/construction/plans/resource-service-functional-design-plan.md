# Functional Design Plan - Resource Service

## Unit Context
- **Unit**: Resource Service (Phase 4)
- **Stories**: US-3.1 (리소스 검색), US-4.1 (리소스 등록 - 중복/유사), US-4.2 (리소스 변경), US-4.3 (리소스 삭제)
- **Responsibility**: 리소스(메시지키/기능ID/메뉴ID) CRUD, 검색, 중복 검사, 유사도 탐지(문자열 일치 + Levenshtein), Soft Delete + 30일 Hard Delete
- **Events Published**: ResourceCreated, ResourceUpdated, ResourceDeleted, ResourceSearched, SimilarityChoiceMade

## Planning Questions

아래 질문들에 답변해 주세요.

---

## Question 1
유사도 탐지 시 Levenshtein 거리 임계값(threshold)은 어느 정도로 설정하시겠습니까? (낮을수록 더 유사해야 후보로 표시)

A) 편집 거리 3 이하 (매우 유사한 것만)
B) 편집 거리 5 이하 (적당히 유사한 것)
C) 문자열 길이 대비 비율 (예: 편집 거리 / 길이 < 0.3이면 유사)
D) 설정 가능하게 (application.yml에서 threshold 조정)
X) Other (please describe after [Answer]: tag below)

[Answer]: A

## Question 2
유사 후보를 최대 몇 개까지 표시하시겠습니까?

A) 최대 5개
B) 최대 10개
C) 전부 표시 (threshold 이하 모두)
X) Other (please describe after [Answer]: tag below)

[Answer]: A

## Question 3
Soft Delete된 리소스의 30일 Hard Delete는 어떤 기준으로 동작합니까?

A) 삭제 시점(deletedAt) 기준 30일 경과 시 제거
B) 마지막 수정 시점(updatedAt) 기준 30일 경과 시 제거
X) Other (please describe after [Answer]: tag below)

[Answer]: A

## Question 4
리소스 검색 시 Soft Delete된 항목의 처리는?

A) 일반 검색에서 완전 제외 (deleted=true인 것은 보이지 않음)
B) 별도 "삭제된 항목" 필터로 조회 가능
C) A + B 둘 다 (기본은 제외, 필터로 삭제 항목만 볼 수 있음)
X) Other (please describe after [Answer]: tag below)

[Answer]: A

---

## Functional Design Execution Plan

- [x] Step 1: Domain Entities 설계 (Resource, ResourceType, SimilarityResult)
- [x] Step 2: Business Rules 정의 (CRUD, 중복 검사, 유사도, Soft/Hard Delete)
- [x] Step 3: Business Logic Model (등록 흐름 - 중복/유사 포함, 검색, 삭제 흐름)
- [x] Step 4: Levenshtein 알고리즘 설계
- [x] Step 5: PBT-01 Testable Properties 식별

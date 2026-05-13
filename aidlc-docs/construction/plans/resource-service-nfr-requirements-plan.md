# NFR Requirements Plan - Resource Service

## Unit Context
- **Unit**: Resource Service
- **Key Concerns**: 유사도 탐지 성능, 검색 응답 시간, Soft Delete 데이터 증가, 이벤트 발행량

## NFR Questions

---

## Question 1
유사도 탐지(Levenshtein) 시 1차 후보 추출 범위는?

A) 전체 리소스 대상 (데이터 적으므로 OK — 수천 건 이하 예상)
B) 동일 resourceType 내에서만 비교 (범위 제한)
C) 최근 등록된 리소스 우선 (예: 최근 1년)
X) Other (please describe after [Answer]: tag below)

[Answer]: A

## Question 2
Resource Service의 예상 데이터 규모는?

A) 소규모 (1,000건 이하)
B) 중규모 (1,000~10,000건)
C) 대규모 (10,000건 이상)
X) Other (please describe after [Answer]: tag below)

[Answer]: B

---

## NFR Execution Plan

- [x] Step 1: NFR Requirements 문서 생성
- [x] Step 2: Tech Stack Decisions 문서 생성

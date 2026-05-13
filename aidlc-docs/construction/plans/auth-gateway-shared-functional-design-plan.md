# Functional Design Plan - Auth Service + API Gateway + Shared Library

## Unit Context
- **Unit**: Auth Service + API Gateway + Shared Library (Phase 1)
- **Stories**: US-1.1 (로그인), US-1.2 (계정 잠금), US-1.3 (로그아웃), US-2.4 (Root Admin 초기화), US-7.3 (로그인 이력)
- **Responsibility**: 인증, JWT 발급/갱신, 계정 잠금/해제, 로그인 이력, 라우팅, JWT 검증

## Planning Questions

아래 질문들에 답변해 주세요. 각 질문의 [Answer]: 태그 뒤에 선택한 옵션 문자를 기입해 주시면 됩니다.

---

## Question 1
JWT 토큰 구성은 어떻게 하시겠습니까?

A) Access Token만 사용 (30분 만료, 만료 시 재로그인)
B) Access Token (15분) + Refresh Token (7일) 조합 (자동 갱신)
C) Access Token (30분) + Refresh Token (24시간) 조합
X) Other (please describe after [Answer]: tag below)

[Answer]: B

## Question 2
계정 잠금 해제 시 실패 횟수 초기화 정책은?

A) 잠금 해제 시 실패 횟수 0으로 초기화
B) 잠금 해제 후 첫 성공 로그인 시 초기화
C) 잠금 해제 시 실패 횟수 유지 (다시 3회 실패하면 즉시 잠금)
X) Other (please describe after [Answer]: tag below)

[Answer]: B

## Question 3
로그인 성공 시 실패 횟수 초기화는?

A) 로그인 성공 시 즉시 실패 횟수 0으로 초기화
B) 로그인 성공과 관계없이 일정 시간(예: 1시간) 후 자동 초기화
X) Other (please describe after [Answer]: tag below)

[Answer]: A

## Question 4
API Gateway의 Rate Limiting 정책은?

A) 적용하지 않음 (사내 도구, 10~50명 규모)
B) IP 기반 간단한 제한 (예: 분당 100회)
C) 사용자 기반 제한 (인증된 사용자별 분당 요청 수 제한)
X) Other (please describe after [Answer]: tag below)

[Answer]: A

## Question 5
비밀번호 변경 기능을 Auth Service에 포함하시겠습니까? (MVP 범위 확인)

A) 포함 — 본인 비밀번호 변경 기능 (현재 비밀번호 확인 후 변경)
B) 포함 — 본인 변경 + 관리자가 타인 비밀번호 초기화
C) 미포함 — MVP 이후로 미룸
X) Other (please describe after [Answer]: tag below)

[Answer]: B

---

## Functional Design Execution Plan

- [x] Step 1: Domain Entities 설계 (Session, LoginHistory, AccountLock, JWT Claims)
- [x] Step 2: Business Rules 정의 (로그인 규칙, 잠금 규칙, JWT 규칙)
- [x] Step 3: Business Logic Model (인증 흐름, 잠금 흐름, 토큰 관리 흐름)
- [x] Step 4: Shared Library 스키마 정의 (이벤트, DTO, JWT 유틸)
- [x] Step 5: API Gateway 라우팅 및 필터 규칙 정의
- [x] Step 6: PBT-01 Testable Properties 식별

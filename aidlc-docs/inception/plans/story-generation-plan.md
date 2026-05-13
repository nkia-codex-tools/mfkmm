# Story Generation Plan - MKFMM

## Planning Questions

아래 질문들에 답변해 주세요. 각 질문의 [Answer]: 태그 뒤에 선택한 옵션 문자를 기입해 주시면 됩니다.

---

## Question 1
User Story 분해(breakdown) 접근 방식은 어떤 것을 선호하시나요?

A) User Journey-Based — 사용자 워크플로우 흐름을 따라 스토리 구성 (로그인 → 검색 → 등록 → ...)
B) Feature-Based — 시스템 기능 단위로 스토리 구성 (인증, 리소스 관리, IMPORT/EXPORT, ...)
C) Persona-Based — 사용자 유형별로 스토리 그룹화 (Admin 스토리, Write 사용자 스토리, ...)
D) Epic-Based — 대분류(Epic) → 하위 스토리 계층 구조
X) Other (please describe after [Answer]: tag below)

[Answer]: A

## Question 2
User Story의 세부 수준(granularity)은 어느 정도를 원하시나요?

A) 높은 수준(High-level) — Epic/대기능 단위 (예: "관리자로서 사용자를 관리할 수 있다")
B) 중간 수준(Medium) — 기능별 세분화 (예: "관리자로서 사용자를 등록할 수 있다")
C) 세밀한 수준(Fine-grained) — 동작 단위 세분화 (예: "관리자로서 사용자 등록 시 중복 ID를 검증받을 수 있다")
X) Other (please describe after [Answer]: tag below)

[Answer]: A

## Question 3
Acceptance Criteria(인수 기준) 형식은 어떤 것을 사용하시겠습니까?

A) Given-When-Then (BDD 스타일: 주어진 조건-행동-결과)
B) Checklist 형식 (체크리스트로 검증 항목 나열)
C) Both (중요 스토리는 Given-When-Then, 단순 스토리는 Checklist)
X) Other (please describe after [Answer]: tag below)

[Answer]: C

## Question 4
MVP 범위 내 스토리 우선순위 표기 방식은?

A) MoSCoW (Must/Should/Could/Won't)
B) High/Medium/Low 단순 분류
C) 순차 번호 (구현 순서대로 1, 2, 3...)
D) 우선순위 없이 동일 레벨로 나열
X) Other (please describe after [Answer]: tag below)

[Answer]: A

## Question 5
에러/예외 시나리오를 별도 스토리로 작성하시겠습니까?

A) Yes — 주요 에러 시나리오를 별도 스토리로 작성 (예: "잠긴 계정으로 로그인 시도 시...")
B) No — 에러 시나리오는 해당 기능 스토리의 Acceptance Criteria에 포함
C) Hybrid — 복잡한 에러는 별도 스토리, 단순 에러는 AC에 포함
X) Other (please describe after [Answer]: tag below)

[Answer]: C

---

## Story Generation Execution Plan

Story plan 승인 후 아래 순서대로 실행합니다:

- [x] Step 1: Personas 정의 (4가지 사용자 유형 기반)
- [x] Step 2: Epic 구조 정의
- [x] Step 3: 인증/세션 관리 스토리 작성
- [x] Step 4: 사용자/권한 관리 스토리 작성
- [x] Step 5: 리소스 검색/조회 스토리 작성
- [x] Step 6: 리소스 등록/변경/삭제 스토리 작성 (중복확인/유사제안 포함)
- [x] Step 7: IMPORT/EXPORT 스토리 작성
- [x] Step 8: 배포 관리 스토리 작성
- [x] Step 9: 이력 관리 스토리 작성
- [x] Step 10: Persona-Story 매핑 검증
- [x] Step 11: INVEST 기준 검증

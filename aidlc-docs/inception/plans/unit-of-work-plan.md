# Unit of Work Plan - MKFMM

## Planning Questions

아래 질문들에 답변해 주세요. 각 질문의 [Answer]: 태그 뒤에 선택한 옵션 문자를 기입해 주시면 됩니다.

---

## Question 1
배포 모델은 어떻게 하시겠습니까?

A) Monolith — 단일 Spring Boot 애플리케이션 (모듈은 패키지로 분리)
B) Modular Monolith — 단일 배포 단위이지만 모듈 경계를 엄격히 분리 (향후 마이크로서비스 전환 가능)
C) Microservices — 각 도메인을 독립 서비스로 분리 배포
X) Other (please describe after [Answer]: tag below)

[Answer]: C

## Question 2
개발 단위(Unit of Work)를 어떻게 나누길 원하시나요? (구현 순서 결정 용도)

A) 도메인 기능별 — Auth, Resource, DataIO, Deploy, History를 각각 하나의 유닛으로
B) 계층별 — Backend 전체를 하나의 유닛, Frontend 전체를 하나의 유닛으로
C) 기능 흐름별 — 핵심 흐름(인증→리소스CRUD→이력)을 첫 유닛, 부가 흐름(IMPORT/EXPORT→배포)을 다음 유닛
D) Full-Stack Slice — 각 기능을 FE+BE+DB 수직으로 묶어 하나의 유닛으로
X) Other (please describe after [Answer]: tag below)

[Answer]: A

## Question 3
유닛 간 구현 순서(우선순위)는 어떻게 결정하시겠습니까?

A) 의존성 기반 — 다른 유닛이 의존하는 기반 유닛(Auth, History)부터 먼저
B) 비즈니스 가치 기반 — 핵심 기능(리소스 관리)부터 먼저
C) 리스크 기반 — 기술적으로 복잡하거나 불확실한 유닛(유사도 탐지)부터 먼저
X) Other (please describe after [Answer]: tag below)

[Answer]: A

## Question 4
프로젝트 디렉토리 구조는 어떤 형태를 선호하시나요?

A) 단일 루트 — backend/, frontend/ 두 디렉토리로 분리
B) 모노레포 — packages/backend, packages/frontend 형태
C) 완전 분리 — backend과 frontend를 별도 프로젝트로 (별도 빌드)
X) Other (please describe after [Answer]: tag below)

[Answer]: A

---

## Unit Generation Execution Plan

질문 답변 및 승인 후 아래 순서대로 실행합니다:

- [x] Step 1: Unit of Work 정의 (unit-of-work.md)
- [x] Step 2: Unit 간 의존성 매트릭스 (unit-of-work-dependency.md)
- [x] Step 3: User Story → Unit 매핑 (unit-of-work-story-map.md)
- [x] Step 4: 코드 조직 전략 문서화
- [x] Step 5: Unit 경계 및 완전성 검증

# Application Design Plan - MKFMM

## Design Questions

아래 질문들에 답변해 주세요. 각 질문의 [Answer]: 태그 뒤에 선택한 옵션 문자를 기입해 주시면 됩니다.

---

## Question 1
백엔드 아키텍처 패턴으로 어떤 구조를 사용하시겠습니까?

A) Layered Architecture — Controller → Service → Repository 3계층 구조
B) Hexagonal Architecture (Ports & Adapters) — 도메인 중심, 외부 의존성 분리
C) Clean Architecture — UseCase 중심, 의존성 역전 적용
X) Other (please describe after [Answer]: tag below)

[Answer]: B

## Question 2
프론트엔드 상태 관리 방식은?

A) React Context + useReducer (내장 기능, 경량)
B) Zustand (간결한 API, 보일러플레이트 최소)
C) Redux Toolkit (대규모 상태, 미들웨어 지원)
D) TanStack Query (서버 상태 중심) + 로컬 상태는 Context
X) Other (please describe after [Answer]: tag below)

[Answer]: A

## Question 3
프론트엔드-백엔드 간 API 통신 방식은?

A) REST API (JSON) — 전통적 CRUD 매핑
B) REST API + OpenAPI Spec (Swagger) — 명세 기반 개발, 코드 생성 가능
C) GraphQL — 유연한 쿼리, 클라이언트 주도 데이터 요청
X) Other (please describe after [Answer]: tag below)

[Answer]: B

## Question 4
유사도 탐지 모듈의 위치는?

A) 백엔드 서비스 레이어에서 직접 계산 (Java 코드)
B) MongoDB 텍스트 인덱스 + 애플리케이션 레벨 Levenshtein 조합
C) 별도 유틸리티 서비스로 분리 (재사용 가능한 독립 모듈)
X) Other (please describe after [Answer]: tag below)

[Answer]: A

## Question 5
IMPORT/EXPORT 파일 처리 방식은?

A) 동기 처리 — 요청 즉시 처리 후 응답 (소규모 파일, 5MB 제한이므로 적합)
B) 비동기 처리 — 작업 큐에 넣고 완료 시 알림 (대용량 대비)
C) 하이브리드 — 작은 파일은 동기, 큰 파일은 비동기 (기준 설정)
X) Other (please describe after [Answer]: tag below)

[Answer]: C

## Question 6
세션 저장소는 어디에 두시겠습니까?

A) MongoDB에 세션 저장 (Spring Session + MongoDB)
B) In-memory (서버 메모리, 단일 인스턴스에 적합)
C) Redis (별도 세션 저장소, 수평 확장 대비)
X) Other (please describe after [Answer]: tag below)

[Answer]: A

---

## Design Artifact Generation Plan

질문 답변 후 아래 순서대로 설계 문서를 생성합니다:

- [x] Step 1: Component 식별 및 책임 정의 (components.md)
- [x] Step 2: Component Method 시그니처 정의 (component-methods.md)
- [x] Step 3: Service Layer 설계 (services.md)
- [x] Step 4: Component 의존성 관계 정의 (component-dependency.md)
- [x] Step 5: 통합 설계 문서 (application-design.md)
- [x] Step 6: 설계 완전성 검증

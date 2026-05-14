# Application Design Plan

## Design Questions

아래 질문에 답변해 주세요. 각 [Answer]: 태그 뒤에 선택한 옵션 문자를 입력해 주세요.

### Component Organization

## Question 1
백엔드 패키지 구조를 어떤 방식으로 조직하시겠습니까?

A) 계층형 구조 (controller/service/repository 패키지로 분리)
B) 도메인 기반 구조 (auth/resource/version/history 도메인별 패키지, 각 도메인 내에 controller/service/repository)
C) 혼합 구조 (핵심 도메인은 도메인 기반, 공통 모듈은 계층형)
X) Other (please describe after [Answer]: tag below)

[Answer]: 뭐를 추천해? 그리고 각 장점알려줘

## Question 2
프론트엔드 상태 관리 라이브러리로 어떤 것을 사용하시겠습니까?

A) Zustand (경량, 간단한 API)
B) Redux Toolkit (강력한 미들웨어, DevTools)
C) React Query (TanStack Query) + Context API (서버 상태 중심, 최소 클라이언트 상태)
D) Recoil (atomic 상태 관리)
X) Other (please describe after [Answer]: tag below)

[Answer]: A

## Question 3
API 통신 방식으로 어떤 것을 사용하시겠습니까?

A) REST API (Spring Boot Controller + React Axios/Fetch)
B) REST API + React Query (캐싱, 자동 재요청, 낙관적 업데이트)
X) Other (please describe after [Answer]: tag below)

[Answer]: A

## Question 4
리소스 3종(기능, 메뉴, 리소스키)의 백엔드 설계 접근 방식은?

A) 각 리소스별 독립 엔티티/테이블 + 독립 API 엔드포인트
B) 공통 베이스 엔티티(상속) + 공통 CRUD 로직 + 리소스 타입별 분기
C) 제네릭 리소스 관리 (단일 테이블 + JSON 컬럼으로 유연하게)
X) Other (please describe after [Answer]: tag below)

[Answer]: 뭐를 추천해? 그리고 각 특징

## Question 5
변경 이력과 버전 관리의 데이터 저장 방식은?

A) 변경 이력: 필드 레벨 diff 저장 (각 필드 변경마다 레코드) / 버전 태그: 전체 스냅샷 JSON 저장
B) 변경 이력: 행 레벨 JSON diff 저장 (행 전체 before/after) / 버전 태그: 전체 스냅샷 JSON 저장
C) Event Sourcing 방식 (모든 변경을 이벤트로 저장, 현재 상태는 이벤트 재생으로 구성)
X) Other (please describe after [Answer]: tag below)

[Answer]: 추천하는걸로 해줘

## Question 6
프론트엔드 라우팅과 레이아웃 구조는?

A) Single Page Application (React Router, 탭은 클라이언트 사이드 라우팅)
B) 단일 페이지 (탭 전환은 컴포넌트 state로 관리, URL 변경 없음)
X) Other (please describe after [Answer]: tag below)

[Answer]: 추천하는걸로 해줘

---

## Design Plan Checklist

위 질문 답변 후 아래 산출물을 생성합니다:

- [ ] Generate components.md - 컴포넌트 정의 및 책임
- [ ] Generate component-methods.md - 메서드 시그니처
- [ ] Generate services.md - 서비스 정의 및 오케스트레이션
- [ ] Generate component-dependency.md - 의존성 관계
- [ ] Generate application-design.md - 통합 설계 문서
- [ ] Validate design completeness and consistency

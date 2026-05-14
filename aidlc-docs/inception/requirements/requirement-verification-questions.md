# Requirements Verification Questions

요구사항을 명확히 하기 위해 아래 질문에 답변해 주세요.
각 질문의 [Answer]: 태그 뒤에 선택한 옵션의 문자를 입력해 주세요.

## Question 1
백엔드 기술 스택으로 어떤 것을 사용하시겠습니까?

A) Java (Spring Boot)
B) Python (FastAPI / Django)
C) Node.js (Express / NestJS)
D) Go (Gin / Echo)
X) Other (please describe after [Answer]: tag below)

[Answer]: A

## Question 2
프론트엔드 기술 스택으로 어떤 것을 사용하시겠습니까?

A) React (TypeScript)
B) Vue.js (TypeScript)
C) Angular
D) Next.js (React + SSR)
X) Other (please describe after [Answer]: tag below)

[Answer]: A

## Question 3
데이터베이스로 어떤 것을 사용하시겠습니까?

A) PostgreSQL
B) MySQL / MariaDB
C) MongoDB
D) SQLite (경량 배포용)
X) Other (please describe after [Answer]: tag below)

[Answer]: D

## Question 4
배포 환경은 어떻게 되나요?

A) AWS (EC2, RDS, S3 등)
B) Docker / Docker Compose (자체 서버)
C) Kubernetes (EKS, GKE 등)
D) 로컬 개발 환경만 (배포는 나중에 결정)
X) Other (please describe after [Answer]: tag below)

[Answer]: EC2에 할건데, B로 할수있게 해줘. 한개의 머신에서 다 해결할 수 있도록

## Question 5
사용자 인증 방식은 어떻게 구현하시겠습니까?

A) JWT (JSON Web Token) 기반 인증
B) Session 기반 인증
C) OAuth2 + JWT 조합
X) Other (please describe after [Answer]: tag below)

[Answer]: A

## Question 6
예상 사용자 규모는 어느 정도입니까?

A) 소규모 (10명 이하 사내 팀)
B) 중규모 (10~50명 사내 사용자)
C) 대규모 (50명 이상)
X) Other (please describe after [Answer]: tag below)

[Answer]: B

## Question 7
리소스 데이터의 예상 규모는 어느 정도입니까? (각 탭당 행 수 기준)

A) 소규모 (1,000행 이하)
B) 중규모 (1,000~10,000행)
C) 대규모 (10,000~100,000행)
X) Other (please describe after [Answer]: tag below)

[Answer]: B

## Question 8
테이블 UI 컴포넌트로 특별히 선호하는 라이브러리가 있습니까?

A) AG Grid (고기능 상용/커뮤니티)
B) TanStack Table (React Table v8, 경량 헤드리스)
C) MUI DataGrid (Material UI 기반)
D) 프레임워크에 맞는 최적 선택에 위임
X) Other (please describe after [Answer]: tag below)

[Answer]: 추천하는걸로 해줘

## Question 9
버전 관리 정책에 대해 요구사항에 두 가지 대안이 제시되어 있습니다. 어떤 방식을 선택하시겠습니까?

A) 현재 설계 유지 - 관리자가 명시적으로 태깅한 버전 단위로만 롤백 가능 (추천)
B) 대안 1 - 모든 개별 변경마다 자동 버전 생성 및 개별 롤백 가능
X) Other (please describe after [Answer]: tag below)

[Answer]: A

## Question 10: Security Extensions
이 프로젝트에 보안 확장 규칙을 적용하시겠습니까?

A) Yes — 모든 SECURITY 규칙을 blocking constraint로 적용 (프로덕션급 애플리케이션 권장)
B) No — 모든 SECURITY 규칙 건너뛰기 (PoC, 프로토타입, 실험적 프로젝트에 적합)
X) Other (please describe after [Answer]: tag below)

[Answer]: A

## Question 11: Property-Based Testing Extension
이 프로젝트에 property-based testing (PBT) 규칙을 적용하시겠습니까?

A) Yes — 모든 PBT 규칙을 blocking constraint로 적용 (비즈니스 로직, 데이터 변환, 직렬화, 상태 컴포넌트가 있는 프로젝트 권장)
B) Partial — pure function과 직렬화 round-trip에만 PBT 규칙 적용 (제한적 알고리즘 복잡성에 적합)
C) No — 모든 PBT 규칙 건너뛰기 (단순 CRUD, UI-only, 얇은 통합 레이어에 적합)
X) Other (please describe after [Answer]: tag below)

[Answer]: A

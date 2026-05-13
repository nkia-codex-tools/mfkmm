# Requirements Verification Questions

아래 질문들에 답변해 주세요. 각 질문의 [Answer]: 태그 뒤에 선택한 옵션 문자를 기입해 주시면 됩니다.
제공된 옵션 중 적합한 것이 없으면 X) Other를 선택하고 설명을 추가해 주세요.

---

## Question 1
프론트엔드 기술 스택으로 무엇을 사용하시겠습니까?

A) React (JavaScript/TypeScript)
B) Vue.js (JavaScript/TypeScript)
C) Angular (TypeScript)
D) Next.js (React + SSR)
X) Other (please describe after [Answer]: tag below)

[Answer]: A

## Question 2
백엔드 기술 스택으로 무엇을 사용하시겠습니까?

A) Node.js (Express/Fastify) + TypeScript
B) Java (Spring Boot)
C) Python (FastAPI/Django)
D) Go (Gin/Echo)
X) Other (please describe after [Answer]: tag below)

[Answer]: B

## Question 3
데이터베이스로 무엇을 사용하시겠습니까?

A) PostgreSQL
B) MySQL/MariaDB
C) SQLite (경량 사내 도구용)
D) MongoDB (NoSQL)
X) Other (please describe after [Answer]: tag below)

[Answer]: D

## Question 4
배포 환경은 어디입니까?

A) 사내 온프레미스 서버
B) AWS 클라우드
C) Docker 컨테이너 기반 (환경 미정)
D) Kubernetes 클러스터
X) Other (please describe after [Answer]: tag below)

[Answer]: C

## Question 5
예상 동시 사용자 수는 어느 정도입니까?

A) 소규모 (10명 이하)
B) 중소규모 (10~50명)
C) 중규모 (50~200명)
D) 대규모 (200명 이상)
X) Other (please describe after [Answer]: tag below)

[Answer]: B

## Question 6
"유사 정보 제안" 기능의 유사도 탐지 방식으로 어떤 수준을 원하시나요? (constraints.md에서 AI 기반 의미 유사도는 제외됨)

A) 문자열 부분 일치 (LIKE 검색 기반, 간단한 구현)
B) Levenshtein 거리 등 편집 거리 기반 유사도 (형태적 유사)
C) N-gram 기반 유사도 (토큰 중첩 비율)
D) 위 방식 조합 (문자열 일치 + 편집 거리)
X) Other (please describe after [Answer]: tag below)

[Answer]: D

## Question 7
시스템 최상위 관리자의 초기 인증 정보(ID/비밀번호)는 어떻게 설정합니까?

A) 환경 변수로 주입 (배포 시 설정)
B) 초기 설정 파일(seed data)에 고정값 지정
C) 시스템 최초 실행 시 CLI로 입력
D) 데이터베이스 마이그레이션 스크립트에 포함
X) Other (please describe after [Answer]: tag below)

[Answer]: A

## Question 8
UI 디자인 스타일/프레임워크 선호도가 있습니까?

A) Material UI (MUI)
B) Ant Design
C) Tailwind CSS + Headless UI
D) Bootstrap 기반
X) Other (please describe after [Answer]: tag below)

[Answer]: C

## Question 9
IMPORT 시 파일 크기 제한은 어느 정도로 설정합니까?

A) 소용량 (5MB 이하)
B) 중용량 (5~20MB)
C) 대용량 (20~100MB)
D) 제한 없음 (서버 메모리 허용 범위 내)
X) Other (please describe after [Answer]: tag below)

[Answer]: A

## Question 10
세션 만료 시간은 어떻게 설정합니까?

A) 짧은 세션 (30분)
B) 표준 세션 (1시간)
C) 긴 세션 (4시간)
D) 업무 시간 기준 (8시간)
X) Other (please describe after [Answer]: tag below)

[Answer]: A

## Question 11
로그인 시도 제한 (브루트포스 방지) 정책은?

A) 5회 실패 시 15분 잠금
B) 5회 실패 시 30분 잠금
C) 10회 실패 시 1시간 잠금
D) 3회 실패 시 관리자 수동 해제 필요
X) Other (please describe after [Answer]: tag below)

[Answer]: D

## Question 12
리소스 삭제 정책은 어떻게 하시겠습니까?

A) Soft Delete (삭제 플래그만 설정, 데이터 보존)
B) Hard Delete (실제 데이터 삭제, 이력에만 기록 남김)
C) Soft Delete + 일정 기간 후 Hard Delete (예: 30일)
X) Other (please describe after [Answer]: tag below)

[Answer]: C

## Question 13
검색 이력도 모두 기록한다고 되어 있는데, 검색 이력의 보존 기간 제한이 필요합니까?

A) 무기한 보존
B) 1년 보존 후 자동 삭제
C) 6개월 보존 후 자동 삭제
D) 보존 기간 설정 기능은 MVP 이후로 미룸 (현재는 무기한)
X) Other (please describe after [Answer]: tag below)

[Answer]: C

---

## Extension Opt-In Questions

## Question 14: Security Extensions
이 프로젝트에 보안 확장(Security Baseline) 규칙을 적용하시겠습니까?

A) Yes — 모든 SECURITY 규칙을 blocking 제약으로 적용 (프로덕션급 애플리케이션에 권장)
B) No — SECURITY 규칙 생략 (PoC, 프로토타입, 실험적 프로젝트에 적합)
X) Other (please describe after [Answer]: tag below)

[Answer]: B

## Question 15: Property-Based Testing Extension
이 프로젝트에 Property-Based Testing(PBT) 규칙을 적용하시겠습니까?

A) Yes — 모든 PBT 규칙을 blocking 제약으로 적용 (비즈니스 로직, 데이터 변환, 직렬화, 상태 관리 컴포넌트가 있는 프로젝트에 권장)
B) Partial — 순수 함수와 직렬화 round-trip에 대해서만 PBT 규칙 적용 (알고리즘 복잡도가 제한적인 프로젝트에 적합)
C) No — PBT 규칙 생략 (단순 CRUD, UI 전용, 비즈니스 로직이 적은 프로젝트에 적합)
X) Other (please describe after [Answer]: tag below)

[Answer]: A

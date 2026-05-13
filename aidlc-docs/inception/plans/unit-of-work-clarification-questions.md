# Units Generation Clarification Questions

Q1에서 Microservices를 선택하셨습니다. 이전 Application Design에서 단일 Spring Boot + Hexagonal Architecture로 설계되었는데, 이를 조정해야 합니다.

---

## Clarification Question 1
Microservices를 선택하셨는데, 디렉토리 구조를 어떻게 하시겠습니까?

A) 모노레포 방식 — backend/ 안에 각 서비스 디렉토리 (예: backend/auth-service/, backend/resource-service/, ...) + frontend/ 하나
B) 각 서비스별 완전 분리 디렉토리 (예: services/auth/, services/resource/, ..., frontend/)
X) Other (please describe after [Answer]: tag below)

[Answer]: A

## Clarification Question 2
마이크로서비스 간 통신 방식은?

A) REST API (HTTP 동기 호출) — 서비스 간 직접 HTTP 호출
B) API Gateway + REST — 단일 진입점(Gateway)에서 각 서비스로 라우팅
C) 이벤트 기반 (메시지 큐) — 비동기 이벤트로 서비스 간 통신
X) Other (please describe after [Answer]: tag below)

[Answer]: C

## Clarification Question 3
세션 관리 방식 변경이 필요합니다. 마이크로서비스에서는 단일 서비스 내 세션이 불가합니다.

A) API Gateway에서 세션 관리 후 각 서비스에 인증 정보 전달 (JWT 토큰 기반)
B) 공유 세션 저장소 (MongoDB) + 각 서비스에서 세션 검증
C) Auth 서비스가 토큰 발급, 다른 서비스는 토큰 검증만 수행
X) Other (please describe after [Answer]: tag below)

[Answer]: A

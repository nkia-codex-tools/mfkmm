# NFR Requirements Plan - Auth Service + API Gateway + Shared Library

## Unit Context
- **Unit**: Auth Service + API Gateway + Shared Library
- **Key Concerns**: JWT 보안, 인증 성능, 메시지 큐 신뢰성, Gateway 가용성

## NFR Questions

아래 질문들에 답변해 주세요.

---

## Question 1
JWT 서명 알고리즘은?

A) HS256 (대칭키, 단순 — 모든 서비스가 동일 secret 공유)
B) RS256 (비대칭키 — Auth가 private key로 서명, 다른 서비스는 public key로 검증)
X) Other (please describe after [Answer]: tag below)

[Answer]: 

## Question 2
로그인 API 응답 시간 목표는?

A) 500ms 이하 (일반적 웹 기준)
B) 1초 이하 (bcrypt 연산 고려, 여유 있게)
C) 특별한 기준 없음 (사내 도구이므로 합리적 범위면 OK)
X) Other (please describe after [Answer]: tag below)

[Answer]: 

## Question 3
메시지 큐(RabbitMQ) 메시지 전달 보장 수준은?

A) At-most-once (유실 허용, 단순) — 이력 일부 누락 감수
B) At-least-once (중복 허용, 유실 방지) — 이력 서비스에서 중복 제거 처리
C) Exactly-once (정확히 한 번) — 복잡하지만 완벽한 이력 보장
X) Other (please describe after [Answer]: tag below)

[Answer]: 

## Question 4
서비스 장애 시 가용성 전략은?

A) 단순 재시작 (Docker restart policy: on-failure)
B) Health check + 자동 재시작 (Docker healthcheck)
C) 별도 가용성 전략 불필요 (사내 도구, 업무 시간 외 다운타임 허용)
X) Other (please describe after [Answer]: tag below)

[Answer]: 

## Question 5
로그 관리 방식은?

A) 각 서비스 stdout/stderr → Docker logs로 확인 (단순)
B) 구조화된 JSON 로그 → 중앙 집중 로그 수집 (ELK/Loki 등)
C) 구조화된 JSON 로그 → 파일 저장 (중앙 집중 미적용)
X) Other (please describe after [Answer]: tag below)

[Answer]: 

---

## NFR Execution Plan

- [ ] Step 1: NFR Requirements 문서 생성
- [ ] Step 2: Tech Stack Decisions 문서 생성
- [ ] Step 3: PBT-09 Framework 확인 (jqwik for Java)

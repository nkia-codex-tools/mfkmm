# NFR Design Plan - User Service

## Unit Context
- **Unit**: User Service
- **NFR Key Points**: 기본 검증, 2년 AuditLog 보존, 재시도 기반 eventually consistent, DLQ

## NFR Design Questions

아래 질문들에 답변해 주세요.

---

## Question 1
Dead Letter Queue(DLQ)에 쌓인 실패 메시지 처리 방식은?

A) 로그로만 기록 — 관리자가 수동으로 확인/재처리
B) 관리 화면 제공 — Admin UI에서 DLQ 메시지 확인 및 재처리 버튼
C) 알림 없이 보관만 — 나중에 필요 시 수동 확인 (MVP 범위 최소화)
X) Other (please describe after [Answer]: tag below)

[Answer]: B

## Question 2
AuditLog 2년 자동 삭제 스케줄 실행 주기는?

A) 매일 1회 (새벽 시간대)
B) 매주 1회
C) 매월 1회
X) Other (please describe after [Answer]: tag below)

[Answer]: A

---

## NFR Design Execution Plan

- [x] Step 1: NFR Design Patterns 문서 생성 (재시도, DLQ, 검증, 보존 정책)
- [x] Step 2: Logical Components 문서 생성 (RabbitMQ 구성, 스케줄러)

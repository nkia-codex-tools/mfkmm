# NFR Requirements Plan - User Service

## Unit Context
- **Unit**: User Service
- **Key Concerns**: 사용자 데이터 보안, 감사 로그 무결성, API 성능, 이벤트 신뢰성

## NFR Questions

아래 질문들에 답변해 주세요.

---

## Question 1
사용자 데이터 검증 수준은?

A) 기본 — 필수 필드 null 체크 + userId 형식(영숫자) 정도만
B) 표준 — 기본 + email 형식 검증 + 문자열 길이 제한
C) 엄격 — 표준 + userId 패턴(영문+숫자, 4~20자) + email 도메인 제한(사내 도메인만) + 비밀번호 복잡도(최소 8자, 영문+숫자)
X) Other (please describe after [Answer]: tag below)

[Answer]: A

## Question 2
감사 로그(AuditLog) 보존 기간은?

A) 무기한 보존 (삭제하지 않음)
B) 1년 보존 후 아카이빙
C) 2년 보존 후 삭제
X) Other (please describe after [Answer]: tag below)

[Answer]: C

## Question 3
사용자 삭제 시 관련 데이터 정리(이벤트 발행 후 타 서비스 삭제)의 실패 처리는?

A) Best-effort — 이벤트 발행 후 타 서비스 삭제 실패는 무시 (로그만 남김)
B) Saga 패턴 — 타 서비스 삭제 실패 시 사용자 삭제 롤백
C) 재시도 — 이벤트 재발행으로 eventually consistent 보장
X) Other (please describe after [Answer]: tag below)

[Answer]: C

## Question 4
User Service API 응답 시간 목표는?

A) 200ms 이하 (빠른 응답)
B) 500ms 이하 (일반적 기준)
C) 특별한 기준 없음 (합리적 범위면 OK)
X) Other (please describe after [Answer]: tag below)

[Answer]: C

---

## NFR Execution Plan

- [x] Step 1: NFR Requirements 문서 생성
- [x] Step 2: Tech Stack Decisions 문서 생성 (User Service 특화)
- [x] Step 3: PBT-09 Framework 확인

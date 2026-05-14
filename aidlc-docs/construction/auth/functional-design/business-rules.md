# Business Rules - Unit 1: Auth

## BR-AUTH-01: 회원가입

| Rule | Description |
|------|-------------|
| BR-AUTH-01.1 | 이메일은 고유해야 함 (중복 가입 불가) |
| BR-AUTH-01.2 | 비밀번호 최소 8자 이상 |
| BR-AUTH-01.3 | 이메일 형식 유효성 검증 (RFC 5322) |
| BR-AUTH-01.4 | 가입 시 역할은 PENDING, 상태는 PENDING |
| BR-AUTH-01.5 | 비밀번호는 BCrypt(cost factor 10+)로 해싱하여 저장 |
| BR-AUTH-01.6 | 이름은 공백 불가, 최대 100자 |

## BR-AUTH-02: 로그인

| Rule | Description |
|------|-------------|
| BR-AUTH-02.1 | 이메일 + 비밀번호 인증 |
| BR-AUTH-02.2 | 인증 성공 시 Access Token (15분) + Refresh Token (7일) 발급 |
| BR-AUTH-02.3 | LOCKED 상태 사용자는 로그인 거부 (잠금 해제 시각까지) |
| BR-AUTH-02.4 | PENDING 상태 사용자도 로그인 가능 (단, 리소스 접근 불가) |
| BR-AUTH-02.5 | 로그인 성공 시 failed_login_attempts = 0 초기화 |

## BR-AUTH-03: Brute-Force Protection

| Rule | Description |
|------|-------------|
| BR-AUTH-03.1 | 연속 5회 로그인 실패 시 계정 잠금 |
| BR-AUTH-03.2 | 잠금 기간: 15분 (locked_until = now + 15분) |
| BR-AUTH-03.3 | 잠금 기간 만료 후 자동 해제 (다음 로그인 시도 시 확인) |
| BR-AUTH-03.4 | 로그인 실패 시 failed_login_attempts + 1 |
| BR-AUTH-03.5 | 잠금 중 로그인 시도 시 "계정이 잠겨있습니다. X분 후 재시도" 메시지 |

## BR-AUTH-04: 토큰 관리

| Rule | Description |
|------|-------------|
| BR-AUTH-04.1 | Access Token 만료 시 Refresh Token으로 재발급 |
| BR-AUTH-04.2 | Refresh Token 만료 시 재로그인 필요 |
| BR-AUTH-04.3 | 토큰 검증: 서명, 만료시간, 발급자 확인 |
| BR-AUTH-04.4 | 잘못된/만료된 토큰 → 401 Unauthorized |
| BR-AUTH-04.5 | JWT Secret Key는 환경 변수로 관리 (코드에 하드코딩 금지) |

## BR-AUTH-05: 권한 관리 (관리자)

| Rule | Description |
|------|-------------|
| BR-AUTH-05.1 | ADMIN만 사용자 역할 변경 가능 |
| BR-AUTH-05.2 | 변경 가능 역할: PENDING → READER/WRITER/ADMIN |
| BR-AUTH-05.3 | READER ↔ WRITER 간 변경 가능 |
| BR-AUTH-05.4 | 역할 변경 시 status가 PENDING이면 ACTIVE로 자동 변경 |
| BR-AUTH-05.5 | 자기 자신의 역할은 변경 불가 (실수 방지) |
| BR-AUTH-05.6 | 마지막 ADMIN은 역할 변경 불가 (최소 1명 ADMIN 유지) |

## BR-AUTH-06: 초기 관리자 계정

| Rule | Description |
|------|-------------|
| BR-AUTH-06.1 | 시스템 최초 구동 시 관리자 계정 자동 생성 |
| BR-AUTH-06.2 | 초기 관리자: email/password는 환경 변수로 설정 |
| BR-AUTH-06.3 | 초기 관리자 role=ADMIN, status=ACTIVE |
| BR-AUTH-06.4 | 이미 관리자가 존재하면 중복 생성하지 않음 |

## BR-AUTH-07: 접근 제어 (Authorization)

| Rule | Description |
|------|-------------|
| BR-AUTH-07.1 | 인증 없이 접근 가능: POST /api/auth/login, POST /api/auth/register, POST /api/auth/refresh |
| BR-AUTH-07.2 | PENDING 사용자: /api/auth/* 만 접근 가능, 리소스/관리자 API 접근 불가 |
| BR-AUTH-07.3 | READER: 리소스 조회(GET), export만 가능 |
| BR-AUTH-07.4 | WRITER: 리소스 CRUD, import/export 가능 |
| BR-AUTH-07.5 | ADMIN: 모든 API 접근 가능 (사용자 관리, 버전 태깅/롤백 포함) |
| BR-AUTH-07.6 | 권한 부족 시 403 Forbidden |

## BR-AUTH-08: Rate Limiting

| Rule | Description |
|------|-------------|
| BR-AUTH-08.1 | 로그인 엔드포인트: IP당 분당 10회 제한 |
| BR-AUTH-08.2 | 회원가입 엔드포인트: IP당 분당 5회 제한 |
| BR-AUTH-08.3 | 일반 API: 사용자당 분당 100회 제한 |
| BR-AUTH-08.4 | 제한 초과 시 429 Too Many Requests |

## Testable Properties (PBT-01)

| Property | Category | Description |
|----------|----------|-------------|
| Password hash round-trip | Round-trip | BCrypt.hash(pw) → BCrypt.verify(pw, hash) = true |
| JWT token round-trip | Round-trip | sign(claims) → verify(token) = original claims |
| Role hierarchy invariant | Invariant | ADMIN permissions ⊇ WRITER permissions ⊇ READER permissions |
| Login attempt idempotency | Invariant | N failed attempts = failed_login_attempts exactly N |
| Account lock threshold | Invariant | failed_login_attempts >= 5 ↔ status = LOCKED |

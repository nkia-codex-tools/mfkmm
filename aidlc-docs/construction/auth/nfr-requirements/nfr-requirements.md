# NFR Requirements - Unit 1: Auth

## 1. Performance Requirements

| ID | Requirement | Target |
|----|------------|--------|
| PERF-AUTH-01 | 로그인 API 응답 시간 | < 300ms (BCrypt 해싱 포함) |
| PERF-AUTH-02 | 토큰 검증 시간 | < 10ms |
| PERF-AUTH-03 | 사용자 목록 조회 (페이징) | < 200ms |
| PERF-AUTH-04 | Rate limit 체크 시간 | < 5ms |

## 2. Scalability Requirements

| ID | Requirement | Target |
|----|------------|--------|
| SCALE-AUTH-01 | 동시 사용자 수 | 최대 50명 (사내 서비스) |
| SCALE-AUTH-02 | 총 사용자 계정 수 | 최대 200개 (충분한 여유) |
| SCALE-AUTH-03 | 동시 로그인 요청 | 최대 10건/초 |

## 3. Availability Requirements

| ID | Requirement | Target |
|----|------------|--------|
| AVAIL-AUTH-01 | 서비스 가용성 | 영업시간 내 99% (사내 서비스) |
| AVAIL-AUTH-02 | 계획된 유지보수 | 업무 외 시간에 수행 |
| AVAIL-AUTH-03 | 장애 복구 시간 | < 1시간 (Docker restart) |

## 4. Security Requirements

| ID | Requirement | SECURITY Rule | Target |
|----|------------|---------------|--------|
| SEC-AUTH-01 | 비밀번호 해싱 | SECURITY-12 | BCrypt cost factor 10 |
| SEC-AUTH-02 | JWT 서명 알고리즘 | SECURITY-12 | HS256 (HMAC-SHA256) |
| SEC-AUTH-03 | Access Token 수명 | SECURITY-12 | 15분 |
| SEC-AUTH-04 | Refresh Token 수명 | SECURITY-12 | 7일 |
| SEC-AUTH-05 | Brute-force 방지 | SECURITY-12 | 5회 실패 시 15분 잠금 |
| SEC-AUTH-06 | Rate limiting | SECURITY-11 | 로그인 10/분, 가입 5/분, API 100/분 |
| SEC-AUTH-07 | HTTPS 강제 | SECURITY-01 | TLS 1.2+ (Nginx에서 처리) |
| SEC-AUTH-08 | Security Headers | SECURITY-04 | CSP, HSTS, X-Content-Type-Options, X-Frame-Options, Referrer-Policy |
| SEC-AUTH-09 | CORS 제한 | SECURITY-08 | 명시적 허용 origin만 |
| SEC-AUTH-10 | 토큰 서버 검증 | SECURITY-08 | 매 요청 서명+만료 검증 |
| SEC-AUTH-11 | 에러 메시지 | SECURITY-09 | 일반적 메시지 (내부 정보 노출 금지) |
| SEC-AUTH-12 | 하드코딩 금지 | SECURITY-12 | JWT Secret, 초기 관리자 정보 → 환경 변수 |
| SEC-AUTH-13 | 입력 검증 | SECURITY-05 | 모든 입력 필드 타입/길이/형식 검증 |
| SEC-AUTH-14 | 구조적 로깅 | SECURITY-03 | SLF4J + Logback JSON, 민감정보 제외 |
| SEC-AUTH-15 | 예외 처리 | SECURITY-15 | GlobalExceptionHandler, fail-closed |

## 5. Reliability Requirements

| ID | Requirement | Target |
|----|------------|--------|
| REL-AUTH-01 | 인증 실패 시 동작 | Fail-closed (접근 거부) |
| REL-AUTH-02 | DB 연결 실패 시 | 503 Service Unavailable 반환 |
| REL-AUTH-03 | 전역 예외 처리 | 모든 미처리 예외 catch + 로깅 + 안전한 응답 |
| REL-AUTH-04 | 데이터 영속성 | Docker volume으로 SQLite 파일 보존 |

## 6. Maintainability Requirements

| ID | Requirement | Target |
|----|------------|--------|
| MAINT-AUTH-01 | 테스트 커버리지 | 비즈니스 로직 80%+ |
| MAINT-AUTH-02 | PBT 테스트 | jqwik (Backend), fast-check (Frontend) |
| MAINT-AUTH-03 | 코드 구조 | 도메인 기반 패키지, 계층 분리 |
| MAINT-AUTH-04 | 로그 레벨 | DEBUG/INFO/WARN/ERROR 적절 사용 |
| MAINT-AUTH-05 | 설정 외부화 | application.yml + 환경 변수 |

## 7. Monitoring & Alerting

| ID | Requirement | SECURITY Rule | Target |
|----|------------|---------------|--------|
| MON-AUTH-01 | 로그인 실패 로깅 | SECURITY-14 | 연속 실패 시 WARN 로그 |
| MON-AUTH-02 | 계정 잠금 로깅 | SECURITY-14 | 잠금 발생 시 WARN 로그 |
| MON-AUTH-03 | Rate limit 초과 로깅 | SECURITY-14 | 초과 시 WARN 로그 |
| MON-AUTH-04 | 로그 보존 | SECURITY-14 | 최소 90일 |
| MON-AUTH-05 | 권한 변경 로깅 | SECURITY-14 | 역할 변경 시 INFO 로그 |

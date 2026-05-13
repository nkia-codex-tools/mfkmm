# Business Rules - Auth Service + API Gateway + Shared

## BR-AUTH: Authentication Rules

### BR-AUTH-01: Login Validation
- 사용자 ID와 비밀번호를 받아 인증을 수행한다
- 사용자 ID가 존재하지 않으면 실패 (USER_NOT_FOUND)
- 계정이 잠긴 상태면 비밀번호 검증 없이 즉시 실패 (ACCOUNT_LOCKED)
- 비밀번호가 불일치하면 실패 (INVALID_PASSWORD)
- 비밀번호 검증은 bcrypt compare로 수행

### BR-AUTH-02: Successful Login
- 인증 성공 시 Access Token (15분) + Refresh Token (7일) 발급
- 실패 횟수를 즉시 0으로 초기화
- LoginHistory에 성공 기록 (시각, IP, success=true)
- UserLoggedIn 이벤트 발행

### BR-AUTH-03: Failed Login
- 인증 실패 시 failedLoginAttempts를 1 증가
- LoginHistory에 실패 기록 (시각, IP, success=false, 사유)
- failedLoginAttempts >= 3 이면 BR-LOCK-01 적용

---

## BR-LOCK: Account Lock Rules

### BR-LOCK-01: Auto Lock
- 연속 3회 로그인 실패 시 계정 자동 잠금 (isLocked = true)
- AccountLockEvent 생성 (lockedAt = now)
- AccountLocked 이벤트 발행
- 잠긴 후 올바른 비밀번호로 시도해도 로그인 차단

### BR-LOCK-02: Unlock
- 관리자(ADMIN 또는 ROOT_ADMIN)만 잠금 해제 가능
- 해제 시 isLocked = false, AccountLockEvent.unlockedAt 업데이트
- 실패 횟수는 유지 (첫 성공 로그인 시 초기화 — BR-LOCK-03)
- AccountUnlocked 이벤트 발행

### BR-LOCK-03: Post-Unlock Reset
- 잠금 해제 후 첫 성공 로그인 시에만 failedLoginAttempts = 0
- 잠금 해제 후 다시 실패하면 누적 (예: 해제 시점에 3이면 다음 실패 시 4 → 아님, 3회 기준이므로 바로 잠금)
- **수정**: 잠금 해제 시에도 실패 횟수 유지. 첫 성공 시 0으로 리셋. 따라서 해제 후 바로 실패하면 failedAttempts=4가 되어 바로 잠금 (>=3 조건)

---

## BR-TOKEN: JWT Token Rules

### BR-TOKEN-01: Access Token
- 유효 기간: 15분
- Claims: sub(userId), role, iat, exp, jti
- 서명 알고리즘: HS256 (shared secret) 또는 RS256 (공개키/비밀키)
- 모든 API 요청 시 Authorization: Bearer {token} 헤더로 전달

### BR-TOKEN-02: Refresh Token
- 유효 기간: 7일
- DB에 저장 (RefreshToken collection)
- Access Token 만료 시 Refresh Token으로 새 Access Token 발급
- Refresh Token 자체도 만료되면 재로그인 필요

### BR-TOKEN-03: Token Refresh
- 유효한 Refresh Token 제출 시 새 Access Token 발급
- Refresh Token은 재사용 가능 (Rotation 미적용 — MVP)
- 로그아웃 시 Refresh Token revoke (revoked=true)

### BR-TOKEN-04: Token Revocation
- 로그아웃 시 해당 사용자의 Refresh Token revoke
- revoked=true인 토큰으로 refresh 시도 시 거부
- UserLoggedOut 이벤트 발행

---

## BR-PWD: Password Rules

### BR-PWD-01: Password Storage
- 비밀번호는 bcrypt (cost factor 10)으로 해싱 후 저장
- 평문 비밀번호는 어디에도 저장하지 않음

### BR-PWD-02: Password Change (Self)
- 본인만 자신의 비밀번호 변경 가능
- 현재 비밀번호 확인 필수 (bcrypt compare)
- 새 비밀번호를 bcrypt 해싱 후 저장
- PasswordChanged 이벤트 발행
- 기존 Refresh Token 모두 revoke (재로그인 강제)

### BR-PWD-03: Password Reset (Admin)
- ADMIN 또는 ROOT_ADMIN이 타 사용자 비밀번호 초기화 가능
- 임시 비밀번호 생성 또는 지정된 비밀번호로 설정
- ROOT_ADMIN의 비밀번호는 누구도 초기화 불가
- PasswordChanged 이벤트 발행
- 대상 사용자의 Refresh Token 모두 revoke

---

## BR-ROOT: Root Administrator Rules

### BR-ROOT-01: Initialization
- 시스템 최초 실행 시 환경 변수(MKFMM_ROOT_USER, MKFMM_ROOT_PASSWORD)에서 읽어 자동 생성
- 이미 존재하면 생성하지 않음 (중복 방지)
- role=ROOT_ADMIN, isRootAdmin=true

### BR-ROOT-02: Immutability
- ROOT_ADMIN 계정은 삭제 불가
- ROOT_ADMIN 계정의 role 변경 불가
- ROOT_ADMIN 비밀번호는 본인만 변경 가능 (다른 Admin도 초기화 불가)

---

## BR-GW: API Gateway Rules

### BR-GW-01: Routing
- 모든 외부 요청은 API Gateway를 통해 진입
- URL 경로 기반으로 백엔드 서비스로 라우팅
- /api/auth/** → auth-service
- /api/users/** → user-service
- /api/resources/** → resource-service
- /api/dataio/** → dataio-service
- /api/deploy/** → deploy-service
- /api/history/** → history-service

### BR-GW-02: JWT Validation
- /api/auth/login, /api/auth/refresh 제외 모든 요청에 JWT 검증
- Authorization 헤더에서 Bearer 토큰 추출
- 토큰 서명 검증 + 만료 확인
- 유효하면 UserInfo(userId, role)를 X-User-Id, X-User-Role 헤더로 백엔드에 전달
- 무효하면 401 Unauthorized 응답

### BR-GW-03: CORS
- 프론트엔드 도메인만 허용 (설정 기반)
- Preflight 요청(OPTIONS) 처리

### BR-GW-04: Rate Limiting
- 미적용 (사내 도구, 10~50명 규모)

# Business Rules - User Service

## BR-USR: User Management Rules

### BR-USR-01: User Creation
- 관리자(ADMIN 또는 ROOT_ADMIN)만 사용자 등록 가능
- userId는 시스템 내 유일해야 함 (중복 검증)
- 초기 비밀번호는 관리자가 지정
- 생성 시 mustChangePassword = true (첫 로그인 시 변경 강제)
- 비밀번호는 bcrypt(cost=10)으로 해싱 후 Auth Service에 저장 위임
- UserCreated 이벤트 발행
- AuditLog에 USER_CREATED 기록

### BR-USR-02: User Update
- 본인 정보 변경: name, email, department, memo만 가능
- 관리자가 타인 변경: name, email, department, memo 가능 (role 제외 — 별도 권한 API)
- ROOT_ADMIN 정보는 본인만 변경 가능
- updatedAt, updatedBy 자동 갱신
- AuditLog에 USER_UPDATED 기록 (변경 전/후 값)

### BR-USR-03: User Deletion
- 관리자(ADMIN 또는 ROOT_ADMIN)만 사용자 삭제 가능
- ROOT_ADMIN 계정은 삭제 불가
- 삭제 시 해당 사용자의 작업 이력도 함께 삭제 (History Service에 이벤트로 통지)
- 삭제된 사용자의 Refresh Token 모두 무효화 (Auth Service에 이벤트로 통지)
- UserDeleted 이벤트 발행
- AuditLog에 USER_DELETED 기록

### BR-USR-04: User Query
- 관리자: 전체 사용자 목록 조회 가능 (전체 정보: ID, 이름, 이메일, 부서, 권한, 생성일, 잠금 상태)
- 일반 사용자: 본인 정보만 조회 가능
- 검색: userId, name, email, department 필터 지원
- 페이지네이션 및 정렬 지원

---

## BR-PERM: Permission Rules

### BR-PERM-01: Grant Permission
- 관리자(ADMIN 또는 ROOT_ADMIN)만 권한 부여 가능
- 부여 가능 권한: READ, WRITE, ADMIN
- ROOT_ADMIN 권한은 시스템만 부여 (수동 부여 불가)
- ROOT_ADMIN의 권한 변경 시도 → 거부
- PermissionChanged 이벤트 발행
- AuditLog에 PERMISSION_GRANTED 기록 (이전 권한 → 새 권한)

### BR-PERM-02: Revoke Permission
- 관리자(ADMIN 또는 ROOT_ADMIN)만 권한 회수 가능
- ROOT_ADMIN 권한 회수 불가
- 자기 자신의 ADMIN 권한 회수 불가 (셀프 강등 방지)
- 회수 시 해당 사용자 role을 READ로 하향 (기본 권한)
- PermissionChanged 이벤트 발행
- AuditLog에 PERMISSION_REVOKED 기록

### BR-PERM-03: Permission Hierarchy
- ROOT_ADMIN > ADMIN > WRITE > READ
- 상위 권한은 하위 권한의 모든 기능 포함
- Admin이 다른 Admin의 권한을 변경할 수 있음 (동급 변경 허용)
- 단, ROOT_ADMIN만은 어떤 Admin도 변경 불가

---

## BR-UNLOCK: Account Unlock Rules

### BR-UNLOCK-01: Unlock Account
- 관리자(ADMIN 또는 ROOT_ADMIN)만 잠금 해제 가능
- User Service에서 isLocked = false로 변경
- Auth Service에 AccountUnlocked 이벤트 전달 (Auth가 잠금 상태 동기화)
- AuditLog에 ACCOUNT_UNLOCKED 기록

---

## BR-AUDIT: Audit Log Rules

### BR-AUDIT-01: Immutability
- AuditLog는 생성만 가능 (수정/삭제 불가)
- 모든 권한 변경, 사용자 생성/삭제에 대해 자동 기록

### BR-AUDIT-02: Query
- 관리자만 AuditLog 조회 가능
- 필터: action, targetUserId, performedBy, 기간
- 페이지네이션 및 정렬 지원

---

## BR-INIT: Root Admin Initialization (User Service 부분)

### BR-INIT-01: Account Creation
- 시스템 시작 시 ROOT_ADMIN 계정이 User Service DB에 존재하는지 확인
- 없으면 환경 변수(MKFMM_ROOT_USER)에서 userId를 읽어 User 레코드 생성
- role=ROOT_ADMIN, isRootAdmin=true, mustChangePassword=false
- 비밀번호 해싱 및 인증 정보는 Auth Service가 처리

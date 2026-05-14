# Components Definition

## Backend Components (Spring Boot, Domain-Based Structure)

### 1. Auth Domain (`com.resourcemanager.auth`)

| Component | Responsibility |
|-----------|---------------|
| AuthController | 인증 API 엔드포인트 (로그인, 회원가입, 토큰 갱신) |
| UserController | 사용자 관리 API (관리자: 사용자 목록, 권한 부여) |
| AuthService | 인증 비즈니스 로직 (JWT 생성/검증, 비밀번호 해싱) |
| UserService | 사용자 관리 로직 (가입 처리, 권한 변경) |
| UserRepository | 사용자 데이터 접근 계층 |
| JwtTokenProvider | JWT 토큰 생성, 검증, 파싱 유틸리티 |
| JwtAuthenticationFilter | HTTP 요청에서 JWT 추출 및 인증 처리 필터 |
| SecurityConfig | Spring Security 설정 (CORS, CSRF, 필터 체인) |

### 2. Resource Domain (`com.resourcemanager.resource`)

| Component | Responsibility |
|-----------|---------------|
| FunctionController | 기능(Functions) 리소스 API 엔드포인트 |
| MenuController | 메뉴(Menus) 리소스 API 엔드포인트 |
| MessageResourceController | 리소스 키(Message Resource) API 엔드포인트 |
| FunctionService | 기능 리소스 비즈니스 로직 (CRUD, 순서 변경, 자동생성 필드) |
| MenuService | 메뉴 리소스 비즈니스 로직 (CRUD, 순서 변경, 기능 참조) |
| MessageResourceService | 리소스 키 비즈니스 로직 (CRUD, 순서 변경, 자동계산 필드) |
| FunctionRepository | 기능 데이터 접근 계층 |
| MenuRepository | 메뉴 데이터 접근 계층 |
| MessageResourceRepository | 리소스 키 데이터 접근 계층 |

### 3. Import/Export Domain (`com.resourcemanager.transfer`)

| Component | Responsibility |
|-----------|---------------|
| TransferController | Import/Export API 엔드포인트 |
| TsvExportService | TSV 형식 export 처리 (전체/선택 행) |
| TsvImportService | TSV 형식 import 처리 (파싱, 검증, 미리보기, 적용) |
| TsvParser | TSV 파일 파싱 및 유효성 검증 유틸리티 |

### 4. History Domain (`com.resourcemanager.history`)

| Component | Responsibility |
|-----------|---------------|
| HistoryController | 변경 이력 조회 API 엔드포인트 |
| HistoryService | 변경 이력 기록 및 조회 로직 |
| HistoryRepository | 변경 이력 데이터 접근 계층 |

### 5. Version Domain (`com.resourcemanager.version`)

| Component | Responsibility |
|-----------|---------------|
| VersionController | 버전 태깅/롤백 API 엔드포인트 |
| VersionService | 버전 태깅, 스냅샷 생성, 롤백 로직 |
| VersionRepository | 버전 태그 데이터 접근 계층 |

### 6. Common (`com.resourcemanager.common`)

| Component | Responsibility |
|-----------|---------------|
| GlobalExceptionHandler | 전역 예외 처리 (에러 응답 포맷팅) |
| RateLimitFilter | 요청 속도 제한 필터 |
| SecurityHeadersFilter | HTTP 보안 헤더 적용 필터 |
| AuditAspect | AOP 기반 변경 이력 자동 기록 |

---

## Frontend Components (React TypeScript)

### 1. Pages

| Component | Responsibility |
|-----------|---------------|
| LoginPage | 로그인 화면 |
| RegisterPage | 회원가입 화면 |
| ResourcePage | 리소스 관리 메인 화면 (탭 기반) |
| AdminPage | 관리자 화면 (사용자 관리, 신규 가입자) |
| HistoryPage | 변경 이력 조회 화면 |
| VersionPage | 버전 관리 화면 (태그 목록, 롤백) |

### 2. Resource Components

| Component | Responsibility |
|-----------|---------------|
| ResourceTabs | 탭 네비게이션 (기능/메뉴/리소스키) |
| FunctionGrid | 기능 탭 AG Grid 테이블 |
| MenuGrid | 메뉴 탭 AG Grid 테이블 |
| MessageResourceGrid | 리소스 키 탭 AG Grid 테이블 |
| GridToolbar | 공통 도구 모음 (추가, 삭제, import, export, 검색) |
| ImportDialog | TSV import 미리보기 다이얼로그 |
| ExportDialog | Export 옵션 다이얼로그 |

### 3. Admin Components

| Component | Responsibility |
|-----------|---------------|
| UserList | 사용자 목록 테이블 |
| PendingUserList | 신규 가입자 대기 목록 |
| PermissionDialog | 권한 부여/변경 다이얼로그 |

### 4. Version Components

| Component | Responsibility |
|-----------|---------------|
| VersionList | 버전 태그 목록 |
| VersionDiff | 버전 간 변경 내역 비교 |
| RollbackConfirmDialog | 롤백 확인 팝업 |
| TagCreateDialog | 버전 태그 생성 다이얼로그 |

### 5. Common Components

| Component | Responsibility |
|-----------|---------------|
| AppLayout | 전체 레이아웃 (헤더, 사이드바, 콘텐츠) |
| ProtectedRoute | 인증 필요 라우트 가드 |
| LoadingSpinner | 로딩 상태 표시 |
| ErrorBoundary | 에러 경계 컴포넌트 |

### 6. State Management (Zustand Stores)

| Store | Responsibility |
|-------|---------------|
| useAuthStore | 인증 상태 (토큰, 사용자 정보, 권한) |
| useResourceStore | 리소스 데이터 상태 (현재 탭, 선택된 행) |
| useNotificationStore | 알림/토스트 메시지 상태 |

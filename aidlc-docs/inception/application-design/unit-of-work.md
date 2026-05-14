# Unit of Work Definition

## Decomposition Strategy
- **Approach**: 도메인 단위 분해 (5개 유닛)
- **Architecture**: 모놀리스 (단일 Spring Boot 앱 + React SPA)
- **Deployment**: 단일 Docker Compose (EC2)
- **Development Priority**: 인증 → 리소스 CRUD → Import/Export → 이력 → 버전

---

## Unit 1: Auth (인증 및 사용자 관리)

**Priority**: 1 (최우선)

**Scope**:
- 사용자 회원가입, 로그인, JWT 토큰 관리
- 관리자 초기 계정 자동 생성
- 역할 기반 접근 제어 (ADMIN, WRITER, READER, PENDING)
- 관리자: 신규 가입자 확인, 권한 부여
- Spring Security 설정, CORS, 필터 체인
- 프론트엔드: 로그인/가입 페이지, 관리자 페이지, ProtectedRoute

**Backend Components**:
- AuthController, UserController
- AuthService, UserService
- UserRepository, User Entity
- JwtTokenProvider, JwtAuthenticationFilter, SecurityConfig
- GlobalExceptionHandler, RateLimitFilter, SecurityHeadersFilter

**Frontend Components**:
- LoginPage, RegisterPage, AdminPage
- UserList, PendingUserList, PermissionDialog
- useAuthStore, ProtectedRoute, AppLayout
- apiClient (Axios + JWT interceptor), authApi

**Database Tables**:
- users

**Deliverables**:
- 동작하는 인증 시스템 (가입 → 로그인 → JWT → 관리자 권한 부여)
- 보안 필터 체인 (rate limiting, security headers)
- 프론트엔드 인증 흐름 완성

---

## Unit 2: Resource Management (리소스 관리)

**Priority**: 2

**Scope**:
- 3개 리소스(기능, 메뉴, 리소스키) CRUD
- 드래그앤드롭 행 순서 변경
- 다중 선택 및 일괄 삭제/이동
- 검색 및 필터링, 가상 스크롤
- 드롭다운 선택 (ACTION, 유형, boolean 필드)
- 자동 계산 필드 (리소스키 자동생성, full_resource_key, 중복/대문자 검증, 개수 카운트)
- 메뉴 탭 기능 ID 자동완성 + 기능 설명 자동 채움
- 인라인 편집

**Backend Components**:
- FunctionController, MenuController, MessageResourceController
- FunctionService, MenuService, MessageResourceService
- FunctionRepository, MenuRepository, MessageResourceRepository
- FunctionResource, MenuResource, MessageResource Entities

**Frontend Components**:
- ResourcePage, ResourceTabs
- FunctionGrid, MenuGrid, MessageResourceGrid
- GridToolbar
- useResourceStore, functionApi, menuApi, messageResourceApi

**Database Tables**:
- functions, menus, message_resources

**Deliverables**:
- 3개 탭 리소스 테이블 (AG Grid)
- 전체 CRUD 기능 (추가, 수정, 삭제, 일괄)
- 드래그앤드롭 순서 변경
- 드롭다운, 자동완성, 자동 계산 필드 동작

---

## Unit 3: Import/Export (데이터 가져오기/내보내기)

**Priority**: 3

**Scope**:
- TSV export (전체/선택 행, 헤더 포함, UTF-8)
- TSV import (파일 업로드, 파싱, 유효성 검증)
- Import 미리보기 (변경 사항 확인)
- 충돌 처리 (덮어쓰기/건너뛰기)
- Import 결과 요약

**Backend Components**:
- TransferController
- TsvExportService, TsvImportService
- TsvParser

**Frontend Components**:
- ImportDialog, ExportDialog
- transferApi

**Database Tables**:
- 기존 리소스 테이블 활용 (functions, menus, message_resources)

**Deliverables**:
- TSV export 기능 (전체/선택)
- TSV import 기능 (검증 + 미리보기 + 적용)
- 충돌 처리 UI

---

## Unit 4: History (변경 이력 관리)

**Priority**: 4

**Scope**:
- 모든 리소스 변경 시 필드 레벨 이력 자동 기록
- 이력 조회 화면 (필터: 날짜, 사용자, 리소스 종류)
- AOP 기반 이력 기록 또는 서비스 계층 직접 호출

**Backend Components**:
- HistoryController
- HistoryService
- HistoryRepository, ChangeHistory Entity
- AuditAspect

**Frontend Components**:
- HistoryPage
- historyApi

**Database Tables**:
- change_history

**Deliverables**:
- 자동 변경 이력 기록 (CRUD 작업 시)
- 이력 조회 화면 (필터, 페이징)

**Note**: 이 유닛 완성 시 Unit 2의 리소스 CRUD에 이력 기록 로직이 통합됨

---

## Unit 5: Version (버전 관리)

**Priority**: 5

**Scope**:
- 관리자 버전 태깅 (이름 부여)
- 전체 스냅샷 JSON 저장
- 태깅된 버전 목록 조회
- 버전 간 변경 내역(diff) 표시
- 특정 태깅 버전으로 롤백
- 롤백 확인 팝업
- 롤백 이력 기록

**Backend Components**:
- VersionController
- VersionService
- VersionRepository, VersionTag Entity

**Frontend Components**:
- VersionPage
- VersionList, VersionDiff, RollbackConfirmDialog, TagCreateDialog
- versionApi

**Database Tables**:
- version_tags

**Deliverables**:
- 버전 태그 생성/목록 조회
- 버전 간 diff 비교
- 롤백 기능 (확인 팝업 + 데이터 복원)

---

## Development Order

```
Unit 1: Auth ──────────────────────┐
                                   │
Unit 2: Resource Management ───────┤ (Auth에 의존: 인증된 사용자만 접근)
                                   │
Unit 3: Import/Export ─────────────┤ (Resource에 의존: 리소스 테이블 필요)
                                   │
Unit 4: History ───────────────────┤ (Resource에 의존: CRUD 작업에 이력 통합)
                                   │
Unit 5: Version ───────────────────┘ (Resource + History에 의존: 스냅샷/롤백)
```

## Code Organization (Greenfield Monolith)

```
project-root/
+-- backend/                    # Spring Boot 프로젝트
|   +-- src/main/java/com/resourcemanager/
|   |   +-- auth/              # Unit 1
|   |   +-- resource/          # Unit 2
|   |   +-- transfer/          # Unit 3
|   |   +-- history/           # Unit 4
|   |   +-- version/           # Unit 5
|   |   +-- common/            # 공통 모듈 (모든 유닛 공유)
|   +-- src/main/resources/
|   +-- src/test/
|   +-- build.gradle (or pom.xml)
|   +-- Dockerfile
|
+-- frontend/                   # React TypeScript 프로젝트
|   +-- src/
|   |   +-- pages/
|   |   +-- components/
|   |   +-- stores/
|   |   +-- api/
|   |   +-- types/
|   +-- package.json
|   +-- Dockerfile
|
+-- docker-compose.yml          # 배포 구성
+-- nginx.conf                  # Nginx 리버스 프록시 설정
```

# Requirements Document - 리소스 관리 시스템

## Intent Analysis

- **User Request**: 사내 Google Sheets로 관리하던 리소스(기능, 메뉴, 리소스키)를 체계적으로 관리하는 웹 애플리케이션 구축
- **Request Type**: New Project (Greenfield)
- **Scope**: System-wide (프론트엔드 + 백엔드 + 데이터베이스)
- **Complexity**: Complex (인증, CRUD, 버전 관리, import/export, 드래그앤드롭 등)
- **Requirements Depth**: Standard

---

## 1. Technology Stack Decisions

| Layer | Technology | Rationale |
|-------|-----------|-----------|
| Backend | Java (Spring Boot) | 사용자 선택 |
| Frontend | React (TypeScript) | 사용자 선택 |
| Database | SQLite | 단일 머신 배포, 경량 운영 |
| Table UI | AG Grid (Community Edition) | 드래그앤드롭, 가상 스크롤, 인라인 편집, 행 선택 내장 지원 |
| Authentication | JWT (JSON Web Token) | Stateless 인증 |
| Deployment | Docker Compose on EC2 | 단일 머신에서 모든 서비스 운영 |
| PBT Framework (Backend) | jqwik | JUnit 5 통합, stateful testing 지원 |
| PBT Framework (Frontend) | fast-check | Jest/Vitest 통합 |

---

## 2. Functional Requirements

### FR-01: 사용자 인증 및 권한 관리

#### FR-01.1: 관리자 계정
- 시스템 최초 구동 시 미리 정의된 관리자 계정 자동 생성
- 관리자는 모든 리소스에 대한 읽기/쓰기 권한 보유
- 사용자 권한 관리 가능 (읽기/쓰기 부여)

#### FR-01.2: 일반 사용자 계정
- 회원가입을 통한 계정 생성
- 가입 후 관리자의 권한 부여 대기 상태
- 권한 종류: 읽기(조회만), 쓰기(수정/추가/삭제)

#### FR-01.3: 신규 사용자 관리
- 관리자 전용 신규 가입자 확인 화면
- 가입자 목록 표시 (가입일시, 이름, 이메일)
- 읽기/쓰기 권한 부여 기능
- 권한 변경 이력 관리

### FR-02: 리소스 관리

#### FR-02.1: 공통 UI
- 탭 기반 네비게이션: 기능(Functions), 메뉴(Menus), 리소스 키(Message Resource)
- 테이블 형식으로 각 리소스 컬럼 표시
- 행 추가, 수정, 삭제 (CRUD)
- 다중 행 선택 (드래그, Shift/Ctrl 클릭)
- 선택한 행 일괄 삭제 및 이동
- 드래그 앤 드롭으로 행 순서 변경
- 검색 및 필터링
- 가상 스크롤 (1,000~10,000행 처리)

#### FR-02.2: 기능(Functions) 관리
- 15개 컬럼: A Class, B Class, C Class, 기능(ACTION), 기능명, 기능(FUNCTION) ID, 유형, Light, Standard, Enterprise, System Menu, Product Domain, 도메인 라이선스 리소스 타입, 관련 컨테이너 서비스명, 기능 ID 리소스키(자동생성)

**컬럼별 입력 방식:**

| 컬럼 | 입력 방식 | 옵션/규칙 |
|------|----------|----------|
| A Class | 텍스트 입력 | - |
| B Class | 텍스트 입력 | - |
| C Class | 텍스트 입력 | - |
| 기능(ACTION) | 드롭다운 선택 | cmm.view, cmm.insert, cmm.delete, cmm.update, cmm.upload, cmm.download_excel, download, cmm.excution, cmm.copy, cmm.load_1 |
| 기능명 | 텍스트 입력 | - |
| 기능(FUNCTION) ID | 텍스트 입력 | - |
| 유형 | 드롭다운 선택 | 읽기, 쓰기, 실행, Excel, Import, Export |
| Light | 드롭다운 선택 | TRUE, FALSE |
| Standard | 드롭다운 선택 | TRUE, FALSE |
| Enterprise | 드롭다운 선택 | TRUE, FALSE |
| System Menu | 드롭다운 선택 | TRUE, FALSE |
| Product Domain | 텍스트 입력 | - |
| 도메인 라이선스 리소스 타입 | 텍스트 입력 | - |
| 관련 컨테이너 서비스명 | 텍스트 입력 | - |
| 기능 ID 리소스키(자동생성) | **자동 계산** (읽기 전용) | `기능(FUNCTION) ID` 값을 소문자 변환 → `.`을 `_`로 치환 → `cmm.fn_` 접두어. 예: `DASHBOARD.DASHBOARD.WIDGET.VIEW` → `cmm.fn_dashboard_dashboard_widget_view`. 기능(FUNCTION) ID가 비어있으면 빈 값. |

#### FR-02.3: 메뉴(Menus) 관리
- 12개 컬럼: 메인 메뉴, 하위 메뉴 그룹, 하위 메뉴, 메뉴 1레벨, 메뉴 2레벨, 메뉴 3레벨, 메뉴 ID, 메뉴여부, System 메뉴, 기능(FUNCTION) ID, 기능 설명, 대메뉴 Icon

**컬럼별 입력 방식:**

| 컬럼 | 입력 방식 | 옵션/규칙 |
|------|----------|----------|
| 메인 메뉴 | 텍스트 입력 | - |
| 하위 메뉴 그룹(or 메뉴) | 텍스트 입력 | - |
| 하위 메뉴 | 텍스트 입력 | - |
| 메뉴 1레벨 | 텍스트 입력 | - |
| 메뉴 2레벨 | 텍스트 입력 | - |
| 메뉴 3레벨 | 텍스트 입력 | - |
| 메뉴 ID | 텍스트 입력 | - |
| 메뉴여부 | 드롭다운 선택 | TRUE, FALSE |
| System 메뉴 | 드롭다운 선택 | TRUE, FALSE |
| 기능(FUNCTION) ID | **자동완성 드롭다운** | 기능 탭의 `기능(FUNCTION) ID` 컬럼을 참조. 타이핑 시 입력한 텍스트와 매칭되는 기능 ID 목록을 드롭다운으로 표시. 기능 탭에 존재하는 ID만 선택 가능. |
| 기능 설명 | **자동 계산** (읽기 전용) | `기능(FUNCTION) ID` 선택 시, 기능 탭의 해당 기능 레코드에서 `기능명` 값을 자동으로 가져옴. |
| 대메뉴 Icon | 텍스트 입력 | - |

#### FR-02.4: 리소스 키(Message Resource) 관리
- 15개 컬럼: 번호, 중복/대문자, 모듈, resource_key, full_resource_key, 국문, 영문, 일문, 설명/사용처, 등록/수정일자, 등록자, 리소스키 개수, 국문 개수, 영문 개수, 일문 개수

**컬럼별 입력 방식:**

| 컬럼 | 입력 방식 | 옵션/규칙 |
|------|----------|----------|
| 번호 | **자동 계산** (읽기 전용) | `모듈`과 `resource_key`가 모두 존재할 때만 행 번호(순번) 표시. 하나라도 비어있으면 빈 값. |
| 중복/대문자 | **자동 계산** (읽기 전용) | 검증 규칙: ① 번호가 비어있으면 빈 값 ② 전체 데이터에서 `국문` 값이 2회 이상 나타나면 "중복" ③ `resource_key` 값이 소문자(trim 후)와 동일하면 "정상" ④ 그 외 "대문자" |
| 모듈 | 텍스트 입력 | - |
| resource_key | 텍스트 입력 | - |
| full_resource_key | **자동 계산** (읽기 전용) | `모듈` + "." + `resource_key` 조합. 번호가 비어있으면 빈 값. 예: 모듈=`tcm`, resource_key=`work_none` → `tcm.work_none` |
| 국문(한글) | 텍스트 입력 | - |
| 영문(영어) | 텍스트 입력 | - |
| 일문(일본어) | 텍스트 입력 | - |
| 용어에 대한 설명/사용처 | 텍스트 입력 | - |
| 등록/수정일자 | 날짜 입력 | - |
| 등록자 | 텍스트 입력 | - |
| 리소스키 개수 | **자동 계산** (읽기 전용) | 전체 데이터에서 동일한 `resource_key` 값의 개수. 번호가 비어있으면 빈 값. |
| 국문 개수 | **자동 계산** (읽기 전용) | 전체 데이터에서 동일한 `국문` 값의 개수. 번호가 비어있으면 빈 값. |
| 영문 개수 | **자동 계산** (읽기 전용) | 전체 데이터에서 동일한 `영문` 값의 개수. 번호가 비어있으면 빈 값. |
| 일문 개수 | **자동 계산** (읽기 전용) | 전체 데이터에서 동일한 `일문` 값의 개수. 번호가 비어있으면 빈 값. |

### FR-03: Import/Export

#### FR-03.1: TSV Export
- 각 탭에서 전체 또는 선택 행을 TSV로 export
- 헤더 행 포함, UTF-8 인코딩
- 파일명: functions.tsv, menus.tsv, message-resource.tsv

#### FR-03.2: TSV Import
- TSV 파일 업로드
- 데이터 유효성 검증 (컬럼 수, 필수 필드)
- 미리보기 표시 (import 전 변경 사항 확인)
- 기존 데이터 충돌 처리 (덮어쓰기/건너뛰기)
- import 결과 요약 (성공/실패 건수)

### FR-04: 변경 이력 관리

- 모든 수정, 추가, 삭제 작업 시 이력 자동 기록
- 기록 정보: 변경 일시, 변경자, 변경 대상, 변경 유형, 변경 전/후 값
- 이력 조회 화면 (필터: 날짜, 사용자, 리소스 종류)

### FR-05: 버전 관리

- 사용자 수정 시 자동 변경 이력(diff) 기록
- 관리자가 특정 시점에 버전 태그 부여 (예: "v1.0", "2025-05 릴리스")
- 태깅된 버전 목록 조회 및 버전 간 변경 내역 확인
- 관리자가 특정 태깅 버전으로 롤백 가능
- 롤백 시 확인 팝업 표시
- 롤백 자체도 이력으로 기록

---

## 3. Non-Functional Requirements

### NFR-01: Performance
- 예상 사용자: 10~50명 (사내 사용자)
- 예상 데이터: 각 탭당 1,000~10,000행
- 테이블 렌더링: 가상 스크롤로 대량 행 효율적 처리
- API 응답 시간: 일반 CRUD < 500ms

### NFR-02: Deployment
- 단일 EC2 인스턴스에 Docker Compose로 배포
- 프론트엔드 + 백엔드 + SQLite를 단일 머신에서 운영
- SQLite 파일은 Docker volume으로 데이터 영속성 보장

### NFR-03: Security (SECURITY Extensions Enabled)
- JWT 기반 인증, 토큰 서버 측 검증
- 역할 기반 접근 제어 (관리자/읽기/쓰기)
- 비밀번호 해싱 (bcrypt 등 적응형 알고리즘)
- 입력 유효성 검증 (모든 API 파라미터)
- HTTP Security Headers 적용
- Rate limiting 적용
- 구조적 로깅 (structured logging)
- CORS 제한 정책

### NFR-04: Testing (PBT Extension Enabled - Full)
- jqwik (Backend, Java) + fast-check (Frontend, TypeScript)
- 모든 직렬화/역직렬화, 데이터 변환에 round-trip PBT
- 비즈니스 규칙 invariant에 PBT
- example-based test와 PBT 병행
- CI에서 seed 로깅 및 shrinking 지원

### NFR-05: Usability
- 인라인 편집 또는 모달 편집 지원
- 변경 사항 시각적 하이라이트
- 드래그 앤 드롭 핸들 직관적 표시
- 한국어 UI

---

## 4. Constraints (제외 사항)

요구사항 문서에 명시된 제외 기능:
- 외부 인증 연동 (OAuth, SSO, SAML, SNS 로그인)
- 다단계 인증 (2FA, OTP) - 단, SECURITY-12에 따라 관리자 MFA는 구현 필요
- 알림 시스템 (이메일, 푸시, 메신저, SMS)
- 고급 데이터 관리 (자동 참조 무결성, 외부 동기화, 10만행 이상 최적화, 저장 시 암호화)
- 분석/리포트 (대시보드, 활동 분석, 사용 빈도, 스케줄링)
- 협업 기능 (실시간 동시 편집, 승인 워크플로우, 댓글, 리소스 잠금)
- 외부 연동 (Git, CI/CD, 이슈 트래커, 번역 API)
- 고급 UI (모바일 반응형, 다크 모드, 단축키 커스터마이징, 컬럼 표시/숨김, 레이아웃 저장)

---

## 5. Architecture Overview

```
+---------------------------------------------------+
|                EC2 Instance                        |
|                                                   |
|  +---------------------------------------------+ |
|  |           Docker Compose                     | |
|  |                                              | |
|  |  +----------------+    +------------------+  | |
|  |  |   Frontend     |    |    Backend       |  | |
|  |  |   (React/TS)   |    |  (Spring Boot)   |  | |
|  |  |   Nginx        |    |    + SQLite      |  | |
|  |  |   Port: 80     |    |    Port: 8080    |  | |
|  |  +-------+--------+    +--------+---------+  | |
|  |          |                       |            | |
|  |          |    REST API (JWT)     |            | |
|  |          +-----------------------+            | |
|  |                                              | |
|  +---------------------------------------------+ |
|                                                   |
|  +---------------------------------------------+ |
|  |  Docker Volume: SQLite DB file               | |
|  +---------------------------------------------+ |
+---------------------------------------------------+
```

---

## 6. Data Model Summary

### Users
- id, email, password_hash, name, role (ADMIN/READER/WRITER), status (PENDING/ACTIVE), created_at, updated_at

### Resources (Functions, Menus, Message Resource)
- 각 리소스 타입별 테이블
- row_order 컬럼으로 행 순서 관리
- created_at, updated_at, created_by, updated_by

### Change History (Audit Trail)
- id, resource_type, resource_id, change_type (CREATE/UPDATE/DELETE), field_name, old_value, new_value, changed_by, changed_at

### Version Tags
- id, tag_name, description, resource_type, snapshot_data (JSON), created_by, created_at

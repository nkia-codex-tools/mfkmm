# Business Rules - DataIO Service + Deploy Service + Frontend

## BR-IMP: Import Rules

### BR-IMP-01: File Validation
- 지원 형식: Excel(.xlsx), TSV(.tsv), JSON(.json)
- 파일 크기 제한: 5MB 이하
- 5MB 초과 시 즉시 거부 (413 Payload Too Large)
- 빈 파일 거부 (400 Bad Request)

### BR-IMP-02: File Parsing
- Excel: 첫 번째 시트의 첫 행을 헤더로 인식
- TSV: 첫 행을 헤더로 인식, 탭 구분
- JSON: 배열 형태 `[{key, content, description, resourceType}, ...]`
- 필수 필드: resourceType, key, content
- 선택 필드: description

### BR-IMP-03: Row Validation
- resourceType은 MESSAGE_KEY, FUNCTION_ID, MENU_ID 중 하나
- key는 비어있으면 안됨 (빈 문자열 또는 null 불가)
- content는 비어있으면 안됨
- 유효하지 않은 행은 errors에 기록하고 건너뜀

### BR-IMP-04: Duplicate Check
- 각 행마다 Resource Service에 중복 검사 요청 (REST 호출)
- key + resourceType 조합으로 정확 일치 검사
- 중복 발견 시 ConflictPolicy에 따라 처리:
  - SKIP: 건너뛰고 skippedCount 증가
  - OVERWRITE: 기존 리소스 업데이트
  - ASK: 해당 행에 대해 사용자 확인 대기 (현재 단순화: SKIP 또는 OVERWRITE만 지원)

### BR-IMP-05: Import Execution
- 5MB 이하 파일은 동기 처리 (즉시 응답)
- 모든 행 처리 후 ImportResult 반환
- 성공/실패/스킵 건수 집계
- ImportCompleted 이벤트 발행
- 작업 이력(History) 기록

### BR-IMP-06: Import Result Report
- 총 행 수, 성공 건수, 실패 건수, 스킵 건수 포함
- 실패한 행에 대해 행 번호, 필드, 값, 사유 목록 제공

---

## BR-EXP: Export Rules

### BR-EXP-01: Partial Export
- 검색 조건(query)에 매칭되는 리소스만 내보냄
- Resource Service에 검색 쿼리 전달 (REST 호출)
- 결과를 지정 형식(EXCEL/TSV/JSON)으로 변환

### BR-EXP-02: Full Export
- 전체 리소스를 지정 형식으로 내보냄
- Resource Service에 전체 데이터 조회 요청
- Soft Delete 상태인 리소스는 제외 (active만)

### BR-EXP-03: Export File Generation
- Excel: Apache POI로 .xlsx 생성, 헤더 행 포함
- TSV: 헤더 행 + 탭 구분 텍스트
- JSON: `[{resourceType, key, content, description, createdBy, createdAt}, ...]`
- 파일명 형식: `export_{type}_{yyyyMMdd_HHmmss}.{ext}`

### BR-EXP-04: Export Delivery
- 생성된 파일을 HTTP Response로 직접 반환 (다운로드)
- Content-Type 적절히 설정
- Content-Disposition: attachment; filename="..." 헤더 설정
- ExportCompleted 이벤트 발행
- 작업 이력(History) 기록

---

## BR-DEP: Deploy Rules

### BR-DEP-01: Deploy Execution
- Admin 또는 Root Admin만 배포 가능
- 배포 = 전체 EXPORT 수행 + 배포 메타데이터 저장
- DataIO Service의 exportAll 기능을 REST 호출로 재사용
- 배포 버전: `yyyyMMdd-HHmmss` 형식 자동 생성

### BR-DEP-02: Deploy Metadata
- 배포 시각, 수행자, 형식, 건수, 파일 크기, 파일 경로 저장
- 배포 파일은 서버 로컬 디스크에 영구 저장 (재다운로드 위해)
- 경로 형식: `/data/deployments/{version}/{filename}`

### BR-DEP-03: Deploy History
- 배포 이력 목록 조회 (페이지네이션, 최신순 정렬)
- Admin 이상만 조회 가능
- DeployCompleted 이벤트 발행
- 작업 이력(History) 기록

### BR-DEP-04: Redownload
- 배포 이력에서 특정 배포의 파일을 다시 다운로드
- 저장된 filePath에서 파일 읽어 반환
- 파일이 존재하지 않으면 404 반환

---

## BR-FE: Frontend Rules

### BR-FE-01: Authentication Flow
- 로그인 성공 시 accessToken, refreshToken을 메모리에 저장 (localStorage 미사용)
- 모든 API 요청에 Authorization: Bearer {accessToken} 헤더 추가
- 401 응답 수신 시 refreshToken으로 토큰 갱신 시도
- 갱신 실패 시 로그인 페이지로 리다이렉트
- 로그아웃 시 상태 초기화 + /auth/logout 호출

### BR-FE-02: Permission-Based UI
- 메뉴 노출은 role에 따라 제어
- READ: 리소스 검색, EXPORT만 노출
- WRITE: READ + 리소스 CRUD, IMPORT 노출
- ADMIN/ROOT_ADMIN: WRITE + 사용자 관리, 배포, 이력 조회 노출
- 권한 없는 기능의 버튼/메뉴는 숨김 (disabled가 아닌 hidden)

### BR-FE-03: Error Handling
- API 오류 시 Toast 알림으로 사용자에게 표시
- 네트워크 오류: "서버에 연결할 수 없습니다" 메시지
- 400 오류: 서버 응답의 message 필드 표시
- 403 오류: "권한이 없습니다" 메시지
- 500 오류: "서버 오류가 발생했습니다" 메시지

### BR-FE-04: File Upload Validation (Client-Side)
- IMPORT 파일 업로드 시 클라이언트 측에서 선제 검증
- 허용 확장자: .xlsx, .tsv, .json
- 파일 크기 제한: 5MB 이하 (초과 시 업로드 차단)
- 빈 파일 차단

### BR-FE-05: Pagination
- 기본 페이지 크기: 20건
- 선택 가능 크기: 10, 20, 50, 100
- 페이지 네비게이션: 이전/다음 + 페이지 번호 직접 선택

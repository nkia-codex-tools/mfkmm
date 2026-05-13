# MKFMM Services

## Service Layer Overview (Hexagonal - Application Services)

Application Services는 Use Case를 구현하며, Domain 로직을 조합(orchestrate)합니다.

---

## 1. AuthService

**Implements**: AuthUseCase

**Orchestration**:
- 로그인: UserRepository에서 사용자 조회 → 잠금 상태 확인 → 비밀번호 검증 → 세션 생성 → 로그인 이력 기록
- 로그아웃: 세션 무효화 → 로그인 이력 기록
- 계정 잠금: 실패 횟수 증가 → 3회 도달 시 자동 잠금
- 잠금 해제: Admin 권한 확인 → 잠금 해제 → 감사 로그

**Collaborators**:
- UserRepository (사용자 조회)
- SessionRepository (세션 CRUD)
- LoginHistoryRepository (이력 기록)
- HistoryUseCase (작업 이력 기록 위임)

---

## 2. UserService

**Implements**: UserManagementUseCase

**Orchestration**:
- 사용자 등록: 중복 ID 확인 → 비밀번호 bcrypt 해싱 → 저장 → 이력 기록
- 사용자 삭제: Root Admin 여부 확인 → 삭제 → 이력 기록
- 사용자 변경: 권한 검증 (본인 or Admin) → 업데이트 → 이력 기록

**Collaborators**:
- UserRepository (사용자 CRUD)
- HistoryUseCase (작업 이력)

---

## 3. PermissionService

**Implements**: PermissionUseCase

**Orchestration**:
- 권한 부여: Admin 권한 확인 → Root Admin 대상 변경 차단 → 권한 업데이트 → 감사 로그 기록
- 권한 회수: Admin 권한 확인 → 권한 제거 → 감사 로그 기록

**Collaborators**:
- UserRepository (사용자 권한 업데이트)
- AuditLogRepository (감사 로그)

---

## 4. ResourceService

**Implements**: ResourceUseCase

**Orchestration**:
- 등록: SimilarityUseCase.checkDuplicate → 중복 시 차단 → SimilarityUseCase.findSimilar → 유사 제안 반환 → 사용자 선택 후 저장 → 이력 기록
- 수정: 중복 검증 → 변경 전 스냅샷 → 업데이트 → 이력 기록 (변경 전/후)
- 삭제: Soft Delete 플래그 설정 → 이력 기록

**Collaborators**:
- ResourceRepository (리소스 CRUD)
- SimilarityUseCase (중복/유사 검사)
- HistoryUseCase (작업 이력)

---

## 5. ResourceSearchService

**Implements**: ResourceSearchUseCase

**Orchestration**:
- 검색: 쿼리 파싱 → 필터 적용 → 페이지네이션 조회 → 검색 이력 기록

**Collaborators**:
- ResourceRepository (검색)
- HistoryUseCase (검색 이력)

---

## 6. SimilarityService

**Implements**: SimilarityUseCase

**Orchestration**:
- 중복 검사: ResourceRepository에서 정확 매칭 조회
- 유사 탐지: 문자열 부분 일치 후보 조회 → Levenshtein 거리 계산 → 점수 기준 정렬 → 상위 후보 반환
- 선택 기록: 사용자 선택 결과를 WorkLog에 기록

**Collaborators**:
- ResourceRepository (후보 조회)
- HistoryUseCase (선택 이력)

---

## 7. ImportService

**Implements**: ImportUseCase

**Orchestration**:
- IMPORT: 파일 파싱(Excel/TSV/JSON) → 크기 확인 (5MB 이하 동기) → 각 행 중복 검증 → 충돌 정책 적용 → 저장 → 결과 리포트 생성 → 이력 기록
- EXPORT 크기 초과 시 비동기: 작업 큐 등록 → 상태 조회 가능

**Collaborators**:
- FileProcessor (파일 파싱)
- ResourceRepository (저장)
- SimilarityUseCase (중복 검증)
- HistoryUseCase (작업 이력)

---

## 8. ExportService

**Implements**: ExportUseCase

**Orchestration**:
- 부분 EXPORT: 검색 쿼리 실행 → 형식 변환 → 파일 생성 → 이력 기록
- 전체 EXPORT: 전체 데이터 조회 → 크기 판단 → 5MB 이하 동기 / 초과 비동기 → 형식 변환 → 이력 기록

**Collaborators**:
- ResourceRepository (데이터 조회)
- FileProcessor (형식 변환)
- HistoryUseCase (작업 이력)

---

## 9. DeployService

**Implements**: DeployUseCase

**Orchestration**:
- 배포: Admin 권한 확인 → ExportUseCase.exportAll 호출 → 배포 메타데이터 생성 → 배포 이력 저장
- 재다운로드: 배포 이력에서 파일 경로 조회 → 파일 반환

**Collaborators**:
- ExportUseCase (전체 EXPORT 재사용)
- DeploymentRepository (배포 이력)
- HistoryUseCase (작업 이력)

---

## 10. HistoryService

**Implements**: HistoryUseCase

**Orchestration**:
- 기록: 작업 정보 수신 → WorkLog 생성 → 저장
- 조회: 필터 적용 → 페이지네이션 조회
- 보존 정책: 스케줄 기반으로 6개월 경과 검색 이력 삭제

**Collaborators**:
- WorkLogRepository (이력 CRUD)

---

## Cross-Cutting Services

### SecurityFilter (Inbound Adapter)
- 모든 요청의 세션 유효성 검증
- 권한 기반 접근 제어 (Read/Write/Admin)
- 잠긴 계정 접근 차단

### ScheduledTaskService
- 30일 경과 Soft Delete 리소스 Hard Delete
- 6개월 경과 검색 이력 삭제
- 만료 세션 정리

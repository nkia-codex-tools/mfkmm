# MKFMM User Stories

**Approach**: User Journey-Based (사용자 워크플로우 흐름 기반)
**Granularity**: High-level (Epic/대기능 단위)
**Priority**: MoSCoW (Must/Should/Could/Won't)

---

## Epic 1: 인증 및 세션 관리

### US-1.1: 시스템 로그인
**As a** 시스템 사용자 (모든 역할)
**I want to** 사용자 ID와 비밀번호로 로그인할 수 있다
**So that** 내 권한에 맞는 시스템 기능에 접근할 수 있다

**Priority**: Must

**Acceptance Criteria (Given-When-Then)**:
- Given 유효한 계정이 있을 때, When 올바른 ID/비밀번호를 입력하면, Then 로그인에 성공하고 대시보드로 이동한다
- Given 유효한 계정이 있을 때, When 잘못된 비밀번호를 입력하면, Then 로그인 실패 메시지가 표시되고 실패 횟수가 증가한다
- Given 로그인 상태일 때, When 30분간 활동이 없으면, Then 세션이 만료되어 로그인 화면으로 이동한다

---

### US-1.2: 계정 잠금 (복잡 에러 시나리오)
**As a** 시스템 운영자
**I want to** 3회 로그인 실패 시 계정이 자동 잠금되고 관리자만 해제할 수 있다
**So that** 브루트포스 공격으로부터 시스템을 보호할 수 있다

**Priority**: Must

**Acceptance Criteria (Given-When-Then)**:
- Given 사용자가 연속 3회 로그인에 실패했을 때, When 다시 로그인을 시도하면, Then 계정 잠금 메시지가 표시되고 로그인이 차단된다
- Given 계정이 잠긴 상태일 때, When 관리자가 잠금 해제를 수행하면, Then 해당 사용자가 다시 로그인할 수 있다
- Given 계정이 잠긴 상태일 때, When 잠금된 사용자가 올바른 비밀번호로 시도하더라도, Then 로그인이 차단된다

---

### US-1.3: 로그아웃
**As a** 시스템 사용자 (모든 역할)
**I want to** 로그아웃하여 세션을 종료할 수 있다
**So that** 다른 사람이 내 계정으로 접근하는 것을 방지할 수 있다

**Priority**: Must

**Acceptance Criteria (Checklist)**:
- [ ] 로그아웃 버튼 클릭 시 세션이 즉시 무효화된다
- [ ] 로그아웃 후 로그인 화면으로 리다이렉트된다
- [ ] 로그아웃 후 이전 세션 토큰으로 API 접근이 차단된다

---

## Epic 2: 사용자 및 권한 관리

### US-2.1: 사용자 등록
**As a** 관리자 (Admin)
**I want to** 새로운 사용자를 등록하고 권한을 부여할 수 있다
**So that** 팀원들이 시스템에 접근하여 리소스를 관리할 수 있다

**Priority**: Must

**Acceptance Criteria (Given-When-Then)**:
- Given 관리자로 로그인한 상태에서, When 사용자 등록 양식에 정보를 입력하고 제출하면, Then 새 사용자가 생성되고 지정된 권한(Read/Write)이 부여된다
- Given 이미 존재하는 사용자 ID로 등록을 시도할 때, When 제출하면, Then 중복 ID 오류 메시지가 표시된다
- Given Write 사용자로 로그인한 상태에서, When 사용자 등록을 시도하면, Then 권한 부족 메시지가 표시되고 등록이 차단된다

---

### US-2.2: 권한 관리
**As a** 관리자 (Admin)
**I want to** 사용자의 권한을 변경하거나 회수할 수 있다
**So that** 업무 변경에 따라 적절한 접근 범위를 통제할 수 있다

**Priority**: Must

**Acceptance Criteria (Given-When-Then)**:
- Given 관리자로 로그인한 상태에서, When 사용자의 권한을 Read에서 Write로 변경하면, Then 해당 사용자는 다음 로그인부터 Write 기능을 사용할 수 있다
- Given 관리자로 로그인한 상태에서, When 시스템 최상위 관리자의 권한 변경을 시도하면, Then 변경 불가 메시지가 표시된다
- Given 권한이 변경될 때, When 변경이 완료되면, Then 감사 로그에 변경 이력이 기록된다

---

### US-2.3: 사용자 삭제
**As a** 관리자 (Admin)
**I want to** 더 이상 필요하지 않은 사용자 계정을 삭제할 수 있다
**So that** 불필요한 계정의 시스템 접근을 차단할 수 있다

**Priority**: Must

**Acceptance Criteria (Checklist)**:
- [ ] 관리자만 사용자 삭제가 가능하다
- [ ] 시스템 최상위 관리자는 삭제할 수 없다
- [ ] 삭제된 사용자는 즉시 로그인이 불가능하다
- [ ] 삭제 작업이 감사 로그에 기록된다

---

### US-2.4: 시스템 최상위 관리자 초기화
**As a** 시스템 운영자
**I want to** 시스템 최초 실행 시 최상위 관리자가 환경 변수 기반으로 자동 생성된다
**So that** 시스템 거버넌스의 기반이 항상 보장된다

**Priority**: Must

**Acceptance Criteria (Checklist)**:
- [ ] 시스템 최초 실행 시 환경 변수에서 Root Admin 인증 정보를 읽어 자동 생성된다
- [ ] Root Admin은 어떤 경우에도 변경/삭제가 불가능하다
- [ ] Root Admin은 모든 권한을 보유한다

---

## Epic 3: 리소스 검색 및 조회

### US-3.1: 리소스 검색
**As a** 시스템 사용자 (모든 역할)
**I want to** 키워드로 메시지 리소스 키, 기능 ID, 메뉴 ID, 내용을 검색할 수 있다
**So that** 필요한 리소스를 빠르게 찾아 재사용할 수 있다

**Priority**: Must

**Acceptance Criteria (Given-When-Then)**:
- Given 로그인한 상태에서, When 검색어를 입력하고 검색을 실행하면, Then 키/ID/내용에 해당 키워드가 포함된 리소스 목록이 표시된다
- Given 검색 결과가 있을 때, When 리소스 유형 필터를 적용하면, Then 해당 유형(메시지/기능/메뉴)만 필터링되어 표시된다
- Given 검색 결과가 많을 때, When 페이지를 이동하면, Then 페이지네이션으로 결과가 나뉘어 표시된다
- Given 검색을 수행하면, When 결과가 반환될 때, Then 검색 이력이 자동 기록된다

---

## Epic 4: 리소스 등록/변경/삭제

### US-4.1: 리소스 등록 (중복 확인 + 유사 제안 포함)
**As a** Write 사용자 (개발자)
**I want to** 새 리소스를 등록할 때 중복 확인과 유사 정보 제안을 받을 수 있다
**So that** 중복 없이 일관된 리소스를 관리할 수 있다

**Priority**: Must

**Acceptance Criteria (Given-When-Then)**:
- Given Write 사용자로 로그인한 상태에서, When 새 리소스 정보를 입력하면, Then 중복 여부가 자동 검증된다
- Given 동일한 키/ID가 이미 존재할 때, When 등록을 시도하면, Then 중복 알림이 표시되고 등록이 차단된다
- Given 유사한 기존 리소스가 존재할 때, When 등록을 시도하면, Then 유사 후보 리스트가 유사도 점수와 함께 표시된다
- Given 유사 정보가 제안되었을 때, When "기존 리소스 사용"을 선택하면, Then 신규 등록이 취소되고 선택 이력이 기록된다
- Given 유사 정보가 제안되었을 때, When "신규 등록 강행"을 선택하면, Then 리소스가 등록되고 선택 사유와 함께 이력이 기록된다

---

### US-4.2: 리소스 변경
**As a** Write 사용자 (개발자)
**I want to** 기존 리소스의 내용을 수정할 수 있다
**So that** 최신 상태를 반영하여 리소스를 유지보수할 수 있다

**Priority**: Must

**Acceptance Criteria (Checklist)**:
- [ ] Write 권한 이상의 사용자만 수정 가능하다
- [ ] 수정 시 변경 전/후 내용이 이력에 기록된다
- [ ] 수정 시에도 중복 검증이 수행된다
- [ ] 최종 수정자와 수정 시각이 자동 업데이트된다

---

### US-4.3: 리소스 삭제
**As a** Write 사용자 (개발자)
**I want to** 더 이상 필요하지 않은 리소스를 삭제할 수 있다
**So that** 불필요한 데이터가 정리되어 검색 품질이 유지된다

**Priority**: Must

**Acceptance Criteria (Checklist)**:
- [ ] Write 권한 이상의 사용자만 삭제 가능하다
- [ ] 삭제 시 Soft Delete로 처리된다 (삭제 플래그 설정)
- [ ] Soft Delete된 리소스는 검색 결과에서 제외된다
- [ ] 30일 후 자동으로 Hard Delete가 수행된다
- [ ] 삭제 이력이 자동 기록된다

---

## Epic 5: IMPORT / EXPORT

### US-5.1: 데이터 IMPORT
**As a** Write 사용자 (개발자)
**I want to** Excel/TSV/JSON 파일을 업로드하여 리소스를 일괄 등록할 수 있다
**So that** 대량의 리소스를 수작업 없이 효율적으로 등록할 수 있다

**Priority**: Must

**Acceptance Criteria (Given-When-Then)**:
- Given Write 사용자로 로그인한 상태에서, When 5MB 이하의 Excel/TSV/JSON 파일을 업로드하면, Then 파일이 파싱되고 중복 검증이 수행된다
- Given 중복/충돌이 발견되었을 때, When 처리 방식(건너뛰기/덮어쓰기/확인)을 선택하면, Then 선택에 따라 데이터가 처리된다
- Given IMPORT가 완료되면, When 결과를 확인하면, Then 성공/실패/스킵 건수가 포함된 리포트가 표시된다
- Given 5MB를 초과하는 파일을 업로드하면, When 업로드를 시도하면, Then 파일 크기 초과 오류가 표시된다

---

### US-5.2: 데이터 EXPORT
**As a** 시스템 사용자 (모든 역할)
**I want to** 검색 결과 또는 전체 리소스를 Excel/TSV/JSON으로 내보낼 수 있다
**So that** 외부 문서 작성이나 백업 용도로 데이터를 활용할 수 있다

**Priority**: Must

**Acceptance Criteria (Checklist)**:
- [ ] 모든 역할의 사용자가 EXPORT 가능하다
- [ ] 부분 EXPORT (검색 결과 기반) 가능하다
- [ ] 전체 EXPORT (전체 리소스) 가능하다
- [ ] Excel(.xlsx), TSV, JSON 형식을 지원한다
- [ ] EXPORT 이력이 자동 기록된다

---

## Epic 6: 배포 관리

### US-6.1: 전체 데이터 배포
**As a** 관리자 (Admin)
**I want to** 전체 리소스 데이터를 공식적으로 배포하고 이력을 관리할 수 있다
**So that** 외부 시스템/팀에 일관된 최신 데이터를 공유할 수 있다

**Priority**: Must

**Acceptance Criteria (Given-When-Then)**:
- Given 관리자로 로그인한 상태에서, When 배포 형식(Excel/TSV/JSON)을 선택하고 배포를 실행하면, Then 전체 데이터가 해당 형식으로 생성되고 배포 이력이 기록된다
- Given 배포가 완료되면, When 배포 이력을 조회하면, Then 배포 시각, 작업자, 형식, 데이터 건수가 표시된다
- Given 이전 배포 이력이 있을 때, When 재다운로드를 요청하면, Then 해당 시점의 배포 파일을 다시 다운로드할 수 있다

---

## Epic 7: 이력 관리

### US-7.1: 작업 이력 조회
**As a** 관리자 (Admin)
**I want to** 모든 사용자의 작업 이력(검색/CRUD/IMPORT/EXPORT)을 조회할 수 있다
**So that** 데이터 변경을 추적하고 감사 목적으로 활용할 수 있다

**Priority**: Must

**Acceptance Criteria (Checklist)**:
- [ ] 작업 종류, 작업자, 기간, 리소스 유형별 필터링 가능
- [ ] 페이지네이션 및 정렬 지원
- [ ] 이력 데이터 EXPORT 가능 (감사 목적)
- [ ] 검색 이력은 6개월 보존 후 자동 삭제

---

### US-7.2: 본인 작업 이력 조회
**As a** Write/Read 사용자
**I want to** 내가 수행한 작업 이력을 조회할 수 있다
**So that** 내 작업 내용을 확인하고 추적할 수 있다

**Priority**: Must

**Acceptance Criteria (Checklist)**:
- [ ] 본인의 작업 이력만 조회 가능 (타인 이력 접근 불가)
- [ ] 작업 종류, 기간별 필터링 가능
- [ ] 페이지네이션 지원

---

### US-7.3: 로그인 이력 조회
**As a** 관리자 (Admin)
**I want to** 전체 사용자의 로그인 이력을 조회할 수 있다
**So that** 비정상 접근 시도를 모니터링하고 보안을 관리할 수 있다

**Priority**: Should

**Acceptance Criteria (Checklist)**:
- [ ] 로그인 시각, IP, 성공/실패, 실패 사유 표시
- [ ] 사용자별, 기간별, 성공/실패 필터링 가능
- [ ] 페이지네이션 지원

---

## Story-Persona Mapping

| Story | Root Admin | Admin | Write | Read |
|---|---|---|---|---|
| US-1.1 Login | ✅ | ✅ | ✅ | ✅ |
| US-1.2 Account Lock | ✅ | ✅ | - | - |
| US-1.3 Logout | ✅ | ✅ | ✅ | ✅ |
| US-2.1 User Registration | ✅ | ✅ | - | - |
| US-2.2 Permission Mgmt | ✅ | ✅ | - | - |
| US-2.3 User Deletion | ✅ | ✅ | - | - |
| US-2.4 Root Admin Init | ✅ | - | - | - |
| US-3.1 Search | ✅ | ✅ | ✅ | ✅ |
| US-4.1 Resource Create | ✅ | ✅ | ✅ | - |
| US-4.2 Resource Update | ✅ | ✅ | ✅ | - |
| US-4.3 Resource Delete | ✅ | ✅ | ✅ | - |
| US-5.1 Import | ✅ | ✅ | ✅ | - |
| US-5.2 Export | ✅ | ✅ | ✅ | ✅ |
| US-6.1 Deploy | ✅ | ✅ | - | - |
| US-7.1 All Logs | ✅ | ✅ | - | - |
| US-7.2 Own Logs | ✅ | ✅ | ✅ | ✅ |
| US-7.3 Login History | ✅ | ✅ | - | - |

---

## INVEST Criteria Verification

| Criterion | Status | Notes |
|---|---|---|
| **Independent** | ✅ | 각 스토리는 독립적으로 구현/테스트 가능 |
| **Negotiable** | ✅ | 구현 세부사항은 협의 가능 (AC에서 what만 정의) |
| **Valuable** | ✅ | 각 스토리가 사용자에게 직접적 가치 제공 |
| **Estimable** | ✅ | High-level이지만 범위가 명확하여 추정 가능 |
| **Small** | ✅ | Epic 단위이지만 각 스토리가 단일 워크플로우에 집중 |
| **Testable** | ✅ | 모든 스토리에 AC(GWT 또는 Checklist) 포함 |

---

## MoSCoW Priority Summary

| Priority | Stories |
|---|---|
| **Must** | US-1.1, US-1.2, US-1.3, US-2.1, US-2.2, US-2.3, US-2.4, US-3.1, US-4.1, US-4.2, US-4.3, US-5.1, US-5.2, US-6.1, US-7.1, US-7.2 |
| **Should** | US-7.3 |
| **Could** | — |
| **Won't** | — |

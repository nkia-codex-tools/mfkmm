# Unit of Work - Requirements Mapping

## Requirements to Unit Mapping

| Requirement ID | Requirement | Unit |
|---------------|-------------|------|
| FR-01.1 | 관리자 계정 (초기 생성, 권한 관리) | Unit 1: Auth |
| FR-01.2 | 일반 사용자 계정 (가입, 대기, 권한) | Unit 1: Auth |
| FR-01.3 | 신규 사용자 관리 (가입자 확인, 권한 부여) | Unit 1: Auth |
| FR-02.1 | 공통 UI (탭, 테이블, CRUD, 다중선택, D&D, 검색) | Unit 2: Resource |
| FR-02.2 | 기능(Functions) 관리 (15컬럼, 드롭다운, 자동생성) | Unit 2: Resource |
| FR-02.3 | 메뉴(Menus) 관리 (12컬럼, 자동완성, 자동채움) | Unit 2: Resource |
| FR-02.4 | 리소스 키(Message Resource) 관리 (15컬럼, 자동계산) | Unit 2: Resource |
| FR-03.1 | TSV Export (전체/선택, 헤더, UTF-8) | Unit 3: Import/Export |
| FR-03.2 | TSV Import (업로드, 검증, 미리보기, 충돌처리) | Unit 3: Import/Export |
| FR-04 | 변경 이력 관리 (자동 기록, 조회, 필터) | Unit 4: History |
| FR-05 | 버전 관리 (태깅, 스냅샷, 목록, diff, 롤백) | Unit 5: Version |

## NFR to Unit Mapping

| NFR | Requirement | Primary Unit | All Units |
|-----|------------|--------------|-----------|
| NFR-01 | Performance (가상 스크롤, <500ms) | Unit 2 | - |
| NFR-02 | Deployment (Docker Compose, EC2) | Unit 1 (초기 구조) | 최종 통합 |
| NFR-03 | Security (JWT, RBAC, rate limit, headers) | Unit 1 | 전체 적용 |
| NFR-04 | Testing (jqwik, fast-check, PBT) | - | 모든 유닛 |
| NFR-05 | Usability (인라인 편집, 하이라이트, D&D) | Unit 2 | - |

## Security Rules to Unit Mapping

| Rule | Description | Primary Unit |
|------|-------------|-------------|
| SECURITY-01 | Encryption at rest/transit | Unit 1 (HTTPS config) |
| SECURITY-03 | Application-level logging | Unit 1 (logging setup), 전체 적용 |
| SECURITY-04 | HTTP Security Headers | Unit 1 (SecurityHeadersFilter) |
| SECURITY-05 | Input validation | 모든 유닛 (각 Controller) |
| SECURITY-07 | Restrictive network config | Unit 1 (Docker/Nginx) |
| SECURITY-08 | Application-level access control | Unit 1 (설정), Unit 2-5 (적용) |
| SECURITY-09 | Security hardening | Unit 1 (설정), 전체 |
| SECURITY-10 | Supply chain security | 모든 유닛 (의존성 관리) |
| SECURITY-11 | Secure design principles | Unit 1 (rate limiting), 전체 |
| SECURITY-12 | Auth & credential management | Unit 1 |
| SECURITY-13 | Software/data integrity | Unit 3 (import 검증), Unit 5 (스냅샷) |
| SECURITY-14 | Alerting & monitoring | Unit 4 (logging), Unit 1 (설정) |
| SECURITY-15 | Exception handling | 모든 유닛 (GlobalExceptionHandler) |

## PBT Rules to Unit Mapping

| Rule | Description | Primary Unit |
|------|-------------|-------------|
| PBT-01 | Property identification | 모든 유닛 (Functional Design 시) |
| PBT-02 | Round-trip properties | Unit 3 (TSV serialize/deserialize), Unit 5 (snapshot/restore) |
| PBT-03 | Invariant properties | Unit 2 (reorder preserves elements), Unit 4 (history count) |
| PBT-04 | Idempotency | Unit 2 (reorder idempotent), Unit 5 (rollback idempotent) |
| PBT-05 | Oracle testing | Unit 2 (자동계산 필드 vs 수동 계산) |
| PBT-06 | Stateful testing | Unit 2 (리소스 상태), Unit 5 (버전 상태) |
| PBT-07 | Generator quality | 모든 유닛 |
| PBT-08 | Shrinking & reproducibility | 모든 유닛 |
| PBT-09 | Framework selection | Unit 1 (jqwik + fast-check 설정) |
| PBT-10 | Complementary testing | 모든 유닛 |

## Unit Delivery Milestones

| Milestone | Units Complete | User Can... |
|-----------|--------------|-------------|
| M1 | Unit 1 | 가입, 로그인, 관리자 권한 부여 |
| M2 | Unit 1 + 2 | 리소스 조회, 추가, 수정, 삭제, 순서 변경 |
| M3 | Unit 1 + 2 + 3 | TSV import/export |
| M4 | Unit 1 + 2 + 3 + 4 | 모든 변경 이력 조회 |
| M5 | Unit 1 + 2 + 3 + 4 + 5 | 버전 태깅, 비교, 롤백 (완성) |

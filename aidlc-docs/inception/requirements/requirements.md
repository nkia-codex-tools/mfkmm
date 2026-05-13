# MKFMM Requirements Document

## Intent Analysis

- **User Request**: 사내 개발 리소스 통합 관리 시스템 (MessageKey, FunctionID, MenuID Management) 구축
- **Request Type**: New Project (신규 프로젝트)
- **Scope Estimate**: System-wide (다중 컴포넌트: 프론트엔드, 백엔드, 데이터베이스)
- **Complexity Estimate**: Complex (권한 관리, 이력 추적, 유사도 탐지, IMPORT/EXPORT, 배포 관리)

---

## 1. Technical Stack Decisions

| Layer | Technology | Rationale |
|---|---|---|
| Frontend | React (TypeScript) | 사용자 선택 |
| UI Framework | Tailwind CSS + Headless UI | 사용자 선택 |
| Backend | Java (Spring Boot) | 사용자 선택 |
| Database | MongoDB (NoSQL) | 사용자 선택 |
| Containerization | Docker | 배포 환경 미정, 컨테이너 기반 |
| PBT Framework (FE) | fast-check | PBT-09: JS/TS용 PBT 프레임워크 |
| PBT Framework (BE) | jqwik | PBT-09: Java용 PBT 프레임워크 |

---

## 2. Functional Requirements

### 2.1 사용자 인증 및 세션 관리

| ID | Requirement | Detail |
|---|---|---|
| FR-AUTH-01 | 로그인 | 사용자 ID / 비밀번호 기반 인증 |
| FR-AUTH-02 | 로그아웃 | 세션 무효화 |
| FR-AUTH-03 | 세션 관리 | 30분 만료, 세션 기반 인증 |
| FR-AUTH-04 | 브루트포스 방지 | 3회 실패 시 계정 잠금, 관리자 수동 해제 필요 |
| FR-AUTH-05 | 로그인 이력 | 시각, IP, 성공/실패 여부, 실패 사유 기록 |
| FR-AUTH-06 | 비밀번호 저장 | bcrypt 해싱 |

### 2.2 사용자 관리

| ID | Requirement | Detail |
|---|---|---|
| FR-USER-01 | 사용자 등록 | 관리자만 수행 가능 |
| FR-USER-02 | 사용자 정보 변경 | 본인 또는 관리자 |
| FR-USER-03 | 사용자 삭제 | 관리자만 수행, 최상위 관리자 삭제 불가 |
| FR-USER-04 | 사용자 목록 조회 | 검색 및 필터링 |
| FR-USER-05 | 시스템 최상위 관리자 | 시스템 초기 생성, 변경/삭제 불가, 환경 변수로 초기 인증 정보 설정 |

### 2.3 권한 관리

| ID | Requirement | Detail |
|---|---|---|
| FR-ROLE-01 | 권한 종류 | Read, Write, Admin |
| FR-ROLE-02 | 권한 부여 | 관리자만 타 사용자에게 부여 가능 |
| FR-ROLE-03 | 권한 회수 | 관리자가 권한 회수 가능 |
| FR-ROLE-04 | 감사 로그 | 권한 변경 시 감사 로그 기록 |

### 2.4 리소스 관리

| ID | Requirement | Detail |
|---|---|---|
| FR-RES-01 | 관리 대상 | 메시지 리소스 키, 기능 ID, 메뉴 ID, 내용 |
| FR-RES-02 | 공통 속성 | 식별자, 내용, 설명/비고, 등록자, 등록 시각, 최종 수정자, 최종 수정 시각 |
| FR-RES-03 | 검색 | 키워드 검색(Key, ID, 내용), 유형별 필터, 메타데이터 필터, 페이지네이션/정렬 |
| FR-RES-04 | 등록 | 중복 검증 + 유사 정보 제안 연동 후 저장 |
| FR-RES-05 | 수정 | 변경 전/후 이력 기록 |
| FR-RES-06 | 삭제 | Soft Delete, 30일 후 Hard Delete |

### 2.5 데이터 무결성 (중복 확인 / 유사 제안)

| ID | Requirement | Detail |
|---|---|---|
| FR-DUP-01 | 중복 검사 대상 | 메시지 리소스 키, 기능 ID, 메뉴 ID, 내용 |
| FR-DUP-02 | 중복 발견 시 | 사용자에게 명확한 알림, 등록 차단 또는 확인 후 진행 |
| FR-SIM-01 | 유사 탐지 방식 | 문자열 부분 일치 + Levenshtein 편집 거리 조합 |
| FR-SIM-02 | 유사 후보 표시 | 후보 리스트 + 유사도 점수 표시 |
| FR-SIM-03 | 사용 여부 선택 | 기존 리소스 사용 / 신규 등록 강행 (사유 입력 가능) |
| FR-SIM-04 | 선택 결과 이력 | 유사 제안 선택 결과를 이력에 기록 |

### 2.6 작업 이력 관리

| ID | Requirement | Detail |
|---|---|---|
| FR-LOG-01 | 이력 대상 | 검색, 추가, 변경, 삭제, IMPORT, EXPORT |
| FR-LOG-02 | 이력 항목 | 작업 종류, 작업자, 시각, 대상 식별자, 변경 전/후, 결과(성공/실패, 사유) |
| FR-LOG-03 | 이력 조회 | 작업 종류/작업자/기간/리소스 유형별 필터, 페이지네이션/정렬 |
| FR-LOG-04 | 이력 보존 | 검색 이력 6개월 보존 후 자동 삭제 |

### 2.7 IMPORT / EXPORT

| ID | Requirement | Detail |
|---|---|---|
| FR-IMP-01 | IMPORT 형식 | Excel(.xlsx), TSV, JSON |
| FR-IMP-02 | IMPORT 검증 | 중복 검증 수행 |
| FR-IMP-03 | 충돌 처리 | 건너뛰기 / 덮어쓰기 / 사용자 확인 선택 |
| FR-IMP-04 | IMPORT 결과 | 성공/실패/스킵 건수 리포트 |
| FR-IMP-05 | 파일 크기 제한 | 5MB 이하 |
| FR-EXP-01 | 부분 EXPORT | 검색 결과 또는 선택 데이터 |
| FR-EXP-02 | 전체 EXPORT | 전체 리소스 일괄 |
| FR-EXP-03 | EXPORT 형식 | Excel(.xlsx), TSV, JSON |

### 2.8 배포 관리

| ID | Requirement | Detail |
|---|---|---|
| FR-DEP-01 | 배포 정의 | 전체 데이터 EXPORT = 배포 |
| FR-DEP-02 | 배포 형식 | Excel, TSV, JSON 중 선택 |
| FR-DEP-03 | 배포 이력 | 시각, 작업자, 형식, 범위, 건수, 버전 |
| FR-DEP-04 | 재다운로드 | 배포 이력에서 다운로드 재실행 가능 |

---

## 3. Non-Functional Requirements

| ID | Category | Requirement | Detail |
|---|---|---|---|
| NFR-01 | Scale | 동시 사용자 | 10~50명 |
| NFR-02 | Security | 비밀번호 | bcrypt 해싱 저장 |
| NFR-03 | Security | 세션 만료 | 30분 |
| NFR-04 | Security | 계정 잠금 | 3회 실패 시 관리자 수동 해제 |
| NFR-05 | Data | 삭제 정책 | Soft Delete + 30일 후 Hard Delete |
| NFR-06 | Data | 이력 보존 | 검색 이력 6개월, 기타 이력 무기한 |
| NFR-07 | Data | IMPORT 제한 | 파일 5MB 이하 |
| NFR-08 | Deployment | 컨테이너화 | Docker 기반 배포 |
| NFR-09 | Testing | PBT | Property-Based Testing 적용 (jqwik/fast-check) |

---

## 4. Constraints (제외 기능)

constraints.md에 명시된 모든 항목은 구현 범위에서 제외:
- SSO, 2FA, LDAP 등 고급 인증
- 이메일/메신저/SMS 알림
- Git/Jira 등 외부 시스템 연동
- 댓글, 멘션, 승인 워크플로우 등 협업 기능
- AI 기반 의미 유사도, 자동 번역, 자연어 검색
- 사용 통계 대시보드, 시각화
- Git 수준 버전 관리, 브랜치/머지
- 시스템 UI 다국어, 자동 백업, 모바일 앱
- SaaS 과금, 고급 권한(필드/행/그룹 단위), 파일 첨부
- 스케줄 EXPORT, 외부 연동 EXPORT, Public API

---

## 5. User Types Summary

| Type | Permissions | Notes |
|---|---|---|
| Root Admin | All | 시스템 자동 생성, 변경/삭제 불가 |
| Admin | All | 사용자 관리 + 권한 부여 가능 |
| Write User | Search, CRUD, Import, Export | 개발자용 |
| Read User | Search, Export | 조회 전용 |

---

## 6. Key Architecture Decisions

1. **Frontend**: React + TypeScript + Tailwind CSS + Headless UI
2. **Backend**: Java Spring Boot (REST API)
3. **Database**: MongoDB (문서 기반, 유연한 리소스 스키마)
4. **Auth**: 세션 기반 인증 (30분 만료)
5. **Root Admin**: 환경 변수로 초기 인증 정보 주입
6. **Similarity**: 문자열 부분 일치 + Levenshtein 편집 거리 조합
7. **Delete Policy**: Soft Delete + 30일 후 자동 Hard Delete
8. **Testing**: PBT 적용 (jqwik for Java, fast-check for TypeScript)
9. **Deployment**: Docker 컨테이너 기반

# MKFMM User Personas

## Persona 1: Root Administrator (시스템 최상위 관리자)

| Attribute | Description |
|---|---|
| **Name** | 시스템 운영자 |
| **Role** | Root Admin |
| **Goal** | 시스템 안정적 운영, 최초 관리자 계정 체계 수립 |
| **Characteristics** | 시스템 초기 구축 시 자동 생성, 변경/삭제 불가, 모든 권한 보유 |
| **Motivation** | 조직의 리소스 거버넌스 기반 마련 |
| **Frustration** | 시스템 접근 불가 상황, 관리자 부재 시 권한 문제 |
| **Tech Level** | 높음 (인프라/시스템 운영 경험) |

---

## Persona 2: Administrator (관리자)

| Attribute | Description |
|---|---|
| **Name** | 팀 리더 / 프로젝트 관리자 |
| **Role** | Admin |
| **Goal** | 팀원 계정/권한 관리, 리소스 배포 통제, 이력 감사 |
| **Characteristics** | 모든 권한 보유, 사용자 등록/변경/삭제, 권한 부여/회수 가능 |
| **Motivation** | 팀 내 리소스 사용 표준화, 변경 이력 추적, 배포 관리 |
| **Frustration** | 무분별한 리소스 등록, 중복 데이터, 누가 무엇을 변경했는지 파악 불가 |
| **Tech Level** | 중~높음 (개발 경험 + 관리 역할) |

---

## Persona 3: Write User (개발자)

| Attribute | Description |
|---|---|
| **Name** | 사내 개발자 |
| **Role** | Write |
| **Goal** | 빠르게 리소스 검색/등록/수정, 일괄 데이터 처리, 중복 방지 |
| **Characteristics** | 리소스 CRUD, IMPORT/EXPORT 가능, 사용자 관리 불가 |
| **Motivation** | 동료가 등록한 리소스 재사용, 표준화된 리소스로 코드 일관성 확보 |
| **Frustration** | 중복 리소스 발견, 유사한 것이 이미 있는지 모름, 수작업 데이터 입력 |
| **Tech Level** | 높음 (소프트웨어 개발자) |

---

## Persona 4: Read User (조회 사용자)

| Attribute | Description |
|---|---|
| **Name** | QA 엔지니어 / 기획자 / 신규 입사자 |
| **Role** | Read |
| **Goal** | 현재 등록된 리소스 검색/확인, 데이터 EXPORT로 참고 |
| **Characteristics** | 검색 및 EXPORT만 가능, 등록/수정/삭제/IMPORT 불가 |
| **Motivation** | 정확한 리소스 정보 확인, 외부 문서 작성 시 참고 데이터 확보 |
| **Frustration** | 원하는 리소스를 빠르게 찾지 못함, 최신 데이터인지 확신 불가 |
| **Tech Level** | 중간 (기본 웹 사용 가능) |

---

## Persona-Permission Matrix

| Action | Root Admin | Admin | Write | Read |
|---|---|---|---|---|
| Login/Logout | ✅ | ✅ | ✅ | ✅ |
| Search Resources | ✅ | ✅ | ✅ | ✅ |
| Create/Update/Delete Resources | ✅ | ✅ | ✅ | ❌ |
| IMPORT | ✅ | ✅ | ✅ | ❌ |
| EXPORT | ✅ | ✅ | ✅ | ✅ |
| User Management | ✅ | ✅ | ❌ | ❌ |
| Permission Management | ✅ | ✅ | ❌ | ❌ |
| Deploy (Full Export) | ✅ | ✅ | ❌ | ❌ |
| View All Logs | ✅ | ✅ | ❌ | ❌ |
| View Own Logs | ✅ | ✅ | ✅ | ✅ |

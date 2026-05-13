# MKFMM Application Design - Consolidated

## 1. Architecture Overview

**Pattern**: Hexagonal Architecture (Ports & Adapters)
- **Frontend**: React + TypeScript + Tailwind CSS + Headless UI
- **Backend**: Java Spring Boot (Hexagonal)
- **Database**: MongoDB
- **API**: REST + OpenAPI 3.0 (Swagger)
- **State Management**: React Context + useReducer
- **Session**: Spring Session + MongoDB (30분 만료)
- **Deployment**: Docker Compose

---

## 2. Design Decisions Summary

| Decision | Choice | Rationale |
|---|---|---|
| Backend Architecture | Hexagonal (Ports & Adapters) | 도메인 중심 설계, 외부 의존성 분리, 테스트 용이 |
| Frontend State | React Context + useReducer | 10~50명 규모, 경량 상태 관리 충분 |
| API Specification | REST + OpenAPI | 명세 기반 개발, 클라이언트 코드 자동 생성 |
| Similarity Location | 서비스 레이어 직접 계산 | 단순 구현, 별도 서비스 불필요 |
| File Processing | 하이브리드 (5MB↓ 동기, 5MB↑ 비동기) | IMPORT=항상 동기(5MB 제한), EXPORT=크기에 따라 |
| Session Store | MongoDB | 별도 인프라 불필요, Docker 단순화 |

---

## 3. Backend Components (7개)

| # | Component | Package | Responsibility |
|---|---|---|---|
| 1 | Auth | `com.mkfmm.auth` | 인증, 세션, 로그인 이력, 계정 잠금 |
| 2 | User | `com.mkfmm.user` | 사용자 CRUD |
| 3 | Permission | `com.mkfmm.user` | 권한 관리, 감사 로그 |
| 4 | Resource | `com.mkfmm.resource` | 리소스 CRUD, 검색 |
| 5 | Similarity | `com.mkfmm.similarity` | 중복 검사, 유사도 탐지 |
| 6 | DataIO | `com.mkfmm.dataio` | IMPORT/EXPORT 처리 |
| 7 | Deploy | `com.mkfmm.deploy` | 배포 관리 |
| 8 | History | `com.mkfmm.history` | 작업 이력 |

**Cross-Cutting**:
- SecurityFilter (인증/인가 필터)
- ScheduledTaskService (Hard Delete, 이력 정리, 세션 정리)

---

## 4. Frontend Modules (5개)

| # | Module | Path | Pages |
|---|---|---|---|
| 1 | Auth | `src/features/auth/` | LoginPage |
| 2 | Resource | `src/features/resource/` | List, Detail, Form |
| 3 | DataIO | `src/features/dataio/` | Import, Export |
| 4 | Admin | `src/features/admin/` | Users, Permissions, Deploy, History, LoginHistory |
| 5 | Shared | `src/shared/` | Layout, Navigation, Pagination, Modal, Toast |

---

## 5. Key Interaction Flows

### Resource Registration (핵심 워크플로우)
1. 사용자가 리소스 정보 입력
2. → 중복 검사 (exact match)
3. → 중복 발견: 등록 차단 + 알림
4. → 유사 정보 탐지 (부분 일치 + Levenshtein)
5. → 유사 후보 표시 (점수 포함)
6. → 사용자 선택: 기존 사용 or 신규 등록
7. → 저장 + 이력 기록

### Import Flow
1. 파일 업로드 (5MB 이하 검증)
2. → 파싱 (Excel/TSV/JSON)
3. → 각 행 중복 검증
4. → 충돌 정책 적용 (건너뛰기/덮어쓰기/확인)
5. → 저장 + 결과 리포트 + 이력 기록

---

## 6. MongoDB Collections (예상)

| Collection | Purpose |
|---|---|
| `users` | 사용자 계정, 권한, 잠금 상태 |
| `resources` | 리소스 데이터 (메시지키/기능ID/메뉴ID) |
| `sessions` | Spring Session 저장 |
| `login_history` | 로그인 이력 |
| `work_logs` | 작업 이력 (검색/CRUD/IMPORT/EXPORT) |
| `audit_logs` | 권한 변경 감사 로그 |
| `deployments` | 배포 이력 + 파일 참조 |

---

## 7. PBT Applicability Notes (PBT-09)

| Layer | Framework | Applicable Areas |
|---|---|---|
| Backend (Java) | jqwik | Levenshtein 계산, 리소스 직렬화, Import 파싱, 권한 검증 로직 |
| Frontend (TypeScript) | fast-check | API 응답 파싱, 검색 쿼리 변환, 폼 유효성 검증 |

---

## Related Documents
- [components.md](components.md) — 컴포넌트 상세 정의
- [component-methods.md](component-methods.md) — 메서드 시그니처
- [services.md](services.md) — 서비스 레이어 설계
- [component-dependency.md](component-dependency.md) — 의존성 관계

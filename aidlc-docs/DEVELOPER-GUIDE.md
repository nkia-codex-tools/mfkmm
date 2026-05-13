# MKFMM Developer Guide

## 프로젝트 개요
- **프로젝트**: 사내 개발 리소스 통합 관리 시스템 (MKFMM)
- **아키텍처**: Microservices + Event-Driven (RabbitMQ) + API Gateway + JWT
- **기술 스택**: Spring Boot (BE) / React + TypeScript + Tailwind (FE) / MongoDB / Docker

---

## 개발자 분배

| Developer | Units | 담당 범위 |
|---|---|---|
| **Dev 1** | Auth Service, API Gateway, Shared Library | 기반 인프라 (인증, JWT, 라우팅, 공유 라이브러리) |
| **Dev 2** | User Service, Resource Service, History Service | 핵심 도메인 (사용자/권한, 리소스/유사도, 이력) |
| **Dev 3** | DataIO Service, Deploy Service, Frontend | 데이터 처리 (IMPORT/EXPORT, 배포) + 전체 UI |

---

## 현재 진행 상태

### 완료된 단계 (공통)
- [x] INCEPTION - Workspace Detection
- [x] INCEPTION - Requirements Analysis
- [x] INCEPTION - User Stories
- [x] INCEPTION - Workflow Planning
- [x] INCEPTION - Application Design
- [x] INCEPTION - Units Generation

### Per-Unit 진행 상태

| Unit | Functional Design | NFR Requirements | NFR Design | Infra Design | Code Gen |
|---|---|---|---|---|---|
| Auth + Gateway + Shared (Dev 1) | ✅ Complete | ⏸️ 질문 생성됨 (미답변) | ❌ | ❌ | ❌ |
| User Service (Dev 2) | ⏸️ 질문 생성됨 (미답변) | ❌ | ❌ | ❌ | ❌ |
| Resource Service (Dev 2) | ❌ | ❌ | ❌ | ❌ | ❌ |
| History Service (Dev 2) | ❌ | ❌ | ❌ | ❌ | ❌ |
| DataIO Service (Dev 3) | ❌ | ❌ | ❌ | ❌ | ❌ |
| Deploy Service (Dev 3) | ❌ | ❌ | ❌ | ❌ | ❌ |
| Frontend (Dev 3) | ❌ | ❌ | ❌ | ❌ | ❌ |

---

## 각 개발자 시작 방법

### 공통
1. `git pull`로 최신 상태 받기
2. AI-DLC 워크플로우에서 "이어서 진행" 요청
3. AI가 `aidlc-docs/aidlc-state.md`를 읽고 현재 상태에서 재개

### Dev 1 (Auth + Gateway + Shared)
**현재 상태**: Functional Design 완료, NFR Requirements 질문 대기

**재개 방법**:
```
"Unit 1 (Auth Service + API Gateway + Shared Library)의 NFR Requirements를 이어서 진행해줘"
```

**미답변 질문 파일**: `aidlc-docs/construction/plans/auth-gateway-shared-nfr-requirements-plan.md`

**완료된 설계 문서**:
- `aidlc-docs/construction/auth-gateway-shared/functional-design/domain-entities.md`
- `aidlc-docs/construction/auth-gateway-shared/functional-design/business-rules.md`
- `aidlc-docs/construction/auth-gateway-shared/functional-design/business-logic-model.md`

---

### Dev 2 (User + Resource + History)
**현재 상태**: User Service Functional Design 질문 대기, Resource/History 미시작

**재개 방법**:
```
"Unit 2 (User Service)의 Functional Design을 이어서 진행해줘"
```

**미답변 질문 파일**: `aidlc-docs/construction/plans/user-service-functional-design-plan.md`

**이후 진행 순서**:
1. User Service: Functional Design → NFR Requirements → NFR Design → Infra Design → Code Generation
2. Resource Service: Functional Design → ... → Code Generation
3. History Service: Functional Design → ... → Code Generation

---

### Dev 3 (DataIO + Deploy + Frontend)
**현재 상태**: 아직 미시작

**시작 방법**:
```
"Unit 5 (DataIO Service)의 Functional Design을 시작해줘"
```
또는 Frontend부터 시작하고 싶다면:
```
"Unit 8 (Frontend)의 Functional Design을 시작해줘"
```

**이후 진행 순서**:
1. DataIO Service: Functional Design → ... → Code Generation
2. Deploy Service: Functional Design → ... → Code Generation
3. Frontend: Functional Design → ... → Code Generation

---

## 의존성 및 조율 사항

### 우선 확정 필요 (Dev 1 → All)
- **Shared Library** 이벤트 스키마 (`backend/shared/` 구조)
- **JWT Claims** 구조 및 검증 방식
- **API Gateway** 라우팅 규칙

### API 스펙 합의 (Dev 2 → Dev 3)
- Resource Service OpenAPI 스펙 → Frontend 연동
- History Service API → Frontend Admin 화면

### 인프라 공유 (공통)
- Docker Compose 설정
- MongoDB 컬렉션 naming convention
- RabbitMQ Exchange/Queue naming convention

---

## 주요 참고 문서

| 문서 | 경로 | 내용 |
|---|---|---|
| 요구사항 | `aidlc-docs/inception/requirements/requirements.md` | 기능/비기능 요구사항 |
| 제외사항 | `requirements/constraints.md` | 구현하지 않는 기능 목록 |
| User Stories | `aidlc-docs/inception/user-stories/stories.md` | 17개 스토리 + AC |
| Personas | `aidlc-docs/inception/user-stories/personas.md` | 4가지 사용자 유형 |
| Application Design | `aidlc-docs/inception/application-design/application-design.md` | 통합 설계 문서 |
| Components | `aidlc-docs/inception/application-design/components.md` | 컴포넌트 정의 |
| Services | `aidlc-docs/inception/application-design/services.md` | 서비스 레이어 |
| Unit of Work | `aidlc-docs/inception/application-design/unit-of-work.md` | 유닛 정의 + 디렉토리 구조 |
| Dependencies | `aidlc-docs/inception/application-design/unit-of-work-dependency.md` | 유닛 간 의존성 |
| Story Map | `aidlc-docs/inception/application-design/unit-of-work-story-map.md` | 스토리-유닛 매핑 |
| Execution Plan | `aidlc-docs/inception/plans/execution-plan.md` | 전체 실행 계획 |
| AI-DLC State | `aidlc-docs/aidlc-state.md` | 워크플로우 진행 상태 |
| Audit Log | `aidlc-docs/audit.md` | 모든 의사결정 이력 |

---

## AI-DLC 워크플로우 재개 시 주의사항

1. **항상 CLAUDE.md 워크플로우를 따릅니다** — AI가 자동으로 인식
2. **aidlc-state.md를 먼저 확인** — 현재 진행 상태 파악
3. **질문 파일에 답변** — [Answer]: 태그 뒤에 옵션 문자 기입
4. **승인/변경 요청** — 각 단계 완료 시 "Continue to Next Stage" 또는 변경 요청
5. **PBT 규칙 적용** — 모든 코드에 Property-Based Testing 적용 (jqwik/fast-check)

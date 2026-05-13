# Functional Design Plan - User Service

## Unit Context
- **Unit**: User Service (Phase 2)
- **Stories**: US-2.1 (사용자 등록), US-2.2 (권한 관리), US-2.3 (사용자 삭제), US-2.4 (Root Admin 초기화 - 계정 부분), US-1.2 (계정 잠금 해제 - Admin 기능)
- **Responsibility**: 사용자 CRUD, 권한(Role) 관리, 감사 로그
- **Events Published**: UserCreated, UserDeleted, PermissionChanged

## Planning Questions

아래 질문들에 답변해 주세요.

---

## Question 1
사용자 정보에 포함할 필드는? (필수 필드 외 추가 정보)

A) 최소 — userId, name, role만 (이메일/부서 없음)
B) 기본 — userId, name, email, role
C) 확장 — userId, name, email, department, role, 메모
X) Other (please describe after [Answer]: tag below)

[Answer]: 

## Question 2
사용자 목록 조회 시 Admin에게 보여줄 정보 범위는?

A) 전체 정보 (ID, 이름, 이메일, 부서, 권한, 생성일, 잠금 상태)
B) 기본 정보 (ID, 이름, 권한, 잠금 상태)
C) 관리에 필요한 정보만 (ID, 이름, 권한, 마지막 로그인)
X) Other (please describe after [Answer]: tag below)

[Answer]: 

## Question 3
사용자 삭제 시 해당 사용자의 기존 작업 이력은?

A) 이력은 그대로 보존 (삭제된 사용자 ID로 표시)
B) 이력의 사용자 정보를 "삭제된 사용자"로 익명화
C) 이력도 함께 삭제
X) Other (please describe after [Answer]: tag below)

[Answer]: 

## Question 4
사용자 등록 시 초기 비밀번호 정책은?

A) 관리자가 임시 비밀번호를 직접 지정
B) 시스템이 랜덤 임시 비밀번호 자동 생성 후 표시
C) 관리자가 지정 + 첫 로그인 시 비밀번호 변경 강제
X) Other (please describe after [Answer]: tag below)

[Answer]: 

---

## Functional Design Execution Plan

- [ ] Step 1: Domain Entities 설계 (User, AuditLog)
- [ ] Step 2: Business Rules 정의 (등록, 삭제, 권한 부여/회수 규칙)
- [ ] Step 3: Business Logic Model (사용자 CRUD 흐름, 권한 관리 흐름)
- [ ] Step 4: PBT-01 Testable Properties 식별

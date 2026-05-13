# Code Generation Plan - User Service

## Unit Context
- **Unit**: User Service
- **Path**: `backend/user-service/`
- **Stories**: US-2.1 (사용자 등록), US-2.2 (권한 관리), US-2.3 (사용자 삭제), US-2.4 (Root Admin 계정 부분), US-1.2 (잠금 해제)
- **Dependencies**: Shared Library (이벤트 스키마, DTO, exceptions)
- **Database**: MongoDB (user-db) — users, audit_logs collections
- **Messaging**: RabbitMQ (mkfmm.user exchange, DLQ)
- **Architecture**: Hexagonal (Ports & Adapters)

## Code Generation Steps

### Phase A: Project Structure Setup
- [x] Step 1: `backend/user-service/build.gradle.kts` — 의존성 설정 (Spring Boot, MongoDB, AMQP, Validation, jqwik)
- [x] Step 2: `backend/user-service/Dockerfile` — 컨테이너 빌드 설정
- [x] Step 3: `backend/user-service/src/main/resources/application.yml` — 설정 파일
- [x] Step 4: `backend/user-service/src/main/java/com/mkfmm/user/UserServiceApplication.java` — 엔트리포인트

### Phase B: Domain Layer
- [x] Step 5: `domain/model/User.java` — User 엔티티 (확장 필드: email, department, memo, mustChangePassword)
- [x] Step 6: `domain/model/Role.java` — Role enum (Shared에서 재사용 가능하면 import)
- [x] Step 7: `domain/model/AuditLog.java` — AuditLog 엔티티
- [x] Step 8: `domain/model/AuditAction.java` — AuditAction enum

### Phase C: Application Layer (Ports)
- [x] Step 9: `application/port/inbound/UserManagementUseCase.java` — 사용자 CRUD 유스케이스 인터페이스
- [x] Step 10: `application/port/inbound/PermissionUseCase.java` — 권한 관리 유스케이스 인터페이스
- [x] Step 11: `application/port/outbound/UserRepository.java` — 사용자 리포지토리 포트
- [x] Step 12: `application/port/outbound/AuditLogRepository.java` — 감사 로그 리포지토리 포트
- [x] Step 13: `application/port/outbound/EventPublisherPort.java` — 이벤트 발행 포트

### Phase D: Application Layer (Services)
- [x] Step 14: `application/service/UserManagementService.java` — 사용자 CRUD 구현 (US-2.1, US-2.3, US-2.4)
- [x] Step 15: `application/service/PermissionService.java` — 권한 부여/회수 구현 (US-2.2)
- [x] Step 16: `application/service/AccountUnlockService.java` — 계정 잠금 해제 구현 (US-1.2)
- [x] Step 17: `application/service/AuditLogService.java` — 감사 로그 조회 + 보존 정책 스케줄

### Phase E: Adapter Layer (Inbound - Controllers)
- [x] Step 18: `adapter/inbound/controller/UserController.java` — 사용자 CRUD REST API
- [x] Step 19: `adapter/inbound/controller/PermissionController.java` — 권한 관리 REST API
- [x] Step 20: `adapter/inbound/controller/AuditLogController.java` — 감사 로그 조회 API
- [x] Step 21: `adapter/inbound/controller/DlqController.java` — DLQ 관리 API (Admin)
- [x] Step 22: `adapter/inbound/controller/GlobalExceptionHandler.java` — 예외 처리
- [x] Step 23: `adapter/inbound/dto/*.java` — Request/Response DTO 클래스들

### Phase F: Adapter Layer (Outbound - Persistence)
- [x] Step 24: `adapter/outbound/persistence/document/UserDocument.java` — MongoDB 문서
- [x] Step 25: `adapter/outbound/persistence/document/AuditLogDocument.java` — MongoDB 문서
- [x] Step 26: `adapter/outbound/persistence/repository/SpringDataUserRepository.java` — Spring Data 인터페이스
- [x] Step 27: `adapter/outbound/persistence/repository/SpringDataAuditLogRepository.java` — Spring Data 인터페이스
- [x] Step 28: `adapter/outbound/persistence/MongoUserRepositoryAdapter.java` — 리포지토리 어댑터
- [x] Step 29: `adapter/outbound/persistence/MongoAuditLogRepositoryAdapter.java` — 리포지토리 어댑터

### Phase G: Adapter Layer (Outbound - Messaging)
- [x] Step 30: `adapter/outbound/messaging/RabbitEventPublisher.java` — RabbitMQ 이벤트 발행
- [x] Step 31: `config/RabbitConfig.java` — Exchange, Queue, DLQ 설정

### Phase H: Configuration
- [x] Step 32: `config/MongoConfig.java` — MongoDB 인덱스 초기화
- [x] Step 33: `config/SchedulerConfig.java` — AuditLog 삭제 스케줄 설정
- [x] Step 34: Shared Library 이벤트 추가 — `UserEvents.java` (UserCreated, UserDeleted, PermissionChanged)

### Phase I: Tests (PBT + Example-based)
- [x] Step 35: `test/UserManagementServiceTest.java` — 핵심 비즈니스 로직 단위 테스트
- [x] Step 36: `test/PermissionServiceTest.java` — 권한 로직 단위 테스트
- [x] Step 37: `test/UserPermissionPropertyTest.java` — PBT: Root immutability, self-demotion block, role hierarchy
- [x] Step 38: `test/AuditLogPropertyTest.java` — PBT: Audit completeness invariant
- [x] Step 39: `test/UserValidationPropertyTest.java` — PBT: userId 형식 검증, uniqueness

### Phase J: Documentation
- [x] Step 40: `aidlc-docs/construction/user-service/code/code-summary.md` — 코드 생성 요약

---

## Story Traceability

| Story | Implemented In |
|---|---|
| US-2.1 사용자 등록 | Step 14 (Service), Step 18 (Controller) |
| US-2.2 권한 관리 | Step 15 (Service), Step 19 (Controller) |
| US-2.3 사용자 삭제 | Step 14 (Service), Step 18 (Controller) |
| US-2.4 Root Admin 계정 | Step 14 (Service — init 로직) |
| US-1.2 잠금 해제 | Step 16 (Service), Step 18 (Controller) |

## PBT Compliance (PBT-01 → Code)

| Property | Test File | Rule |
|---|---|---|
| Root immutability | Step 37 | PBT-03 (Invariant) |
| Self-demotion block | Step 37 | PBT-03 (Invariant) |
| Role hierarchy | Step 37 | PBT-03 (Invariant) |
| Audit completeness | Step 38 | PBT-03 (Invariant) |
| userId format validation | Step 39 | PBT-03 (Invariant) |
| Unlock idempotence | Step 37 | PBT-04 (Idempotence) |

# NFR Design Patterns - User Service

## 1. Retry Pattern (이벤트 발행 재시도)

**적용 대상**: UserDeleted, PermissionChanged 등 타 서비스에 영향 주는 이벤트

**패턴**:
```
Event Publish → Success? → Done
                  ↓ No
              Retry (Exponential Backoff)
              1s → 2s → 4s (최대 3회)
                  ↓ All Failed
              Move to DLQ
```

**구현 방식**:
- Spring AMQP `RetryTemplate` 설정
- `initial-interval`: 1000ms
- `multiplier`: 2.0
- `max-attempts`: 3
- 최종 실패 시 `x-dead-letter-exchange`로 라우팅

---

## 2. Dead Letter Queue (DLQ) Pattern

**적용 대상**: 3회 재시도 후에도 실패한 모든 이벤트 메시지

**패턴**:
```
Main Queue → Consumer 처리 실패 (3회) → DLQ
                                          ↓
                                    Admin UI에서 확인
                                          ↓
                                    수동 재처리 또는 폐기
```

**DLQ 관리**:
- Queue: `mkfmm.user.dlq`
- Admin UI에서 DLQ 메시지 목록 조회 가능
- 재처리 버튼: DLQ 메시지를 원래 큐로 재발행
- 폐기 버튼: DLQ 메시지 삭제
- DLQ 메시지 보존: 무기한 (수동 처리까지)

---

## 3. Validation Pattern (입력 검증)

**적용 대상**: 모든 Inbound API 요청

**패턴**: Fail-Fast Validation
```
Request → Controller (@Valid) → 검증 실패 → 400 Bad Request
                              → 검증 성공 → Service Layer
```

**검증 규칙**:
| Field | Rule | Annotation |
|---|---|---|
| userId | Not blank, 영숫자 | @NotBlank, @Pattern(regexp="^[a-zA-Z0-9]+$") |
| name | Not blank | @NotBlank |
| role | Not null, 유효값 | @NotNull |
| email | Email 형식 (optional) | @Email (값이 있을 때만) |

---

## 4. Data Retention Pattern (감사 로그 보존)

**적용 대상**: AuditLog collection

**패턴**: Scheduled Purge
```
Scheduler (매일 02:00) → Query: performedAt < (now - 730 days) → Delete
```

**구현 방식**:
- Spring `@Scheduled(cron = "0 0 2 * * *")` — 매일 새벽 2시
- Batch 삭제: 한 번에 최대 1000건씩 반복 삭제 (대량 삭제 시 DB 부하 방지)
- 삭제 결과 로그 기록 (삭제된 건수)

---

## 5. Eventually Consistent Pattern (사용자 삭제 일관성)

**적용 대상**: 사용자 삭제 시 타 서비스 데이터 정리

**패턴**:
```
User Service: Delete User → Publish UserDeleted event
                                    ↓
History Service: 해당 사용자 이력 삭제 (이벤트 수신)
Auth Service: 인증 정보 + RefreshToken 삭제 (이벤트 수신)
```

**일관성 보장**:
- 이벤트는 At-least-once 전달 (RabbitMQ ack 기반)
- 소비자는 idempotent하게 구현 (중복 이벤트 무해)
- 실패 시 재시도 → DLQ → Admin 수동 처리

---

## 6. Immutable Audit Pattern (감사 로그 무결성)

**적용 대상**: AuditLog

**패턴**:
- MongoDB에서 AuditLog collection은 insert-only
- Application 레벨에서 update/delete 메서드 제공하지 않음
- Repository 인터페이스: `save()`만 노출, `delete()` 미제공
- 보존 기간 만료 삭제만 별도 스케줄러가 직접 DB 쿼리로 수행

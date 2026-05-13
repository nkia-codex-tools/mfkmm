# Business Logic Model - History Service

## 1. Event Consumption Flow

```
Input: BaseEvent from RabbitMQ queue

1. Parse event (eventId, eventType, timestamp, userId, payload)
2. Check sourceEvent duplication:
   - Query: exists by sourceEvent == eventId
   - EXISTS → discard (idempotent, log at DEBUG)
3. Map event to WorkLog:
   - Determine WorkLogType from eventType
   - Extract fields from payload (resourceId, resourceKey, previousValue, newValue, details)
   - Set performedAt = event.timestamp
   - Set success = true
   - Set sourceEvent = event.eventId
4. Save WorkLog
5. ACK message (manual ack)
```

## 2. UserDeleted Event Handler

```
Input: UserDeleted event { userId, deletedBy }

1. Standard flow: Save WorkLog (type=USER_DELETED)
2. Mark for deletion:
   - Update all WorkLogs where userId == deletedUserId
   - Set markedForDeletion = true
   - (실제 삭제는 batch scheduler가 수행)
3. ACK message
```

## 3. Admin Query Flow (전체 이력)

```
Input: WorkLogFilter { workLogType?, userId?, resourceKey?, startDate?, endDate?, page, size, sort }
Actor: Admin (X-User-Role = ADMIN or ROOT_ADMIN)

1. Verify requester role is ADMIN+
   - INSUFFICIENT → return 403
2. Build query from filter
   - markedForDeletion = false (삭제 대기 이력은 제외)
3. Execute paginated query
4. Return Page<WorkLog> (previousValue, newValue 포함 — 단순 텍스트)
```

## 4. Self Query Flow (본인 이력)

```
Input: WorkLogFilter { workLogType?, startDate?, endDate?, page, size }
Actor: Any authenticated user

1. Set userId filter = requester userId (강제)
2. Build query (markedForDeletion = false)
3. Execute paginated query
4. Return Page<WorkLog>
```

## 5. Search History Retention Scheduler

```
Trigger: Daily 04:00 (cron)

1. Query: find WorkLogs where workLogType == SEARCH AND performedAt < (now - 180 days)
2. Delete matched records (hard delete, batch 1000)
3. Log deleted count
```

## 6. User Deletion Batch Scheduler

```
Trigger: Daily 04:30 (cron)

1. Query: find WorkLogs where markedForDeletion == true
2. Delete matched records (hard delete, batch 1000)
3. Log deleted count
```

## 7. History Export Flow

```
Input: ExportFilter { workLogType?, userId?, startDate?, endDate? }
Actor: Admin

1. Verify requester role is ADMIN+
2. Build query from filter
3. Stream all matching WorkLogs
4. Convert to JSON format
5. Return as file download
```

---

## Testable Properties (PBT-01)

### Idempotence Properties
| Property | Description | Category |
|---|---|---|
| Duplicate event ignored | 동일 eventId로 두 번 처리해도 WorkLog 1개만 존재 | Idempotence |

### Invariant Properties
| Property | Description | Category |
|---|---|---|
| Event mapping completeness | 모든 지원 eventType은 반드시 하나의 WorkLogType으로 매핑됨 | Invariant |
| Self query isolation | 본인 조회 시 다른 userId의 이력은 절대 포함 안 됨 | Invariant |
| Search retention | SEARCH 타입 이력은 180일 후 항상 삭제 대상 | Invariant |
| Marked deletion excluded | markedForDeletion=true 이력은 조회 결과에 포함 안 됨 | Invariant |

# Logical Components - User Service

## Component Diagram

```
+------------------------------------------------------------------+
|                      User Service                                 |
|                                                                  |
|  +------------------+    +-----------------+    +-------------+  |
|  | REST Controllers |    | Event Publisher |    | Scheduler   |  |
|  | (Inbound Adapter)|    | (Outbound)     |    |             |  |
|  +--------+---------+    +-------+---------+    +------+------+  |
|           |                      |                     |         |
|           v                      v                     v         |
|  +------------------+    +-----------------+    +-------------+  |
|  | Application      |    | RabbitMQ        |    | AuditLog    |  |
|  | Services         |    | Template        |    | Purge Job   |  |
|  | (Use Cases)      |    |                 |    | (Daily 2AM) |  |
|  +--------+---------+    +-----------------+    +-------------+  |
|           |                                                      |
|           v                                                      |
|  +------------------+                                            |
|  | MongoDB          |                                            |
|  | Repository       |                                            |
|  | (Outbound)       |                                            |
|  +------------------+                                            |
+------------------------------------------------------------------+
          |                        |
          v                        v
+------------------+    +---------------------+
| MongoDB          |    | RabbitMQ            |
| (user-db)        |    | Exchange + Queues   |
+------------------+    +---------------------+
```

---

## RabbitMQ Configuration

### Exchange
| Name | Type | Durable | Purpose |
|---|---|---|---|
| mkfmm.user | topic | Yes | User Service 이벤트 발행 |

### Routing Keys
| Event | Routing Key |
|---|---|
| UserCreated | user.created |
| UserDeleted | user.deleted |
| PermissionChanged | user.permission.changed |
| AccountUnlocked | user.account.unlocked |

### Queues (Consumer 측 - History Service가 생성)
| Queue | Binding | Consumer |
|---|---|---|
| mkfmm.history.user-events | mkfmm.user / user.# | History Service |

### Dead Letter Configuration
| Property | Value |
|---|---|
| x-dead-letter-exchange | mkfmm.user.dlx |
| x-dead-letter-routing-key | dlq |
| DLQ Name | mkfmm.user.dlq |

---

## Scheduled Jobs

### AuditLog Purge Job
| Property | Value |
|---|---|
| Schedule | 매일 02:00 (cron: `0 0 2 * * *`) |
| Target | AuditLog where performedAt < (now - 730 days) |
| Batch Size | 1000건씩 반복 삭제 |
| Logging | 삭제 건수 INFO 로그 |
| Error Handling | 실패 시 ERROR 로그, 다음 실행 시 재시도 |

---

## MongoDB Collections

### users
| Index | Fields | Type | Purpose |
|---|---|---|---|
| Primary | _id | Unique | MongoDB default |
| Unique | userId | Unique | 로그인 ID 유일성 보장 |
| Query | name, department | Compound | 검색 성능 |
| Query | role | Single | 역할별 필터링 |

### audit_logs
| Index | Fields | Type | Purpose |
|---|---|---|---|
| Primary | _id | Unique | MongoDB default |
| TTL | performedAt | — | (TTL 미사용, 스케줄러가 관리) |
| Query | action, targetUserId | Compound | 필터링 |
| Query | performedBy | Single | 수행자 검색 |
| Query | performedAt | Single | 기간 검색 + 보존 정책 삭제 |

---

## Error Handling Strategy

### API Layer (Controller)
| Error Type | HTTP Status | Response |
|---|---|---|
| Validation failure | 400 | { code, message, field errors } |
| Unauthorized | 401 | { code, message } |
| Forbidden (권한 부족) | 403 | { code, message } |
| Not found | 404 | { code, message } |
| Duplicate userId | 409 | { code, message } |
| Internal error | 500 | { code, message } |

### Event Publishing
| Scenario | Action |
|---|---|
| 발행 성공 | 정상 완료 |
| 발행 실패 (1~3회) | Exponential backoff 재시도 |
| 최종 실패 (3회 초과) | DLQ로 이동, ERROR 로그 |
| DLQ 메시지 | Admin UI에서 확인 + 재처리/폐기 |

---

## DLQ Management (Admin UI 연동)

### API Endpoints (User Service 내)
| Endpoint | Method | Purpose |
|---|---|---|
| /admin/dlq/messages | GET | DLQ 메시지 목록 조회 |
| /admin/dlq/messages/{id}/retry | POST | DLQ 메시지 재처리 (원래 큐로 재발행) |
| /admin/dlq/messages/{id} | DELETE | DLQ 메시지 폐기 |

### DLQ Message Schema
| Field | Type | Description |
|---|---|---|
| messageId | String | 원본 메시지 ID |
| eventType | String | 이벤트 유형 |
| payload | JSON | 원본 이벤트 데이터 |
| failedAt | Instant | 최종 실패 시각 |
| failureReason | String | 실패 사유 |
| retryCount | Integer | 시도 횟수 |

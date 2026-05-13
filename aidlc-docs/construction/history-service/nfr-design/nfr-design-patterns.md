# NFR Design Patterns - History Service

## 1. Idempotent Consumer Pattern

**적용 대상**: 모든 이벤트 수신

**패턴**:
```
Event received → Check sourceEvent exists in DB
    → EXISTS → Discard (ACK, log DEBUG)
    → NOT EXISTS → Process → Save WorkLog → ACK
```

**구현**: MongoDB unique index on `sourceEvent` field. 중복 insert 시 DuplicateKeyException → 무시.

---

## 2. Manual Acknowledgment Pattern

**적용 대상**: RabbitMQ consumer

**패턴**:
```
Message received → Process successfully → manual ACK
                 → Process failed → NACK with requeue (재시도 횟수 내)
                 → Max retries exceeded → NACK without requeue → DLQ
```

**구현**: `spring.rabbitmq.listener.simple.acknowledge-mode=manual`

---

## 3. Scheduled Purge Pattern (Dual)

**적용 대상**: SEARCH 이력 보존 + User 삭제 batch

**패턴**:
```
Scheduler A (04:00): DELETE WHERE workLogType=SEARCH AND performedAt < (now-180d)
Scheduler B (04:30): DELETE WHERE markedForDeletion=true
```

**구현**: 각 스케줄러 batch 1000건씩 반복 삭제

---

## 4. Multi-Queue Binding Pattern

**적용 대상**: 여러 서비스 이벤트를 하나의 큐로 수신

**패턴**:
```
mkfmm.auth exchange ──(user.#)───┐
mkfmm.user exchange ──(user.#)───┤
mkfmm.resource exchange ─(resource.#)─┼──→ mkfmm.history.events queue → Consumer
mkfmm.dataio exchange ─(dataio.#)─┤
mkfmm.deploy exchange ─(deploy.#)─┘
```

**구현**: 하나의 큐에 여러 exchange를 바인딩 (topic 패턴)

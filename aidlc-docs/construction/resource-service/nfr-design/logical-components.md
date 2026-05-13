# Logical Components - Resource Service

## Component Diagram

```
+------------------------------------------------------------------+
|                    Resource Service                               |
|                                                                  |
|  +------------------+    +------------------+    +-------------+ |
|  | REST Controllers |    | Event Publisher  |    | Scheduler   | |
|  | (Inbound)        |    | (Outbound)      |    |             | |
|  +--------+---------+    +--------+---------+    +------+------+ |
|           |                       |                     |        |
|           v                       v                     v        |
|  +------------------+    +------------------+    +-------------+ |
|  | ResourceService  |    | RabbitMQ         |    | HardDelete  | |
|  | SimilarityService|    | Template (Async) |    | Job (03:00) | |
|  +--------+---------+    +------------------+    +-------------+ |
|           |                                                      |
|           v                                                      |
|  +------------------+                                            |
|  | Levenshtein      |                                            |
|  | Calculator       |                                            |
|  | (Domain Service) |                                            |
|  +--------+---------+                                            |
|           |                                                      |
|           v                                                      |
|  +------------------+                                            |
|  | MongoDB Repo     |                                            |
|  | (Outbound)       |                                            |
|  +------------------+                                            |
+------------------------------------------------------------------+
          |                        |
          v                        v
+------------------+    +---------------------+
| MongoDB          |    | RabbitMQ            |
| (resource-db)    |    | mkfmm.resource      |
+------------------+    +---------------------+
```

---

## RabbitMQ Configuration

### Exchange
| Name | Type | Durable |
|---|---|---|
| mkfmm.resource | topic | Yes |

### Routing Keys
| Event | Routing Key |
|---|---|
| ResourceCreated | resource.created |
| ResourceUpdated | resource.updated |
| ResourceDeleted | resource.deleted |
| ResourceSearched | resource.searched |
| SimilarityChoiceMade | resource.similarity.choice |

### DLQ
| Property | Value |
|---|---|
| DLX | mkfmm.resource.dlx |
| DLQ | mkfmm.resource.dlq |

---

## MongoDB Collections & Indexes

### resources
| Index | Fields | Type | Purpose |
|---|---|---|---|
| Primary | _id | Unique | Default |
| Unique (partial) | resourceKey | Unique where deleted=false | 중복 방지 |
| Compound | { deleted, resourceType, createdAt } | Compound | 검색 + 필터 |
| Text | { resourceKey, content } | Text | 키워드 검색 |
| Query | deletedAt | Single | Hard Delete 스케줄 |

---

## Scheduled Jobs

### Hard Delete Job
| Property | Value |
|---|---|
| Schedule | 매일 03:00 (`0 0 3 * * *`) |
| Target | resources where deleted=true AND deletedAt < (now - 30d) |
| Batch Size | 1000건씩 |
| Logging | 삭제 건수 INFO 로그 |

---

## Domain Services

### LevenshteinCalculator
| Method | Input | Output | Description |
|---|---|---|---|
| calculate(s1, s2) | String, String | int | 두 문자열 간 편집 거리 (lowercase) |
| findSimilar(input, candidates, threshold, maxResults) | String, List<Resource>, int, int | List<SimilarityResult> | 후보 중 threshold 이하인 것 반환 |

---

## Error Handling

| Error Type | HTTP Status | Notes |
|---|---|---|
| Duplicate resourceKey | 409 Conflict | DB unique index 위반 시 |
| Resource not found | 404 | deleted=true도 not found 처리 |
| Validation failure | 400 | resourceKey blank, invalid type |
| Forbidden | 403 | READ 사용자가 CRUD 시도 |
| Similarity detected | 200 + similarityResults | 등록 차단이 아닌 사용자 선택 요청 |

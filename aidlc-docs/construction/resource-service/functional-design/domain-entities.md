# Domain Entities - Resource Service

## Resource
| Field | Type | Description |
|---|---|---|
| id | String (ObjectId) | 고유 식별자 |
| resourceKey | String | 리소스 키/ID (unique, 메시지키 또는 기능ID 또는 메뉴ID) |
| resourceType | ResourceType (enum) | MESSAGE_KEY, FUNCTION_ID, MENU_ID |
| content | String | 리소스에 매핑되는 실제 값/설명 |
| description | String | 설명/비고 (nullable) |
| deleted | boolean | Soft Delete 플래그 |
| deletedAt | Instant | 삭제 시각 (deleted=true일 때만, Hard Delete 기준) |
| createdBy | String | 등록자 userId |
| createdAt | Instant | 등록 시각 |
| updatedBy | String | 최종 수정자 userId |
| updatedAt | Instant | 최종 수정 시각 |

---

## ResourceType (Enum)
```
MESSAGE_KEY   — 다국어 메시지 리소스 키
FUNCTION_ID   — 기능 단위 식별자
MENU_ID       — 화면 메뉴 식별자
```

---

## SimilarityResult (Value Object)
| Field | Type | Description |
|---|---|---|
| resource | Resource | 유사 후보 리소스 |
| score | int | Levenshtein 편집 거리 (낮을수록 유사) |
| matchType | MatchType (enum) | EXACT_CONTENT, SIMILAR_KEY, SIMILAR_CONTENT |

### MatchType
```
EXACT_CONTENT   — 내용이 정확히 일치
SIMILAR_KEY     — resourceKey가 편집 거리 3 이하
SIMILAR_CONTENT — content가 편집 거리 3 이하
```

---

## DuplicateCheckResult (Value Object)
| Field | Type | Description |
|---|---|---|
| isDuplicate | boolean | 중복 여부 |
| existingResource | Resource | 중복된 기존 리소스 (null if not duplicate) |
| duplicateField | String | 중복 발생 필드 (resourceKey / content) |

---

## Events (Published to RabbitMQ)

### ResourceCreated
| Field | Type |
|---|---|
| eventId | String (UUID) |
| eventType | "ResourceCreated" |
| timestamp | Instant |
| userId | String (작업자) |
| payload | { resourceId, resourceKey, resourceType, content, createdBy } |

### ResourceUpdated
| Field | Type |
|---|---|
| eventId | String (UUID) |
| eventType | "ResourceUpdated" |
| timestamp | Instant |
| userId | String (작업자) |
| payload | { resourceId, resourceKey, previousContent, newContent, updatedBy } |

### ResourceDeleted
| Field | Type |
|---|---|
| eventId | String (UUID) |
| eventType | "ResourceDeleted" |
| timestamp | Instant |
| userId | String (작업자) |
| payload | { resourceId, resourceKey, deletedBy } |

### ResourceSearched
| Field | Type |
|---|---|
| eventId | String (UUID) |
| eventType | "ResourceSearched" |
| timestamp | Instant |
| userId | String (검색자) |
| payload | { query, resultCount } |

### SimilarityChoiceMade
| Field | Type |
|---|---|
| eventId | String (UUID) |
| eventType | "SimilarityChoiceMade" |
| timestamp | Instant |
| userId | String (선택자) |
| payload | { inputKey, chosenAction (USE_EXISTING/CREATE_NEW), chosenResourceId, reason } |

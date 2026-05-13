# Business Logic Model - Resource Service

## 1. Create Resource Flow (핵심 워크플로우)

```
Input: CreateResourceRequest { resourceKey, resourceType, content, description }
Actor: Write User (X-User-Id, X-User-Role from Gateway)

1. Verify requester role is WRITE, ADMIN, or ROOT_ADMIN
   - READ → return 403
2. Validate input (resourceKey not blank, resourceType valid)
   - INVALID → return 400
3. Duplicate Check:
   - Query: find resource where resourceKey == input.resourceKey AND deleted == false
   - FOUND → return DuplicateCheckResult { isDuplicate=true, existingResource }
4. Similarity Detection:
   - Find candidates: regex match on resourceKey (partial) + deleted=false
   - Calculate Levenshtein distance for each candidate (key)
   - Find candidates: regex match on content (partial) + deleted=false
   - Calculate Levenshtein distance for each candidate (content)
   - Filter: distance ≤ 3
   - Sort by distance ascending, limit 5
   - IF similar found → return SimilarityResult list (user must choose)
5. Save Resource (if no duplicate, and user confirmed or no similar found):
   - Create Resource entity (deleted=false, createdAt=now, createdBy=requester)
   - Save to MongoDB
6. Publish ResourceCreated event
7. Return saved Resource
```

## 2. Confirm Similarity Choice Flow

```
Input: SimilarityChoiceRequest { inputResourceKey, chosenAction, chosenResourceId?, reason? }
Actor: Write User

1. IF chosenAction == USE_EXISTING:
   - Publish SimilarityChoiceMade event (action=USE_EXISTING, chosenResourceId)
   - Return existing resource
2. IF chosenAction == CREATE_NEW:
   - Create and save new Resource (same as step 5 above)
   - Publish SimilarityChoiceMade event (action=CREATE_NEW, reason)
   - Publish ResourceCreated event
   - Return saved Resource
```

## 3. Update Resource Flow

```
Input: UpdateResourceRequest { content?, description? }
Actor: Write User

1. Verify requester role is WRITE+
2. Find resource by id (deleted=false)
   - NOT FOUND or DELETED → return 404
3. IF content changed:
   - Duplicate check on new content against other resources
   - DUPLICATE → return 409
4. Store previousContent = resource.content
5. Apply updates (content, description)
6. Set updatedBy=requester, updatedAt=now
7. Save resource
8. Publish ResourceUpdated event (previousContent, newContent)
9. Return updated Resource
```

## 4. Delete Resource Flow (Soft Delete)

```
Input: { resourceId }
Actor: Write User

1. Verify requester role is WRITE+
2. Find resource by id (deleted=false)
   - NOT FOUND or already DELETED → return 404
3. Set deleted=true, deletedAt=now
4. Save resource
5. Publish ResourceDeleted event
6. Return 204 No Content
```

## 5. Search Resources Flow

```
Input: SearchQuery { keyword?, resourceType?, createdBy?, startDate?, endDate?, page, size, sort }
Actor: Any authenticated user (READ+)

1. Build query:
   - keyword → regex match on resourceKey OR content
   - resourceType → exact match
   - createdBy → exact match
   - date range → createdAt between
   - deleted=false (항상)
2. Execute paginated query
3. Publish ResourceSearched event (query summary, resultCount)
4. Return Page<Resource>
```

## 6. Hard Delete Scheduled Job

```
Trigger: Daily 03:00 (cron)

1. Query: find resources where deleted=true AND deletedAt < (now - 30 days)
2. Delete all matched resources (hard delete)
3. Log deleted count
```

## 7. Get Resource Detail Flow

```
Input: { resourceId }
Actor: Any authenticated user (READ+)

1. Find resource by id
   - NOT FOUND or deleted=true → return 404
2. Return Resource
```

---

## Levenshtein Distance Algorithm

```
function levenshtein(s1, s2):
    s1 = s1.toLowerCase()
    s2 = s2.toLowerCase()
    m = s1.length
    n = s2.length
    
    // Create matrix (m+1) x (n+1)
    dp[0..m][0..n]
    
    for i = 0 to m: dp[i][0] = i
    for j = 0 to n: dp[0][j] = j
    
    for i = 1 to m:
        for j = 1 to n:
            cost = (s1[i-1] == s2[j-1]) ? 0 : 1
            dp[i][j] = min(
                dp[i-1][j] + 1,      // deletion
                dp[i][j-1] + 1,      // insertion
                dp[i-1][j-1] + cost  // substitution
            )
    
    return dp[m][n]
```

---

## Testable Properties (PBT-01)

### Round-Trip Properties
| Property | Description | Category |
|---|---|---|
| Levenshtein symmetric | levenshtein(a, b) == levenshtein(b, a) | Invariant (commutativity) |
| Levenshtein identity | levenshtein(a, a) == 0 | Invariant |
| Levenshtein triangle | levenshtein(a, c) ≤ levenshtein(a, b) + levenshtein(b, c) | Invariant |

### Invariant Properties
| Property | Description | Category |
|---|---|---|
| Duplicate blocks create | 동일 resourceKey 존재 시 항상 등록 차단 | Invariant |
| Soft delete hides | deleted=true인 리소스는 검색에서 항상 제외 | Invariant |
| Similarity max 5 | 유사 후보는 항상 최대 5개 반환 | Invariant |
| Similarity threshold | 반환된 후보의 편집 거리는 항상 ≤ 3 | Invariant |

### Idempotence Properties
| Property | Description | Category |
|---|---|---|
| Double delete | 이미 deleted=true인 리소스 삭제 시도 → 404 (부작용 없음) | Idempotence |
| Search idempotent | 동일 쿼리 반복 시 동일 결과 (데이터 변경 없으면) | Idempotence |

### Oracle Properties
| Property | Description | Category |
|---|---|---|
| Levenshtein vs brute force | 최적화된 구현 vs 단순 재귀 구현 결과 동일 | Oracle |

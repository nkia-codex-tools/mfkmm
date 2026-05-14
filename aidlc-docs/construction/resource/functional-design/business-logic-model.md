# Business Logic Model - Unit 2: Resource Management

## 1. Resource Get All (공통)

```
Input: resourceType, SearchFilter (optional query)
    |
    v
[Apply Search Filter]
    - query가 있으면: 모든 텍스트 컬럼 LIKE %query% (case-insensitive)
    - query가 없으면: 전체 조회
    |
    v
[Order by rowOrder ASC]
    |
    v
[Compute Derived Fields] (resource type에 따라)
    - Functions: resourceKey 계산
    - Menus: functionDescription 조회
    - MessageResource: rowNumber, duplicateStatus, fullResourceKey, counts 계산
    |
    v
Return List<ResourceResponse>
```

## 2. Resource Create (공통)

```
Input: resourceType, CreateRequest, userId
    |
    v
[Validate Input]
    - 필수 필드 확인
    - 드롭다운 값 유효성 (action, type 등)
    |
    +--(invalid)--> Return 400
    |
    v
[Set Row Order]
    - newOrder = MAX(rowOrder) + 1
    |
    v
[Save to DB]
    - createdBy = userId
    - updatedBy = userId
    |
    v
[Record Change History] (Unit 4 통합 후)
    |
    v
Return 201 + ResourceResponse (with computed fields)
```

## 3. Resource Update (공통)

```
Input: resourceType, id, UpdateRequest, userId
    |
    v
[Find Existing]
    |
    +--(not found)--> Return 404
    |
    v
[Validate Input]
    |
    v
[Compute Field-Level Diff]
    - 각 필드별 이전 값 vs 새 값 비교
    - 변경된 필드만 추출
    |
    v
[Update Entity]
    - updatedBy = userId
    |
    v
[Record Change History] (Unit 4 통합 후)
    - 변경된 각 필드에 대해 이력 레코드 생성
    |
    v
Return 200 + ResourceResponse (with computed fields)
```

## 4. Resource Delete (공통)

```
Input: resourceType, id, userId
    |
    v
[Find Existing]
    |
    +--(not found)--> Return 404
    |
    v
[Delete from DB]
    |
    v
[Reorder Remaining Rows]
    - 삭제된 행 이후의 모든 행 rowOrder 재할당
    |
    v
[Record Change History] (Unit 4 통합 후)
    |
    v
Return 204 No Content
```

## 5. Batch Delete

```
Input: resourceType, List<Long> ids, userId
    |
    v
[Validate All IDs Exist]
    |
    v
[Delete All]
    |
    v
[Reorder All Remaining Rows]
    - 1부터 순차적으로 rowOrder 재할당
    |
    v
[Record Batch Change History] (Unit 4 통합 후)
    |
    v
Return 204 No Content
```

## 6. Reorder (Drag & Drop)

```
Input: resourceType, ReorderRequest (orderedIds), userId
    |
    v
[Validate]
    - orderedIds에 포함된 모든 ID가 존재하는지 확인
    - orderedIds 크기 == 전체 행 수 (전체 리오더) 또는 부분 리오더
    |
    v
[Update Row Orders]
    - orderedIds[0] → rowOrder = 1
    - orderedIds[1] → rowOrder = 2
    - ...
    |
    v
[Record Change History] (Unit 4 통합 후)
    |
    v
Return 200 OK
```

## 7. Menu: Function ID Autocomplete

```
Input: query (partial functionId string)
    |
    v
[Search FunctionResource]
    - WHERE functionId LIKE %query% (case-insensitive)
    - LIMIT 20
    |
    v
Return List<String> (matching functionIds)
```

## 8. Menu: Resolve Function Description

```
Input: functionId
    |
    v
[Find FunctionResource by functionId]
    |
    +--(not found)--> Return empty string
    |
    v
Return functionName
```

## 9. MessageResource: Compute Aggregate Fields

```
[On Query - After loading all rows]
    |
    v
[For each row]
    1. rowNumber: module과 resourceKey 모두 non-empty → assign sequential number
    2. duplicateStatus:
       - rowNumber == null → ""
       - COUNT(korean) in all rows > 1 → "중복"
       - resourceKey.trim().equals(resourceKey.trim().toLowerCase()) → "정상"
       - else → "대문자"
    3. fullResourceKey: rowNumber == null → "" | module + "." + resourceKey
    4. resourceKeyCount: COUNT of same resourceKey in all rows
    5. koreanCount: COUNT of same korean in all rows
    6. englishCount: COUNT of same english in all rows
    7. japaneseCount: COUNT of same japanese in all rows
    |
    v
Return rows with all computed fields attached
```

## 10. FunctionResource: Compute Resource Key

```
Input: functionId
    |
    v
[Check Empty]
    - functionId == null or blank → return ""
    |
    v
[Transform]
    - lowercase = functionId.toLowerCase()
    - replaced = lowercase.replace(".", "_")
    - result = "cmm.fn_" + replaced
    |
    v
Return result
```

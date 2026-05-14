# Logical Components - Unit 2: Resource Management

## Backend Components

### 1. FunctionController / MenuController / MessageResourceController

**Type**: REST Controllers
**Responsibility**: HTTP 요청 처리, 입력 검증, 서비스 위임

**공통 엔드포인트 패턴**:
```
GET    /api/resources/{type}           → getAll(query?)
POST   /api/resources/{type}           → create(request)
PUT    /api/resources/{type}/{id}      → update(id, request)
DELETE /api/resources/{type}/{id}      → delete(id)
DELETE /api/resources/{type}/batch     → deleteBatch(ids)
PATCH  /api/resources/{type}/reorder   → reorder(orderedIds)
```

**추가 (Menu only)**:
```
GET /api/resources/menus/function-ids?query=... → searchFunctionIds(query)
```

---

### 2. FunctionService / MenuService / MessageResourceService

**Type**: Service Layer
**Responsibility**: 비즈니스 로직, 자동 계산 필드, 순서 관리

**공통 메서드**:
- `getAll(query?)`: 전체 조회 + 자동 계산 필드 부착
- `create(request, userId)`: 검증 → rowOrder 할당 → 저장
- `update(id, request, userId)`: 검증 → diff 계산 → 업데이트
- `delete(id, userId)`: 삭제 → rowOrder 재정렬
- `deleteBatch(ids, userId)`: 일괄 삭제 → rowOrder 재정렬
- `reorder(orderedIds, userId)`: 순서 배치 업데이트

**FunctionService 추가**:
- `generateResourceKey(functionId)`: 자동생성 키 계산

**MenuService 추가**:
- `searchFunctionIds(query)`: functionId 자동완성 검색
- `resolveFunctionDescription(functionId)`: 기능명 조회

**MessageResourceService 추가**:
- `computeAggregateFields(List<MessageResource>)`: 전체 행 대상 집계 계산

---

### 3. FunctionRepository / MenuRepository / MessageResourceRepository

**Type**: Spring Data JPA Repository
**Responsibility**: 데이터 접근

**공통 메서드**:
- `findAllByOrderByRowOrderAsc()`: 전체 조회 (순서대로)
- `findMaxRowOrder()`: 현재 최대 rowOrder
- `updateRowOrder(Long id, int newOrder)`: @Query로 직접 UPDATE

**FunctionRepository 추가**:
- `findByFunctionIdContainingIgnoreCase(String query)`: 자동완성 검색
- `findByFunctionId(String functionId)`: 기능 조회

**MessageResourceRepository 추가**:
- 집계는 서비스 레이어에서 Java로 처리 (SQLite에서 복잡 집계 비효율)

---

## Frontend Components

### 4. ResourcePage

**Responsibility**: 리소스 관리 메인 페이지 조합

```
ResourcePage
├── ResourceTabs         (탭 전환)
├── GridToolbar          (도구 모음)
└── ResourceGridContainer (활성 그리드 렌더링)
    ├── FunctionGrid
    ├── MenuGrid
    └── MessageResourceGrid
```

---

### 5. AG Grid Wrapper (per resource type)

**Responsibility**: AG Grid 인스턴스 관리, 이벤트 처리

**Internal State**:
- `gridApi`: AG Grid API 인스턴스
- `rowData`: 서버에서 로드한 전체 데이터
- `columnDefs`: 리소스 타입별 컬럼 정의

**Event Handlers**:
- `onCellValueChanged`: 인라인 편집 완료 → PUT API 호출
- `onRowDragEnd`: D&D 완료 → PATCH /reorder API 호출
- `onSelectionChanged`: 선택 상태 → useResourceStore 동기화

---

### 6. FunctionIdEditor (Custom Cell Editor)

**Type**: AG Grid ICellEditorComp
**Responsibility**: functionId 자동완성 에디터

**Internal State**:
- `inputValue`: 현재 입력 텍스트
- `suggestions`: 자동완성 결과 목록
- `isOpen`: 드롭다운 표시 여부

**Behavior**:
1. 입력 시작 → 현재 값으로 초기화
2. 타이핑 → debounce 300ms → GET /function-ids?query=...
3. 결과 → 드롭다운 표시
4. 선택 또는 Enter → getValue() 반환 → AG Grid에 값 전달

---

### 7. GridToolbar

**Type**: React Component
**Responsibility**: 리소스 그리드 상단 도구 모음

**Buttons**:
- 행 추가 (WRITER+)
- 선택 삭제 (WRITER+, 선택된 행 있을 때만 활성)
- Import (WRITER+)
- Export (ALL)
- 검색 입력 (ALL)

---

### 8. useResourceStore (Zustand)

**State**:
```typescript
{
  activeTab: 'functions' | 'menus' | 'messageResources',
  selectedRowIds: number[],
  searchQuery: string,
}
```

**Actions**:
```typescript
setActiveTab(tab)
setSelectedRows(ids)
clearSelection()
setSearchQuery(query)
```

---

## Data Flow

### Full Load Flow
```
Page Mount
    → useEffect → GET /api/resources/{activeTab}
    → Server: DB query + compute derived fields
    → Response: List<ResourceResponse> (with computed fields)
    → AG Grid setRowData(response.data)
    → Virtual scroll renders visible rows
```

### Inline Edit Flow
```
Double-click cell → AG Grid edit mode
    → Edit value → Enter
    → onCellValueChanged
    → PUT /api/resources/{type}/{id} { ...updatedFields }
    → Server: validate → update → compute derived → respond
    → Success: update entire row from response (computed fields refresh)
    → Failure: revert cell + toast error
```

### Reorder Flow
```
Drag row(s) to new position
    → onRowDragEnd
    → Collect all row IDs in new order from grid
    → PATCH /api/resources/{type}/reorder { orderedIds: [...] }
    → Server: batch update rowOrder
    → Success: (grid already shows new order from drag)
    → Failure: reload full data to restore correct order
```

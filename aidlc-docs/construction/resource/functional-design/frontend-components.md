# Frontend Components - Unit 2: Resource Management

## 1. ResourcePage

**Route**: `/resources`

**Structure**:
```
ResourcePage
├── ResourceTabs (기능 / 메뉴 / 리소스키)
├── GridToolbar (추가, 삭제, import, export, 검색)
└── [ActiveGrid] (FunctionGrid | MenuGrid | MessageResourceGrid)
```

**State (useResourceStore)**:
- activeTab: 'functions' | 'menus' | 'messageResources'
- selectedRows: number[]
- searchQuery: string

---

## 2. AG Grid Configuration (공통)

**공통 설정**:
- `rowSelection: 'multiple'` - 다중 선택
- `rowDragManaged: true` - 드래그앤드롭 순서 변경
- `animateRows: true` - 행 이동 애니메이션
- `suppressRowClickSelection: true` - 체크박스로만 선택
- `rowDragMultiRow: true` - 다중 행 드래그
- `getRowId: (params) => params.data.id` - 행 ID
- `defaultColDef.editable: true` - 인라인 편집 기본 활성

**인라인 편집**:
- 셀 클릭 시 편집 모드 진입
- Enter/Tab으로 확인, Escape로 취소
- `onCellValueChanged` → API 호출로 서버 업데이트

---

## 3. FunctionGrid

**Column Definitions**:

| Field | Header | Editor | Editable |
|-------|--------|--------|----------|
| aClass | A Class | text | Yes |
| bClass | B Class | text | Yes |
| cClass | C Class | text | Yes |
| action | 기능(ACTION) | agSelectCellEditor (10 options) | Yes |
| functionName | 기능명 | text | Yes |
| functionId | 기능(FUNCTION) ID | text | Yes |
| type | 유형 | agSelectCellEditor (6 options) | Yes |
| light | Light | agSelectCellEditor (TRUE/FALSE) | Yes |
| standard | Standard | agSelectCellEditor (TRUE/FALSE) | Yes |
| enterprise | Enterprise | agSelectCellEditor (TRUE/FALSE) | Yes |
| systemMenu | System Menu | agSelectCellEditor (TRUE/FALSE) | Yes |
| productDomain | Product Domain | text | Yes |
| domainLicenseResourceType | 도메인 라이선스 리소스 타입 | text | Yes |
| relatedServices | 관련 컨테이너 서비스명 | text | Yes |
| resourceKey | 기능 ID 리소스키(자동생성) | - | **No** (read-only, computed) |

---

## 4. MenuGrid

**Column Definitions**:

| Field | Header | Editor | Editable |
|-------|--------|--------|----------|
| mainMenu | 메인 메뉴 | text | Yes |
| subMenuGroup | 하위 메뉴 그룹 | text | Yes |
| subMenu | 하위 메뉴 | text | Yes |
| menuLevel1 | 메뉴 1레벨 | text | Yes |
| menuLevel2 | 메뉴 2레벨 | text | Yes |
| menuLevel3 | 메뉴 3레벨 | text | Yes |
| menuId | 메뉴 ID | text | Yes |
| isMenu | 메뉴여부 | agSelectCellEditor (TRUE/FALSE) | Yes |
| isSystemMenu | System 메뉴 | agSelectCellEditor (TRUE/FALSE) | Yes |
| functionId | 기능(FUNCTION) ID | custom autocomplete editor | Yes |
| functionDescription | 기능 설명 | - | **No** (read-only, auto-filled) |
| menuIcon | 대메뉴 Icon | text | Yes |

**Custom Autocomplete Editor** (functionId):
- 타이핑 시 GET /api/resources/menus/function-ids?query=... 호출
- 결과를 드롭다운 목록으로 표시
- 선택하면 functionId 설정 + functionDescription 자동 갱신

---

## 5. MessageResourceGrid

**Column Definitions**:

| Field | Header | Editor | Editable |
|-------|--------|--------|----------|
| rowNumber | 번호 | - | **No** (computed) |
| duplicateStatus | 중복/대문자 | - | **No** (computed) |
| module | 모듈 | text | Yes |
| resourceKey | resource_key | text | Yes |
| fullResourceKey | full_resource_key | - | **No** (computed) |
| korean | 국문 | text | Yes |
| english | 영문 | text | Yes |
| japanese | 일문 | text | Yes |
| description | 설명/사용처 | text | Yes |
| registeredDate | 등록/수정일자 | text | Yes |
| registeredBy | 등록자 | text | Yes |
| resourceKeyCount | 리소스키 개수 | - | **No** (computed) |
| koreanCount | 국문 개수 | - | **No** (computed) |
| englishCount | 영문 개수 | - | **No** (computed) |
| japaneseCount | 일문 개수 | - | **No** (computed) |

---

## 6. GridToolbar

**Actions**:
- **행 추가**: 빈 행을 맨 아래에 추가 → 인라인 편집 시작
- **선택 삭제**: 선택된 행들 일괄 삭제 (확인 팝업)
- **검색**: 텍스트 입력 → 실시간 필터링 (AG Grid quickFilter 또는 서버 검색)
- **Import**: ImportDialog 열기
- **Export**: ExportDialog 열기

**권한별 표시**:
- READER: 검색, Export만 표시
- WRITER/ADMIN: 모든 버튼 표시

---

## 7. Data Flow

### 인라인 편집 Flow
```
User clicks cell → AG Grid enters edit mode
    → User types/selects value → Press Enter
    → onCellValueChanged fires
    → PUT /api/resources/{type}/{id} with updated fields
    → Server response with computed fields
    → Update AG Grid row data with response
    → If error: revert cell value, show toast
```

### Drag & Drop Reorder Flow
```
User drags row(s) to new position
    → onRowDragEnd fires
    → Collect new order (all IDs in new sequence)
    → PATCH /api/resources/{type}/reorder
    → Server updates all rowOrder values
    → Refresh grid data
```

### Function ID Autocomplete Flow (Menu Tab)
```
User starts typing in functionId cell
    → Debounce 300ms
    → GET /api/resources/menus/function-ids?query=...
    → Show dropdown with matching IDs
    → User selects ID
    → PUT /api/resources/menus/{id} with new functionId
    → Server responds with functionDescription filled
    → Update row in grid
```

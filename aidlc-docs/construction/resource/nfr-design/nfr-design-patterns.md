# NFR Design Patterns - Unit 2: Resource Management

## 1. Performance Patterns

### 1.1 Full Data Load + Client-Side Filtering

**패턴**: 전체 데이터를 한 번에 로드하고, 필터링/검색은 클라이언트에서 처리

```
초기 로딩:
  GET /api/resources/functions → 전체 데이터 (10,000행 이하)
  → AG Grid에 rowData 설정
  → AG Grid 가상 스크롤로 DOM 최소화

검색:
  사용자 타이핑 → AG Grid quickFilterText 설정
  → 클라이언트에서 즉시 필터 (서버 호출 없음)
```

**장점**: 즉각적인 검색 응답, 서버 부하 최소
**적용 조건**: 10,000행 이하 (이 프로젝트의 범위)

### 1.2 Batch Row Order Update

**패턴**: 순서 변경 시 전체 rowOrder를 단일 트랜잭션으로 배치 업데이트

```java
@Transactional
void reorder(List<Long> orderedIds) {
    for (int i = 0; i < orderedIds.size(); i++) {
        repository.updateRowOrder(orderedIds.get(i), i + 1);
    }
}
```

**SQLite 최적화**: WAL 모드에서 단일 트랜잭션 내 다수 UPDATE는 효율적

### 1.3 Computed Fields on Read

**패턴**: 자동 계산 필드는 DB에 저장하지 않고, 조회 시 서버에서 실시간 계산

```
DB 조회 → Entity List
    → Service에서 computed fields 계산
    → DTO에 포함하여 응답

장점: 데이터 일관성 보장, 동기화 문제 없음
단점: 매 조회 시 계산 비용 (10,000행 이하로 수용 가능)
```

### 1.4 Debounced Autocomplete

**패턴**: 자동완성 입력 시 300ms 디바운스 후 서버 호출

```
User types → 300ms wait → GET /api/.../function-ids?query=... → dropdown results
           ↑ reset timer on each keystroke
```

---

## 2. Data Integrity Patterns

### 2.1 Row Order Gap Prevention

**패턴**: 삽입/삭제/이동 시 rowOrder에 빈 틈이 생기지 않도록 보장

```
삽입: newOrder = MAX(rowOrder) + 1
삭제: 삭제 후 남은 행 전체 rowOrder 재할당 (1, 2, 3, ...)
이동: 전체 순서 재할당
```

**불변식**: 언제든 rowOrder는 1~N까지 빈 틈 없이 연속

### 2.2 Optimistic UI with Server Confirmation

**패턴**: 인라인 편집 시 서버 확인 후 UI 갱신 (낙관적 업데이트 안 함)

```
Cell edit complete
    → PUT /api/resources/{type}/{id}
    → Wait for server response
    → Success: update row with response data (includes computed fields)
    → Failure: revert cell value + show error toast
```

**이유**: 자동 계산 필드가 서버 응답에 포함되므로, 서버 확인이 필수

### 2.3 Soft Reference (Menu → Function)

**패턴**: 메뉴의 functionId는 기능 테이블의 FK가 아닌 soft reference

```
메뉴 저장 시:
    → functionId 유효성 검사 (FunctionResource에 존재하는지)
    → 존재: 정상 저장 + functionDescription 채움
    → 미존재: 경고 메시지와 함께 저장 허용 (functionDescription = "")
```

**이유**: 데이터 입력 유연성 보장 (순서에 관계없이 입력 가능)

---

## 3. Frontend Architecture Patterns

### 3.1 AG Grid Cell Editor Strategy

**패턴**: 컬럼 타입에 따라 적절한 에디터 할당

| 타입 | AG Grid Editor | 설정 |
|------|---------------|------|
| 텍스트 | 기본 text editor | `editable: true` |
| 드롭다운 (고정 옵션) | `agSelectCellEditor` | `cellEditorParams: { values: [...] }` |
| Boolean | `agSelectCellEditor` | `cellEditorParams: { values: ['TRUE', 'FALSE'] }` |
| 자동완성 | Custom React component | 커스텀 ICellEditorComp |
| 읽기 전용 | - | `editable: false`, 배경색 회색 |

### 3.2 Custom Autocomplete Cell Editor

**패턴**: AG Grid ICellEditorComp 인터페이스 구현

```typescript
// FunctionIdEditor.tsx
- 텍스트 입력 필드 + 드롭다운 팝업
- 입력 시 debounce 300ms → API 호출
- 결과를 드롭다운으로 표시
- 선택 시 getValue() 반환
- AG Grid가 onCellValueChanged 트리거
```

### 3.3 Row Drag Configuration

**패턴**: AG Grid의 managed row drag 사용

```typescript
columnDefs = [
  { rowDrag: true, width: 40, suppressMenu: true },  // drag handle column
  ...dataColumns
];

gridOptions = {
  rowDragManaged: true,
  rowDragMultiRow: true,
  animateRows: true,
  onRowDragEnd: (event) => handleReorder(event)
};
```

### 3.4 Modified Cell Highlight

**패턴**: 수정된 셀에 CSS 클래스 적용

```typescript
// 셀 값 변경 후 서버 저장 성공 시
onCellValueChanged → API 성공 → 해당 셀에 'cell-modified' 클래스 추가
                                 → 3초 후 클래스 제거 (fade out)

CSS:
.cell-modified {
  background-color: #e8f5e9;  // 연한 초록
  transition: background-color 0.3s;
}
```

---

## 4. Error Handling Patterns

### 4.1 Cell Edit Error Recovery

```
API 호출 실패 시:
1. AG Grid api.undoCellEditing() 또는 이전 값으로 복원
2. Toast notification: "저장에 실패했습니다. 다시 시도해주세요."
3. 로그 기록 (console.error)
```

### 4.2 Bulk Operation Error Handling

```
일괄 삭제 실패 시:
1. 전체 실패: 에러 메시지 표시, 데이터 변경 없음
2. 부분 실패 (가능성 낮음 - 트랜잭션): 전체 롤백, 에러 메시지

재시도: 사용자가 직접 다시 시도 (자동 재시도 없음)
```

---

## 5. Security Patterns (Unit 2 specific)

### 5.1 Permission-Based UI Rendering

```typescript
// GridToolbar에서 권한에 따라 버튼 표시/숨김
const { hasWritePermission } = useAuthStore();

{hasWritePermission() && <button>행 추가</button>}
{hasWritePermission() && <button>선택 삭제</button>}
// 검색, Export는 모든 인증 사용자에게 표시
```

### 5.2 Read-Only Grid for READER

```typescript
// READER 사용자는 모든 셀 편집 불가
defaultColDef = {
  editable: hasWritePermission(),
  ...
};
// rowDrag도 비활성화
rowDragManaged: hasWritePermission(),
```

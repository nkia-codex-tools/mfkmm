# Business Rules - Unit 2: Resource Management

## BR-RES-01: CRUD 공통 규칙

| Rule | Description |
|------|-------------|
| BR-RES-01.1 | WRITER 또는 ADMIN만 생성/수정/삭제 가능 |
| BR-RES-01.2 | READER는 조회만 가능 |
| BR-RES-01.3 | 모든 변경 시 updatedBy, updatedAt 자동 갱신 |
| BR-RES-01.4 | 생성 시 rowOrder는 현재 최대값 + 1 (맨 아래 추가) |
| BR-RES-01.5 | 삭제 시 해당 행의 rowOrder 이후 행들 순서 재정렬 |

## BR-RES-02: 기능(Functions) 탭 규칙

| Rule | Description |
|------|-------------|
| BR-RES-02.1 | `action` 필드는 정해진 10개 값 중 선택 |
| BR-RES-02.2 | `type` 필드는 정해진 6개 값 중 선택 |
| BR-RES-02.3 | `light`, `standard`, `enterprise`, `systemMenu`는 boolean |
| BR-RES-02.4 | `resourceKey` (자동생성): functionId가 비어있으면 빈 문자열 |
| BR-RES-02.5 | `resourceKey` (자동생성): "cmm.fn_" + functionId.toLowerCase().replace(".", "_") |
| BR-RES-02.6 | `functionId`는 고유 권장 (경고만, 중복 허용) |

## BR-RES-03: 메뉴(Menus) 탭 규칙

| Rule | Description |
|------|-------------|
| BR-RES-03.1 | `isMenu`, `isSystemMenu`는 boolean |
| BR-RES-03.2 | `functionId` 입력 시 자동완성: FunctionResource에서 LIKE 검색 |
| BR-RES-03.3 | `functionId` 선택/입력 시 해당 기능의 `functionName`을 `functionDescription`으로 자동 표시 |
| BR-RES-03.4 | FunctionResource에 존재하지 않는 functionId도 입력 가능 (경고 표시, 차단 안 함) |
| BR-RES-03.5 | `functionDescription`은 읽기 전용 (사용자 직접 수정 불가) |

## BR-RES-04: 리소스 키(Message Resource) 탭 규칙

| Rule | Description |
|------|-------------|
| BR-RES-04.1 | `rowNumber`: module과 resourceKey가 모두 비어있지 않은 행에만 순번 부여 |
| BR-RES-04.2 | `duplicateStatus` 계산 순서: ① 국문 값이 전체에서 2회 이상 → "중복" ② resourceKey == resourceKey.trim().toLowerCase() → "정상" ③ 그 외 → "대문자" |
| BR-RES-04.3 | `fullResourceKey`: module + "." + resourceKey |
| BR-RES-04.4 | 개수 필드들: 전체 데이터에서 해당 값과 동일한 값의 개수 (COUNTIF 동치) |
| BR-RES-04.5 | 모든 자동 계산 필드는 읽기 전용, DB에 저장하지 않음 |
| BR-RES-04.6 | 자동 계산 필드는 서버 응답 시 실시간 계산하여 포함 |

## BR-RES-05: 행 순서 변경 (Drag & Drop)

| Rule | Description |
|------|-------------|
| BR-RES-05.1 | 드래그앤드롭으로 단일 행 이동 |
| BR-RES-05.2 | 다중 선택 후 일괄 이동 (선택된 행들을 목표 위치로 이동) |
| BR-RES-05.3 | 이동 후 전체 rowOrder 재할당 (1부터 순차) |
| BR-RES-05.4 | 순서 변경도 변경 이력으로 기록 (Unit 4 통합 후) |

## BR-RES-06: 다중 선택 및 일괄 작업

| Rule | Description |
|------|-------------|
| BR-RES-06.1 | Shift 클릭: 범위 선택 (현재 선택 ~ 클릭 행) |
| BR-RES-06.2 | Ctrl/Cmd 클릭: 개별 추가/해제 |
| BR-RES-06.3 | 드래그: 연속 행 범위 선택 |
| BR-RES-06.4 | 일괄 삭제: 선택된 모든 행 삭제, 확인 팝업 표시 |
| BR-RES-06.5 | 일괄 이동: 선택된 행들의 순서 변경 |

## BR-RES-07: 검색 및 필터링

| Rule | Description |
|------|-------------|
| BR-RES-07.1 | 검색어 입력 시 모든 텍스트 컬럼 대상 LIKE 검색 |
| BR-RES-07.2 | 대소문자 구분 없음 |
| BR-RES-07.3 | 검색 결과에서도 순서(rowOrder) 유지 |
| BR-RES-07.4 | 검색 중에도 편집/삭제 가능 |

## Testable Properties (PBT-01)

| Property | Category | Description |
|----------|----------|-------------|
| Reorder preserves elements | Invariant | reorder 후 전체 행 수, ID 집합 동일 |
| Reorder idempotent | Idempotence | reorder(reorder(ids)) == reorder(ids) |
| ResourceKey computation | Oracle | generateResourceKey(fId) == "cmm.fn_" + fId.toLowerCase().replace(".", "_") |
| FullResourceKey computation | Oracle | computeFullKey(mod, key) == mod + "." + key |
| DuplicateStatus consistent | Invariant | 동일 데이터에 대해 항상 동일 결과 |
| Batch delete size | Invariant | deleteBatch(n개) 후 총 행 수 = 이전 - n |
| Row order contiguous | Invariant | 모든 작업 후 rowOrder는 1부터 N까지 빈 틈 없이 연속 |

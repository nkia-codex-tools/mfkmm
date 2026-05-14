# Domain Entities - Unit 2: Resource Management

## Entity: FunctionResource

| Field | Type | Constraints | Description |
|-------|------|------------|-------------|
| id | Long | PK, auto-increment | 고유 식별자 |
| aClass | String | max 255 | A 분류 |
| bClass | String | max 255 | B 분류 |
| cClass | String | max 255 | C 분류 |
| action | String | max 100, enum-like | 기능 ACTION (드롭다운) |
| functionName | String | max 255 | 기능명 |
| functionId | String | NOT NULL, max 255 | 기능(FUNCTION) ID |
| type | String | max 50, enum-like | 유형 (드롭다운) |
| light | Boolean | NOT NULL, default FALSE | Light 라이선스 |
| standard | Boolean | NOT NULL, default FALSE | Standard 라이선스 |
| enterprise | Boolean | NOT NULL, default FALSE | Enterprise 라이선스 |
| systemMenu | Boolean | NOT NULL, default FALSE | System Menu 여부 |
| productDomain | String | max 255 | Product Domain |
| domainLicenseResourceType | String | max 255 | 도메인 라이선스 리소스 타입 |
| relatedServices | String | max 500 | 관련 컨테이너 서비스명 |
| rowOrder | Integer | NOT NULL | 행 순서 |
| createdBy | Long | NOT NULL | 생성자 ID |
| updatedBy | Long | NOT NULL | 수정자 ID |
| createdAt | Timestamp | NOT NULL | 생성 시각 |
| updatedAt | Timestamp | NOT NULL | 수정 시각 |

**Computed Field (read-only, not stored):**
- `resourceKey`: `functionId`가 비어있으면 빈 값, 아니면 `"cmm.fn_" + functionId.toLowerCase().replace(".", "_")`

**ACTION 드롭다운 옵션:**
`cmm.view`, `cmm.insert`, `cmm.delete`, `cmm.update`, `cmm.upload`, `cmm.download_excel`, `download`, `cmm.excution`, `cmm.copy`, `cmm.load_1`

**유형 드롭다운 옵션:**
`읽기`, `쓰기`, `실행`, `Excel`, `Import`, `Export`

---

## Entity: MenuResource

| Field | Type | Constraints | Description |
|-------|------|------------|-------------|
| id | Long | PK, auto-increment | 고유 식별자 |
| mainMenu | String | max 255 | 메인 메뉴 |
| subMenuGroup | String | max 255 | 하위 메뉴 그룹 |
| subMenu | String | max 255 | 하위 메뉴 |
| menuLevel1 | String | max 255 | 메뉴 1레벨 |
| menuLevel2 | String | max 255 | 메뉴 2레벨 |
| menuLevel3 | String | max 255 | 메뉴 3레벨 |
| menuId | String | max 255 | 메뉴 ID |
| isMenu | Boolean | NOT NULL, default TRUE | 메뉴여부 |
| isSystemMenu | Boolean | NOT NULL, default FALSE | System 메뉴 |
| functionId | String | max 255 | 기능(FUNCTION) ID (기능 탭 참조) |
| menuIcon | String | max 100 | 대메뉴 Icon |
| rowOrder | Integer | NOT NULL | 행 순서 |
| createdBy | Long | NOT NULL | 생성자 ID |
| updatedBy | Long | NOT NULL | 수정자 ID |
| createdAt | Timestamp | NOT NULL | 생성 시각 |
| updatedAt | Timestamp | NOT NULL | 수정 시각 |

**Computed Field (read-only, not stored):**
- `functionDescription`: `functionId`가 존재하면 FunctionResource 테이블에서 해당 `functionId`의 `functionName` 조회

---

## Entity: MessageResource

| Field | Type | Constraints | Description |
|-------|------|------------|-------------|
| id | Long | PK, auto-increment | 고유 식별자 |
| module | String | max 100 | 모듈 |
| resourceKey | String | max 255 | resource_key |
| korean | String | max 1000 | 국문 |
| english | String | max 1000 | 영문 |
| japanese | String | max 1000 | 일문 |
| description | String | max 2000 | 설명/사용처 |
| registeredDate | String | max 20 | 등록/수정일자 |
| registeredBy | String | max 100 | 등록자 |
| rowOrder | Integer | NOT NULL | 행 순서 |
| createdBy | Long | NOT NULL | 생성자 ID |
| updatedBy | Long | NOT NULL | 수정자 ID |
| createdAt | Timestamp | NOT NULL | 생성 시각 |
| updatedAt | Timestamp | NOT NULL | 수정 시각 |

**Computed Fields (read-only, not stored, calculated on query):**
- `rowNumber`: `module`과 `resourceKey` 모두 비어있지 않으면 순번, 그렇지 않으면 null
- `duplicateStatus`: rowNumber가 null이면 빈 값. 전체 데이터에서 `korean` 값이 2회 이상 → "중복". `resourceKey`가 소문자와 동일하면 "정상", 아니면 "대문자"
- `fullResourceKey`: rowNumber가 null이면 빈 값. `module` + "." + `resourceKey`
- `resourceKeyCount`: rowNumber가 null이면 빈 값. 전체에서 동일 `resourceKey` 개수
- `koreanCount`: rowNumber가 null이면 빈 값. 전체에서 동일 `korean` 개수
- `englishCount`: rowNumber가 null이면 빈 값. 전체에서 동일 `english` 개수
- `japaneseCount`: rowNumber가 null이면 빈 값. 전체에서 동일 `japanese` 개수

---

## Common DTO: SearchFilter

| Field | Type | Description |
|-------|------|-------------|
| query | String | 검색어 (전체 컬럼 대상 LIKE 검색) |
| page | Integer | 페이지 번호 (가상 스크롤이므로 사용 안 할 수 있음) |
| size | Integer | 페이지 크기 |

## Common DTO: ReorderRequest

| Field | Type | Description |
|-------|------|-------------|
| orderedIds | List<Long> | 새 순서대로 정렬된 ID 목록 |

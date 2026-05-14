# Tech Stack Decisions - Unit 2: Resource Management

## Inherits from Unit 1
- Backend: Spring Boot 3.3, Java 21, Gradle, Spring Data JPA, SQLite
- Frontend: React 18, TypeScript, Vite, Zustand, Axios
- Testing: JUnit 5, jqwik, Vitest, fast-check

## Additional Dependencies (Unit 2)

### Frontend

| Package | Version | Purpose |
|---------|---------|---------|
| ag-grid-community | ^32.x | 테이블 그리드 (가상 스크롤, D&D, 인라인 편집) |
| ag-grid-react | ^32.x | React wrapper for AG Grid |

### Backend

| Package | Version | Purpose |
|---------|---------|---------|
| (No additional) | - | JPA + SQLite로 충분. 검색은 JPQL LIKE 사용 |

## AG Grid Configuration Decisions

| Decision | Choice | Rationale |
|----------|--------|-----------|
| Edition | Community | 무료, 필요 기능 충분 (row drag, selection, editors) |
| Row Model | Client-Side | 10,000행 이하로 client-side에서 충분히 처리 |
| Virtual Scroll | AG Grid 기본 지원 | DOM에 보이는 행만 렌더링 |
| Cell Editor | Built-in (text, select) + Custom (autocomplete) | 대부분 내장 에디터 사용, 자동완성만 커스텀 |
| Row Drag | Managed | AG Grid 내장 D&D 사용 |
| Selection | Multiple (checkbox) | 체크박스 + Shift/Ctrl 지원 |

## Performance Strategy

| Concern | Strategy |
|---------|----------|
| 대량 행 렌더링 | AG Grid 가상 스크롤 (기본) |
| 전체 데이터 로딩 | 서버에서 전체 반환 (10,000행 이하이므로 페이징 불필요) |
| 자동 계산 필드 | 서버에서 한 번에 계산하여 응답에 포함 |
| 검색 | AG Grid quickFilterText (클라이언트 필터) + 서버 필터 병행 |
| 인라인 편집 | 낙관적 업데이트 없이 서버 응답 대기 후 반영 |
| 자동완성 | 300ms 디바운스 + 서버 LIKE 쿼리 (LIMIT 20) |

## SQLite Considerations

| Concern | Approach |
|---------|----------|
| 동시 쓰기 | SQLite WAL 모드 활성화 (spring 설정) |
| LIKE 검색 성능 | 10,000행 이하로 인덱스 없이도 충분 |
| 트랜잭션 | Spring @Transactional 사용 |
| 행 순서 업데이트 | Batch UPDATE 문 (단일 트랜잭션) |

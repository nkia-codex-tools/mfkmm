# Infrastructure Design - Unit 2: Resource Management

## Infrastructure Reuse

Unit 2는 Unit 1과 동일한 인프라를 공유합니다:
- 동일 Spring Boot 앱 (새 도메인 패키지 추가)
- 동일 Docker Compose 구성
- 동일 SQLite 데이터베이스 파일
- 동일 Nginx 리버스 프록시

**변경 사항: DB 스키마 추가만 필요**

## Database Schema Addition

### functions 테이블
```sql
CREATE TABLE IF NOT EXISTS functions (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    a_class TEXT,
    b_class TEXT,
    c_class TEXT,
    action TEXT,
    function_name TEXT,
    function_id TEXT NOT NULL,
    type TEXT,
    light INTEGER NOT NULL DEFAULT 0,
    standard INTEGER NOT NULL DEFAULT 0,
    enterprise INTEGER NOT NULL DEFAULT 0,
    system_menu INTEGER NOT NULL DEFAULT 0,
    product_domain TEXT,
    domain_license_resource_type TEXT,
    related_services TEXT,
    row_order INTEGER NOT NULL,
    created_by INTEGER NOT NULL,
    updated_by INTEGER NOT NULL,
    created_at TEXT NOT NULL,
    updated_at TEXT NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_functions_row_order ON functions(row_order);
CREATE INDEX IF NOT EXISTS idx_functions_function_id ON functions(function_id);
```

### menus 테이블
```sql
CREATE TABLE IF NOT EXISTS menus (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    main_menu TEXT,
    sub_menu_group TEXT,
    sub_menu TEXT,
    menu_level1 TEXT,
    menu_level2 TEXT,
    menu_level3 TEXT,
    menu_id TEXT,
    is_menu INTEGER NOT NULL DEFAULT 1,
    is_system_menu INTEGER NOT NULL DEFAULT 0,
    function_id TEXT,
    menu_icon TEXT,
    row_order INTEGER NOT NULL,
    created_by INTEGER NOT NULL,
    updated_by INTEGER NOT NULL,
    created_at TEXT NOT NULL,
    updated_at TEXT NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_menus_row_order ON menus(row_order);
CREATE INDEX IF NOT EXISTS idx_menus_function_id ON menus(function_id);
```

### message_resources 테이블
```sql
CREATE TABLE IF NOT EXISTS message_resources (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    module TEXT,
    resource_key TEXT,
    korean TEXT,
    english TEXT,
    japanese TEXT,
    description TEXT,
    registered_date TEXT,
    registered_by TEXT,
    row_order INTEGER NOT NULL,
    created_by INTEGER NOT NULL,
    updated_by INTEGER NOT NULL,
    created_at TEXT NOT NULL,
    updated_at TEXT NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_message_resources_row_order ON message_resources(row_order);
CREATE INDEX IF NOT EXISTS idx_message_resources_resource_key ON message_resources(resource_key);
```

## SQLite Configuration

### WAL Mode Activation
```yaml
# application.yml 추가
spring:
  datasource:
    hikari:
      connection-init-sql: "PRAGMA journal_mode=WAL; PRAGMA busy_timeout=5000;"
```

**WAL(Write-Ahead Logging) 모드 이점**:
- 읽기와 쓰기 동시 수행 가능
- 쓰기 중 읽기 블록 없음
- busy_timeout으로 잠금 대기 (5초)

## Performance Indices

| Table | Index | Purpose |
|-------|-------|---------|
| functions | idx_functions_row_order | ORDER BY rowOrder ASC 최적화 |
| functions | idx_functions_function_id | functionId 검색 (자동완성) |
| menus | idx_menus_row_order | ORDER BY rowOrder ASC 최적화 |
| menus | idx_menus_function_id | functionId 참조 조회 |
| message_resources | idx_message_resources_row_order | ORDER BY rowOrder ASC 최적화 |
| message_resources | idx_message_resources_resource_key | resourceKey 중복 검사 |

## Frontend Static Asset

### AG Grid CSS
- AG Grid Community CSS는 프론트엔드 빌드에 포함
- 추가 인프라 변경 없음 (Nginx에서 기존 정적 파일 서빙)

## Deployment Impact

| Aspect | Impact |
|--------|--------|
| Docker Compose | 변경 없음 |
| Backend Dockerfile | 변경 없음 (동일 JAR) |
| Frontend Dockerfile | 변경 없음 (동일 빌드) |
| Nginx | 변경 없음 |
| EBS Volume | 변경 없음 (20GB 충분) |
| Memory | 변경 없음 (512MB backend 충분) |

## Data Volume Estimate

| Resource | Max Rows | Columns | Estimated Size |
|----------|----------|---------|---------------|
| functions | 10,000 | 15 | ~5MB |
| menus | 10,000 | 12 | ~4MB |
| message_resources | 10,000 | 10 | ~8MB (다국어 텍스트) |
| **Total** | 30,000 | - | **~17MB** |

SQLite 파일 크기: 최대 ~30MB (인덱스 포함) - Docker volume 20GB 내 충분

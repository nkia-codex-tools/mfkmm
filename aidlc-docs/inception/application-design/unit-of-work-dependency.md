# Unit of Work Dependency Matrix

## Dependency Matrix

| Unit | Depends On | Dependency Type | Reason |
|------|-----------|-----------------|--------|
| Unit 1: Auth | (none) | - | 독립적, 첫 번째로 개발 |
| Unit 2: Resource Management | Unit 1: Auth | Hard | 인증된 사용자만 리소스 접근 가능, 권한 검증 필요 |
| Unit 3: Import/Export | Unit 2: Resource | Hard | 리소스 테이블 및 Repository 활용 |
| Unit 4: History | Unit 2: Resource | Hard | 리소스 CRUD 작업에 이력 기록 통합 |
| Unit 5: Version | Unit 2: Resource, Unit 4: History | Hard | 스냅샷 생성은 리소스 데이터 필요, 롤백은 이력 기록 필요 |

## Visual Dependency Graph

```
+------------+
|  Unit 1    |
|   Auth     |
+-----+------+
      |
      v
+------------+
|  Unit 2    |
|  Resource  |
+-----+------+
      |
      +------------------+------------------+
      |                  |                  |
      v                  v                  |
+------------+    +------------+            |
|  Unit 3    |    |  Unit 4    |            |
| Import/Exp |    |  History   |            |
+------------+    +-----+------+            |
                        |                   |
                        +-------------------+
                        |
                        v
                  +------------+
                  |  Unit 5    |
                  |  Version   |
                  +------------+
```

## Shared Resources

| Resource | Used By | Access Pattern |
|----------|---------|---------------|
| users 테이블 | Unit 1 (read/write), Unit 2-5 (read: 현재 사용자 조회) | Unit 1이 소유, 나머지는 userId 참조만 |
| functions 테이블 | Unit 2 (CRUD), Unit 3 (import/export), Unit 5 (스냅샷/롤백) | Unit 2가 소유 |
| menus 테이블 | Unit 2 (CRUD), Unit 3 (import/export), Unit 5 (스냅샷/롤백) | Unit 2가 소유 |
| message_resources 테이블 | Unit 2 (CRUD), Unit 3 (import/export), Unit 5 (스냅샷/롤백) | Unit 2가 소유 |
| change_history 테이블 | Unit 4 (write), Unit 2-5 (trigger writes via HistoryService) | Unit 4가 소유 |
| version_tags 테이블 | Unit 5 (read/write) | Unit 5가 소유 |
| JwtTokenProvider | Unit 1 (생성), 모든 요청 (검증) | Unit 1 소유, 공통 필터로 적용 |
| SecurityConfig | Unit 1 (정의), 전체 (적용) | Unit 1에서 정의, 전역 적용 |

## Integration Points

| Integration | Provider Unit | Consumer Unit | Interface |
|-------------|--------------|---------------|-----------|
| JWT 인증 | Unit 1 | Unit 2, 3, 4, 5 | JwtAuthenticationFilter (HTTP Filter) |
| 권한 검증 | Unit 1 | Unit 2, 3, 4, 5 | @PreAuthorize / SecurityContext |
| 이력 기록 | Unit 4 | Unit 2, 3, 5 | HistoryService.recordChange() |
| 기능 ID 참조 | Unit 2 (Function) | Unit 2 (Menu) | FunctionRepository 직접 조회 |
| 리소스 스냅샷 | Unit 2 | Unit 5 | 각 Repository의 findAll() |
| 리소스 복원 | Unit 5 | Unit 2 | 각 Repository의 deleteAll() + saveAll() |

## Development Constraints

1. **Unit 1 반드시 먼저**: SecurityConfig와 JWT 인프라가 없으면 다른 유닛의 Controller 테스트 불가
2. **Unit 2 완성 후 Unit 3, 4**: Import/Export와 History 모두 리소스 엔티티와 Repository에 의존
3. **Unit 4 완성 후 Unit 5**: 롤백 시 이력 기록 필요
4. **Unit 3과 Unit 4 병렬 가능**: 서로 직접 의존 없음 (다만 import 시 이력 기록은 Unit 4 완성 후 통합)

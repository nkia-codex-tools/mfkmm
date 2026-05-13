# NFR Requirements - DataIO Service + Deploy Service + Frontend

## NFR-PERF: Performance Requirements

### NFR-PERF-01: Import Processing Time
- **Target**: 특별한 기준 없음 (합리적 범위)
- **Context**: 5MB 이하 파일 동기 처리, 행 단위로 Resource Service REST 호출 포함
- **Expected range**: 파일 크기에 비례 (1000행 기준 5~15초)
- **Bottleneck**: 행마다 중복 검사 REST 호출이 주요 지연 요인

### NFR-PERF-02: Export/Download Response Time
- **Target**: 특별한 기준 없음
- **Context**: 전체 리소스 수 수천 건 수준 (사내 도구)
- **Expected range**: 1~5초 (형식 변환 + 파일 생성)

### NFR-PERF-03: Frontend Initial Load
- **Target**: < 3초 (정적 자산 로드 + 초기 렌더링)
- **Rationale**: 사내 도구이므로 엄격하지 않으나 합리적 UX 보장
- **Strategy**: Code splitting (route-based lazy loading)

### NFR-PERF-04: Concurrent Users
- **Target**: 10~50명 동시 사용
- **Rationale**: 사내 도구 규모, Dev 1과 동일 기준

---

## NFR-REL: Reliability Requirements

### NFR-REL-01: Import Atomicity
- **Strategy**: Best-effort with result report
- **Behavior**: 개별 행 단위 처리, 일부 실패해도 나머지 계속 진행
- **Rationale**: 트랜잭션 롤백보다 결과 리포트(성공/실패/스킵)가 사용자에게 더 유용
- **Error recovery**: 실패 건 목록을 사용자에게 제공하여 수동 재처리

### NFR-REL-02: File Storage (Deploy)
- **Strategy**: 서버 로컬 디스크 저장
- **Path**: `/data/deployments/{version}/` (Docker volume mount)
- **Durability**: Docker named volume으로 컨테이너 재시작 시에도 유지
- **Rationale**: 사내 도구 규모에서 별도 Object Storage(S3 등) 불필요

### NFR-REL-03: Event Publishing
- **Guarantee**: At-least-once delivery (Dev 1과 동일 RabbitMQ 설정)
- **Events**: ImportCompleted, ExportCompleted, DeployCompleted
- **Consumer**: History Service에서 수신하여 이력 기록
- **Deduplication**: History Service 측 eventId 중복 제거

### NFR-REL-04: Service Availability
- **Strategy**: Docker Health Check + 자동 재시작 (Dev 1과 동일)
- **Health endpoint**: `GET /actuator/health`
- **Restart policy**: on-failure (max 5 retries)

---

## NFR-SEC: Security Requirements

### NFR-SEC-01: File Upload Security
- **Size limit**: 5MB 서버 측 강제 (Spring multipart config)
- **Type validation**: 서버 측 확장자 + MIME type 검증
- **Path traversal**: 업로드 파일명을 UUID로 교체 (원본 파일명 사용 안함)
- **Temp storage**: 처리 완료 후 임시 파일 즉시 삭제

### NFR-SEC-02: Deploy Permission
- **Authorization**: API Gateway에서 X-User-Role 헤더로 전달받은 role 검증
- **Required role**: ADMIN 또는 ROOT_ADMIN
- **Enforcement**: Controller 레벨에서 검증

### NFR-SEC-03: Frontend Token Security
- **Storage**: 메모리만 사용 (localStorage/sessionStorage 미사용)
- **XSS mitigation**: React 기본 이스케이핑 + dangerouslySetInnerHTML 미사용
- **CORS**: API Gateway에서 프론트엔드 origin만 허용 (Dev 1에서 구현 완료)

---

## NFR-OPS: Operational Requirements

### NFR-OPS-01: Logging
- **Format**: stdout/stderr → Docker logs (Dev 1과 동일)
- **Structure**: Plain text (Spring Boot default)
- **Frontend**: Browser console (개발), 서버로 전송 안함
- **Rationale**: Dev 1과 일관성 유지

### NFR-OPS-02: Health Monitoring
- Spring Boot Actuator `/actuator/health` endpoint
- DataIO Service: MongoDB connection + disk space
- Deploy Service: MongoDB connection + disk space + deployment volume accessible
- Frontend: static files served (nginx health check)

### NFR-OPS-03: Disk Space Monitoring
- Deploy 파일 누적으로 디스크 사용량 증가 가능
- Health check에서 디스크 사용량 확인 (Spring Boot Actuator disk space indicator)
- 임계값: 기본 10% 여유 공간 (Spring Boot default)

---

## NFR-TEST: Testing Requirements (PBT Extension)

### NFR-TEST-01: Backend PBT (jqwik)
- **DataIO Service targets**:
  - Import count invariant: successCount + failedCount + skippedCount == totalRows
  - Export/Import round-trip: EXPORT → IMPORT 시 동일 데이터 복원
  - File size rejection: 5MB 초과 시 항상 거부
  - Conflict policy correctness: SKIP이면 데이터 불변, OVERWRITE면 갱신
- **Deploy Service targets**:
  - Deploy requires admin: non-admin은 항상 403
  - Redownload consistency: 같은 ID로 재다운로드 시 동일 파일

### NFR-TEST-02: Frontend PBT (fast-check)
- **Targets**:
  - Pagination invariant: 어떤 page/size 조합이든 유효한 범위 내 결과
  - Token refresh retry: 401 수신 후 refresh 성공 시 원래 요청 재실행
  - File validation: 허용되지 않은 확장자/크기는 항상 거부
  - Permission UI invariant: role에 따른 메뉴 표시 일관성

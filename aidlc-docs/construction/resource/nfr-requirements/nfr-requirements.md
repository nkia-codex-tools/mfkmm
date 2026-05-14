# NFR Requirements - Unit 2: Resource Management

## 1. Performance Requirements

| ID | Requirement | Target |
|----|------------|--------|
| PERF-RES-01 | 리소스 전체 조회 (10,000행) | < 500ms (서버) |
| PERF-RES-02 | AG Grid 렌더링 (10,000행, 가상 스크롤) | < 1초 초기 렌더 |
| PERF-RES-03 | 인라인 편집 저장 (단일 셀) | < 300ms |
| PERF-RES-04 | 행 순서 변경 (전체 reorder) | < 1초 (10,000행) |
| PERF-RES-05 | 검색/필터링 응답 | < 300ms |
| PERF-RES-06 | 일괄 삭제 (100행) | < 1초 |
| PERF-RES-07 | 자동완성 드롭다운 응답 | < 200ms |
| PERF-RES-08 | 자동 계산 필드 (MessageResource counts) | < 500ms (전체 데이터 스캔) |

## 2. Scalability Requirements

| ID | Requirement | Target |
|----|------------|--------|
| SCALE-RES-01 | 각 리소스 탭 최대 행 수 | 10,000행 |
| SCALE-RES-02 | 동시 편집 사용자 | 최대 10명 (동시 편집 충돌 처리 불필요 - 제외 기능) |
| SCALE-RES-03 | 컬럼 수 | 최대 15개 (현재 사양) |

## 3. Usability Requirements

| ID | Requirement | Target |
|----|------------|--------|
| UX-RES-01 | 드래그앤드롭 핸들 | 직관적 아이콘 표시 (행 좌측) |
| UX-RES-02 | 인라인 편집 진입 | 셀 더블클릭 또는 Enter |
| UX-RES-03 | 변경 사항 표시 | 수정된 셀 하이라이트 (배경색 변경) |
| UX-RES-04 | 드롭다운 | 클릭 즉시 옵션 표시 |
| UX-RES-05 | 자동완성 | 타이핑 300ms 디바운스 후 검색 |
| UX-RES-06 | 읽기 전용 필드 | 회색 배경으로 편집 불가 시각적 표시 |
| UX-RES-07 | 일괄 삭제 확인 | "N개의 행을 삭제하시겠습니까?" 팝업 |

## 4. Security Requirements (Unit 1 인프라 재사용)

| ID | Requirement | SECURITY Rule |
|----|------------|---------------|
| SEC-RES-01 | READER는 GET만 허용 | SECURITY-08 |
| SEC-RES-02 | WRITER/ADMIN만 CUD 허용 | SECURITY-08 |
| SEC-RES-03 | 모든 입력 필드 길이 제한 | SECURITY-05 |
| SEC-RES-04 | SQL Injection 방지 (JPA parameterized queries) | SECURITY-05 |
| SEC-RES-05 | XSS 방지 (입력 값 이스케이프) | SECURITY-05 |

## 5. Reliability Requirements

| ID | Requirement | Target |
|----|------------|--------|
| REL-RES-01 | 인라인 편집 실패 시 | 셀 값 원래 값으로 롤백 + 에러 토스트 |
| REL-RES-02 | 네트워크 오류 시 | 재시도 안내 메시지 |
| REL-RES-03 | 대량 데이터 로딩 실패 | 에러 메시지 + 새로고침 버튼 |

## 6. Data Integrity

| ID | Requirement | Target |
|----|------------|--------|
| INT-RES-01 | rowOrder 연속성 | 모든 변경 후 1~N 연속 (갭 없음) |
| INT-RES-02 | 자동 계산 필드 일관성 | 동일 데이터 → 동일 결과 (결정적) |
| INT-RES-03 | 메뉴 functionId 참조 | 경고만 표시, 차단 안 함 (soft reference) |

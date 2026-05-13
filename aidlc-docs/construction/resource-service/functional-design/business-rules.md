# Business Rules - Resource Service

## BR-RES: Resource CRUD Rules

### BR-RES-01: Create Resource
- Write 권한 이상만 등록 가능
- 등록 전 중복 검사 수행 (BR-DUP-01)
- 중복 없으면 유사도 탐지 수행 (BR-SIM-01)
- 유사 후보가 있으면 사용자에게 반환 (최종 결정은 사용자)
- 사용자가 "신규 등록 강행" 선택 시 저장
- createdBy, createdAt 자동 설정
- ResourceCreated 이벤트 발행

### BR-RES-02: Update Resource
- Write 권한 이상만 수정 가능
- 수정 시에도 중복 검사 수행 (변경된 값이 다른 리소스와 중복되는지)
- 변경 전 스냅샷(previousContent) 보존
- updatedBy, updatedAt 자동 갱신
- ResourceUpdated 이벤트 발행

### BR-RES-03: Soft Delete
- Write 권한 이상만 삭제 가능
- deleted = true, deletedAt = now() 설정
- Soft Delete된 리소스는 검색 결과에서 제외
- ResourceDeleted 이벤트 발행

### BR-RES-04: Hard Delete (Scheduled)
- deletedAt 기준 30일 경과한 리소스를 물리적으로 삭제
- 매일 새벽 3시 스케줄러 실행
- 삭제 건수 로그 기록

### BR-RES-05: Search
- 모든 인증 사용자 검색 가능 (READ 이상)
- 검색 대상: resourceKey, content (키워드 부분 일치)
- 필터: resourceType, createdBy, 기간
- deleted=true인 리소스는 결과에서 제외
- 페이지네이션 및 정렬 지원
- ResourceSearched 이벤트 발행 (검색 이력 기록용)

---

## BR-DUP: Duplicate Check Rules

### BR-DUP-01: Exact Duplicate Check
- 검사 대상: resourceKey (정확 일치)
- 동일 resourceKey가 존재하면 (deleted=false) → 중복으로 판정
- 중복 발견 시 DuplicateCheckResult 반환 (isDuplicate=true, existingResource)
- 중복 상태에서는 등록 차단

---

## BR-SIM: Similarity Rules

### BR-SIM-01: Similarity Detection
- 중복이 아닌 경우에만 실행
- 탐지 방식:
  1. resourceKey 부분 일치 (MongoDB regex)로 1차 후보 추출
  2. 각 후보에 대해 Levenshtein 편집 거리 계산
  3. 편집 거리 ≤ 3인 것만 유사 후보로 인정
- content에 대해서도 동일하게 수행
- 최대 5개 후보 반환 (편집 거리 오름차순 정렬)

### BR-SIM-02: Similarity Choice
- 유사 후보가 있으면 사용자에게 선택 옵션 제공:
  - **USE_EXISTING**: 기존 리소스 사용 (등록 취소)
  - **CREATE_NEW**: 신규 등록 강행 (사유 입력 가능)
- 선택 결과를 SimilarityChoiceMade 이벤트로 발행

---

## BR-LEVENSHTEIN: Algorithm Rules

### BR-LEVENSHTEIN-01: Calculation
- 표준 Levenshtein 편집 거리 (삽입, 삭제, 치환 각 비용 1)
- 대소문자 무시 (비교 전 lowercase 변환)
- 공백 무시하지 않음 (공백도 문자로 취급)

### BR-LEVENSHTEIN-02: Threshold
- 유사 판정 기준: 편집 거리 ≤ 3
- 임계값은 고정 (application.yml에서 변경 불가, 향후 확장 가능)

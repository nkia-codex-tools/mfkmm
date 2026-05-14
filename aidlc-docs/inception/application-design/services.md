# Services Definition

## Backend Service Layer

### Service Orchestration Patterns

```
Controller → Service → Repository
                    → HistoryService (변경 이력 자동 기록)
                    → VersionService (스냅샷 생성 시)
```

---

### 1. AuthService

**책임**: 인증 및 토큰 관리

**오케스트레이션**:
- 로그인: 사용자 조회 → 비밀번호 검증 → JWT 생성 → 로그인 이력 기록
- 회원가입: 중복 확인 → 비밀번호 해싱 → 사용자 생성 (PENDING 상태)
- 토큰 갱신: Refresh Token 검증 → 새 Access Token 발급

**의존성**: UserRepository, JwtTokenProvider, PasswordEncoder

---

### 2. UserService

**책임**: 사용자 관리 및 권한 부여

**오케스트레이션**:
- 권한 변경: 사용자 조회 → 권한 변경 → 이력 기록

**의존성**: UserRepository, HistoryService

---

### 3. FunctionService

**책임**: 기능 리소스 CRUD + 자동생성 필드

**오케스트레이션**:
- 생성: 요청 검증 → 리소스키 자동생성 → 저장 → 이력 기록
- 수정: 기존 조회 → diff 계산 → 업데이트 → 이력 기록
- 삭제: 메뉴 탭 참조 확인 → 삭제 → 이력 기록
- 순서 변경: row_order 업데이트 → 이력 기록
- 일괄 삭제: 각 행 삭제 → 일괄 이력 기록

**의존성**: FunctionRepository, HistoryService

---

### 4. MenuService

**책임**: 메뉴 리소스 CRUD + 기능 참조

**오케스트레이션**:
- 생성: 요청 검증 → 기능 ID 존재 확인 → 기능명 자동 조회 → 저장 → 이력 기록
- 수정: 기존 조회 → 기능 ID 변경 시 기능명 재조회 → diff 계산 → 업데이트 → 이력 기록
- 기능 ID 자동완성: FunctionRepository에서 LIKE 검색

**의존성**: MenuRepository, FunctionRepository, HistoryService

---

### 5. MessageResourceService

**책임**: 리소스 키 CRUD + 자동계산 필드

**오케스트레이션**:
- 생성: 요청 검증 → full_resource_key 계산 → 저장 → 이력 기록
- 조회: 데이터 로드 → 자동계산 필드 부착 (번호, 중복/대문자, 개수들)
- 수정: 기존 조회 → diff 계산 → 업데이트 → 이력 기록

**자동계산 필드** (조회 시 서버에서 계산하여 응답):
- 번호: 모듈과 resource_key 모두 존재 시 순번
- 중복/대문자: 국문 중복 검사 + resource_key 대소문자 검증
- full_resource_key: 모듈 + "." + resource_key
- 리소스키/국문/영문/일문 개수: COUNTIF 등가 집계

**의존성**: MessageResourceRepository, HistoryService

---

### 6. TsvExportService

**책임**: TSV 형식 데이터 내보내기

**오케스트레이션**:
- 전체 export: 리소스 전체 조회 → 헤더 생성 → 행 변환 → UTF-8 TSV 파일 생성
- 선택 export: 선택 ID 조회 → 동일 과정

**의존성**: FunctionRepository, MenuRepository, MessageResourceRepository

---

### 7. TsvImportService

**책임**: TSV 파일 업로드 및 데이터 적용

**오케스트레이션**:
- 미리보기: 파일 파싱 → 유효성 검증 → 기존 데이터와 비교 → 변경/추가/충돌 분류 → 미리보기 응답
- 적용: 사용자 선택 반영 (덮어쓰기/건너뛰기) → 데이터 저장 → 이력 기록 → 결과 요약

**의존성**: TsvParser, FunctionRepository/MenuRepository/MessageResourceRepository, HistoryService

---

### 8. HistoryService

**책임**: 모든 변경 이력 기록 및 조회

**오케스트레이션**:
- 기록: 변경 정보 수신 → 이력 레코드 저장 (리소스 타입, 행 ID, 필드명, 이전/이후 값, 변경자, 시각)
- 조회: 필터 적용 (날짜, 사용자, 리소스 종류) → 페이징 결과 반환

**호출 방식**: AOP Aspect 또는 각 서비스에서 직접 호출

**의존성**: HistoryRepository

---

### 9. VersionService

**책임**: 버전 태깅 및 롤백

**오케스트레이션**:
- 태그 생성: 리소스 타입의 현재 전체 데이터 스냅샷 생성 (JSON) → 태그 저장
- 롤백: 태그 스냅샷 로드 → 현재 데이터 삭제 → 스냅샷 데이터 복원 → 이력 기록 (롤백 이벤트)
- diff 조회: 두 버전 스냅샷 비교 → 차이점 반환

**의존성**: VersionRepository, FunctionRepository/MenuRepository/MessageResourceRepository, HistoryService

---

## Frontend Service Layer

### API Service (Axios Instance)
```
apiClient: Axios instance with JWT interceptor, error handling, base URL config
```

### Resource API Services
```
functionApi: CRUD + reorder + batch operations for Functions
menuApi: CRUD + reorder + function ID search for Menus
messageResourceApi: CRUD + reorder for Message Resources
```

### Auth API Service
```
authApi: login, register, refresh, logout
```

### Transfer API Service
```
transferApi: export, importPreview, importApply
```

### History API Service
```
historyApi: getHistory with filters
```

### Version API Service
```
versionApi: getVersions, createTag, getDiff, rollback
```

# Domain Entities - History Service

## WorkLog
| Field | Type | Description |
|---|---|---|
| id | String (ObjectId) | 고유 식별자 |
| workLogType | WorkLogType (enum) | 작업 종류 |
| userId | String | 작업자 userId |
| resourceId | String | 대상 리소스 ID (nullable) |
| resourceKey | String | 대상 리소스 키 (nullable) |
| previousValue | String | 변경 전 값 (nullable) |
| newValue | String | 변경 후 값 (nullable) |
| details | String | 추가 상세 (JSON, nullable) |
| success | boolean | 성공/실패 |
| failureReason | String | 실패 사유 (nullable) |
| performedAt | Instant | 작업 시각 |
| sourceEvent | String | 원본 이벤트 ID (중복 처리용) |

---

## WorkLogType (Enum)
```
SEARCH              — 검색 수행
RESOURCE_CREATED    — 리소스 등록
RESOURCE_UPDATED    — 리소스 변경
RESOURCE_DELETED    — 리소스 삭제
IMPORT              — 데이터 IMPORT
EXPORT              — 데이터 EXPORT
DEPLOY              — 배포
USER_CREATED        — 사용자 생성
USER_DELETED        — 사용자 삭제
PERMISSION_CHANGED  — 권한 변경
LOGIN               — 로그인
LOGOUT              — 로그아웃
ACCOUNT_LOCKED      — 계정 잠금
ACCOUNT_UNLOCKED    — 계정 잠금 해제
SIMILARITY_CHOICE   — 유사 제안 선택
```

---

## Event-to-WorkLog Mapping

| Source Event | WorkLogType | Key Fields Extracted |
|---|---|---|
| ResourceCreated | RESOURCE_CREATED | resourceId, resourceKey, newValue=content |
| ResourceUpdated | RESOURCE_UPDATED | resourceId, resourceKey, previousValue, newValue |
| ResourceDeleted | RESOURCE_DELETED | resourceId, resourceKey |
| ResourceSearched | SEARCH | details={query, resultCount} |
| ImportCompleted | IMPORT | details={successCount, failCount, skipCount} |
| ExportCompleted | EXPORT | details={format, recordCount} |
| DeployCompleted | DEPLOY | details={format, recordCount, version} |
| UserCreated | USER_CREATED | details={userId, role} |
| UserDeleted | USER_DELETED | details={userId} |
| PermissionChanged | PERMISSION_CHANGED | previousValue=oldRole, newValue=newRole |
| UserLoggedIn | LOGIN | details={ipAddress} |
| UserLoggedOut | LOGOUT | — |
| AccountLocked | ACCOUNT_LOCKED | details={failedAttempts} |
| AccountUnlocked | ACCOUNT_UNLOCKED | details={unlockedBy} |
| SimilarityChoiceMade | SIMILARITY_CHOICE | details={inputKey, chosenAction, reason} |

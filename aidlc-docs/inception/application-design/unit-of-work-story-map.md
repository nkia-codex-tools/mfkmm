# MKFMM User Story → Unit Mapping

## Story-to-Unit Assignment

| Story ID | Story Name | Assigned Unit(s) |
|---|---|---|
| US-1.1 | 시스템 로그인 | Auth Service + Frontend (Auth Module) |
| US-1.2 | 계정 잠금 | Auth Service + User Service (잠금 해제) + Frontend |
| US-1.3 | 로그아웃 | Auth Service + Frontend (Auth Module) |
| US-2.1 | 사용자 등록 | User Service + Frontend (Admin Module) |
| US-2.2 | 권한 관리 | User Service + Frontend (Admin Module) |
| US-2.3 | 사용자 삭제 | User Service + Frontend (Admin Module) |
| US-2.4 | 시스템 최상위 관리자 초기화 | Auth Service + User Service |
| US-3.1 | 리소스 검색 | Resource Service + Frontend (Resource Module) |
| US-4.1 | 리소스 등록 (중복/유사) | Resource Service + Frontend (Resource Module) |
| US-4.2 | 리소스 변경 | Resource Service + Frontend (Resource Module) |
| US-4.3 | 리소스 삭제 | Resource Service + Frontend (Resource Module) |
| US-5.1 | 데이터 IMPORT | DataIO Service + Frontend (DataIO Module) |
| US-5.2 | 데이터 EXPORT | DataIO Service + Frontend (DataIO Module) |
| US-6.1 | 전체 데이터 배포 | Deploy Service + Frontend (Admin Module) |
| US-7.1 | 작업 이력 조회 (전체) | History Service + Frontend (Admin Module) |
| US-7.2 | 본인 작업 이력 조회 | History Service + Frontend (Resource Module) |
| US-7.3 | 로그인 이력 조회 | Auth Service + Frontend (Admin Module) |

---

## Unit-to-Story Assignment (역방향)

### Unit 1: Auth Service
| Story | Priority |
|---|---|
| US-1.1 시스템 로그인 | Must |
| US-1.2 계정 잠금 | Must |
| US-1.3 로그아웃 | Must |
| US-2.4 Root Admin 초기화 (인증 정보 부분) | Must |
| US-7.3 로그인 이력 조회 | Should |

### Unit 2: User Service
| Story | Priority |
|---|---|
| US-2.1 사용자 등록 | Must |
| US-2.2 권한 관리 | Must |
| US-2.3 사용자 삭제 | Must |
| US-2.4 Root Admin 초기화 (계정 부분) | Must |
| US-1.2 계정 잠금 해제 (Admin 기능) | Must |

### Unit 3: History Service
| Story | Priority |
|---|---|
| US-7.1 작업 이력 조회 (전체) | Must |
| US-7.2 본인 작업 이력 조회 | Must |

### Unit 4: Resource Service
| Story | Priority |
|---|---|
| US-3.1 리소스 검색 | Must |
| US-4.1 리소스 등록 (중복/유사) | Must |
| US-4.2 리소스 변경 | Must |
| US-4.3 리소스 삭제 | Must |

### Unit 5: DataIO Service
| Story | Priority |
|---|---|
| US-5.1 데이터 IMPORT | Must |
| US-5.2 데이터 EXPORT | Must |

### Unit 6: Deploy Service
| Story | Priority |
|---|---|
| US-6.1 전체 데이터 배포 | Must |

### Unit 7: API Gateway
| Story | Priority |
|---|---|
| (Cross-cutting) JWT 검증, 라우팅 | Must |

### Unit 8: Frontend
| Story | Priority |
|---|---|
| US-1.1 ~ US-7.3 (전체 UI) | Must |

---

## Coverage Verification

| Check | Status |
|---|---|
| 모든 스토리가 최소 1개 유닛에 할당됨 | ✅ |
| 모든 유닛에 최소 1개 스토리가 할당됨 | ✅ |
| Must 스토리 16개 모두 할당됨 | ✅ |
| Should 스토리 1개 (US-7.3) 할당됨 | ✅ |
| Frontend는 모든 사용자 대면 스토리 포함 | ✅ |

---

## Implementation Phase → Story Coverage

| Phase | Units | Stories Covered |
|---|---|---|
| Phase 1 | Auth + Gateway + Shared | US-1.1, US-1.2, US-1.3, US-2.4(일부), US-7.3 |
| Phase 2 | User | US-2.1, US-2.2, US-2.3, US-2.4(일부), US-1.2(해제) |
| Phase 3 | History | US-7.1, US-7.2 |
| Phase 4 | Resource + Frontend | US-3.1, US-4.1, US-4.2, US-4.3 + 전체 UI |
| Phase 5 | DataIO | US-5.1, US-5.2 |
| Phase 6 | Deploy | US-6.1 |

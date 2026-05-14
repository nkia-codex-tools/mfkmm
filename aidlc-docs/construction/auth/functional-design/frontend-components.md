# Frontend Components - Unit 1: Auth

## 1. Page Components

### LoginPage

**Route**: `/login`

**State**:
- email: string
- password: string
- isLoading: boolean
- error: string | null

**Behavior**:
- 이메일/비밀번호 폼 입력
- Submit → authApi.login() → 성공 시 useAuthStore에 토큰 저장 → `/resources`로 리다이렉트
- 실패 시 에러 메시지 표시 (잘못된 인증정보, 계정 잠금 등)
- "회원가입" 링크 → `/register`

**Validation**:
- 이메일: 비어있지 않음, 이메일 형식
- 비밀번호: 비어있지 않음

---

### RegisterPage

**Route**: `/register`

**State**:
- name: string
- email: string
- password: string
- confirmPassword: string
- isLoading: boolean
- error: string | null
- success: boolean

**Behavior**:
- 이름/이메일/비밀번호/비밀번호확인 폼 입력
- Submit → authApi.register() → 성공 시 "가입 완료, 관리자 승인 대기" 메시지 → 로그인 페이지 링크
- 실패 시 에러 메시지 표시 (이메일 중복 등)

**Validation**:
- 이름: 비어있지 않음, 최대 100자
- 이메일: 비어있지 않음, 이메일 형식
- 비밀번호: 최소 8자
- 비밀번호 확인: password와 일치

---

### AdminPage

**Route**: `/admin`
**Access**: ADMIN role only

**Sections**:
1. 신규 가입자 (PendingUserList)
2. 전체 사용자 목록 (UserList)

---

## 2. Admin Components

### PendingUserList

**Props**: (none - 내부에서 데이터 fetch)

**State**:
- pendingUsers: UserResponse[]
- isLoading: boolean

**Behavior**:
- 마운트 시 GET /api/admin/users/pending 호출
- 각 사용자에 대해 "권한 부여" 버튼 표시
- 버튼 클릭 → PermissionDialog 오픈

**표시 정보**: 이름, 이메일, 가입일시

---

### UserList

**Props**: (none - 내부에서 데이터 fetch)

**State**:
- users: UserResponse[]
- isLoading: boolean
- page: number
- totalPages: number

**Behavior**:
- 마운트 시 GET /api/admin/users 호출 (페이징)
- 각 사용자의 현재 역할 표시
- "역할 변경" 버튼 → PermissionDialog 오픈
- 페이지네이션 지원

**표시 정보**: 이름, 이메일, 역할, 상태, 가입일시

---

### PermissionDialog

**Props**:
- user: UserResponse
- open: boolean
- onClose: () => void
- onSuccess: () => void

**State**:
- selectedRole: Role
- isLoading: boolean
- error: string | null

**Behavior**:
- 역할 선택 드롭다운 (READER, WRITER, ADMIN)
- "저장" 버튼 → PATCH /api/admin/users/{id}/role
- 성공 시 onSuccess 콜백 (목록 새로고침)
- 실패 시 에러 표시 (마지막 관리자, 자기 자신 등)

---

## 3. Common Components

### AppLayout

**Props**: children: ReactNode

**Structure**:
```
+----------------------------------+
| Header (로고, 사용자 정보, 로그아웃) |
+----------------------------------+
| Sidebar     |    Content          |
| (Navigation)|    (children)       |
|             |                     |
+----------------------------------+
```

**Header 표시**: 사용자 이름, 역할 배지, 로그아웃 버튼

**Sidebar Navigation**:
- 리소스 관리 (READER 이상)
- 이력 조회 (READER 이상)
- 버전 관리 (ADMIN only)
- 사용자 관리 (ADMIN only)

---

### ProtectedRoute

**Props**:
- requiredRole?: Role (minimum role required)
- children: ReactNode

**Behavior**:
- useAuthStore에서 인증 상태 확인
- 미인증 → `/login`으로 리다이렉트
- PENDING 사용자 → "승인 대기" 메시지 화면
- 권한 부족 → "접근 권한 없음" 메시지 화면
- 권한 충분 → children 렌더링

---

## 4. Zustand Store: useAuthStore

**State**:
```typescript
interface AuthState {
  accessToken: string | null;
  refreshToken: string | null;
  user: UserInfo | null;
  isAuthenticated: boolean;
}

interface UserInfo {
  id: number;
  email: string;
  name: string;
  role: 'ADMIN' | 'WRITER' | 'READER' | 'PENDING';
}
```

**Actions**:
```typescript
login(email: string, password: string): Promise<void>
register(name: string, email: string, password: string): Promise<void>
logout(): void
refreshToken(): Promise<void>
isAdmin(): boolean
hasWritePermission(): boolean
hasReadPermission(): boolean
```

**Persistence**:
- accessToken, refreshToken → localStorage
- 앱 로드 시 localStorage에서 복원
- 토큰 만료 시 자동 refresh 시도

---

## 5. API Client (Axios)

**Configuration**:
- baseURL: 환경변수 (VITE_API_URL)
- Request interceptor: Authorization header에 Bearer token 추가
- Response interceptor:
  - 401 → refreshToken 시도 → 실패 시 logout + /login 리다이렉트
  - 403 → 에러 표시
  - 429 → "너무 많은 요청" 에러 표시
  - 5xx → 일반 에러 표시

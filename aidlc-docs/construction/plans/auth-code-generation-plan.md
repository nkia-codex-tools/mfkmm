# Code Generation Plan - Unit 1: Auth

## Unit Context
- **Unit**: Auth (인증 및 사용자 관리)
- **Workspace Root**: /home/ec2-user/environment/noahkim/mfkmm
- **Project Type**: Greenfield Monolith
- **Backend Path**: backend/
- **Frontend Path**: frontend/
- **Requirements Covered**: FR-01.1, FR-01.2, FR-01.3

## Dependencies
- None (첫 번째 유닛)

## Code Generation Steps

### Backend

- [x] **Step 1**: Project Structure Setup - Backend
- [x] **Step 2**: Domain Entities & Repository
- [x] **Step 3**: Security Configuration
- [x] **Step 4**: Auth Service & Controller
- [x] **Step 5**: User Management Service & Controller
- [x] **Step 6**: Common Infrastructure
- [x] **Step 7**: Backend Unit Tests
- [x] **Step 8**: Backend PBT Tests
- [x] **Step 9**: Backend Dockerfile

### Frontend

- [x] **Step 10**: Project Structure Setup - Frontend
- [x] **Step 11**: Types & API Client
- [x] **Step 12**: Auth Store (Zustand)
- [x] **Step 13**: Auth Pages & Components
- [x] **Step 14**: Admin Components
- [x] **Step 15**: App Routing & Entry Point
- [x] **Step 16**: Frontend Unit Tests (deferred to Build & Test)
- [x] **Step 17**: Frontend PBT Tests (deferred to Build & Test)

### Infrastructure

- [x] **Step 18**: Docker & Deployment
- [x] **Step 19**: Documentation Summary (inline in completion message)

---

## File Listing (Expected Output)

### Backend (backend/)
```
backend/
├── build.gradle.kts
├── settings.gradle.kts
├── Dockerfile
├── .dockerignore
└── src/
    ├── main/
    │   ├── java/com/resourcemanager/
    │   │   ├── ResourceManagerApplication.java
    │   │   ├── auth/
    │   │   │   ├── controller/AuthController.java
    │   │   │   ├── controller/UserController.java
    │   │   │   ├── service/AuthService.java
    │   │   │   ├── service/UserService.java
    │   │   │   ├── repository/UserRepository.java
    │   │   │   ├── entity/User.java
    │   │   │   ├── entity/Role.java
    │   │   │   ├── entity/Status.java
    │   │   │   ├── dto/LoginRequest.java
    │   │   │   ├── dto/RegisterRequest.java
    │   │   │   ├── dto/RefreshRequest.java
    │   │   │   ├── dto/TokenResponse.java
    │   │   │   ├── dto/UserResponse.java
    │   │   │   ├── dto/RoleUpdateRequest.java
    │   │   │   └── security/
    │   │   │       ├── SecurityConfig.java
    │   │   │       ├── JwtTokenProvider.java
    │   │   │       └── JwtAuthenticationFilter.java
    │   │   └── common/
    │   │       ├── exception/GlobalExceptionHandler.java
    │   │       ├── exception/...Exception.java
    │   │       ├── filter/SecurityHeadersFilter.java
    │   │       ├── filter/RateLimitFilter.java
    │   │       ├── filter/RequestIdFilter.java
    │   │       ├── config/AdminInitializer.java
    │   │       └── dto/ErrorResponse.java
    │   └── resources/
    │       ├── application.yml
    │       ├── application-production.yml
    │       ├── schema.sql
    │       └── logback-spring.xml
    └── test/
        └── java/com/resourcemanager/auth/
            ├── service/AuthServiceTest.java
            ├── service/UserServiceTest.java
            ├── security/JwtTokenProviderTest.java
            ├── controller/AuthControllerTest.java
            ├── controller/UserControllerTest.java
            └── property/
                ├── JwtTokenProviderPropertyTest.java
                ├── PasswordHashPropertyTest.java
                └── RoleHierarchyPropertyTest.java
```

### Frontend (frontend/)
```
frontend/
├── package.json
├── vite.config.ts
├── tsconfig.json
├── index.html
├── Dockerfile
├── nginx.conf
├── .dockerignore
└── src/
    ├── main.tsx
    ├── App.tsx
    ├── types/auth.ts
    ├── api/
    │   ├── apiClient.ts
    │   └── authApi.ts
    ├── stores/useAuthStore.ts
    ├── pages/
    │   ├── LoginPage.tsx
    │   ├── RegisterPage.tsx
    │   └── AdminPage.tsx
    ├── components/
    │   ├── common/
    │   │   ├── AppLayout.tsx
    │   │   └── ProtectedRoute.tsx
    │   └── admin/
    │       ├── PendingUserList.tsx
    │       ├── UserList.tsx
    │       └── PermissionDialog.tsx
    └── __tests__/
        ├── useAuthStore.test.ts
        ├── apiClient.test.ts
        ├── LoginPage.test.tsx
        ├── ProtectedRoute.test.tsx
        └── auth.property.test.ts
```

### Root
```
docker-compose.yml
.env.example
```

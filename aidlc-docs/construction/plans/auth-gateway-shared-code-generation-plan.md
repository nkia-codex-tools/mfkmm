# Code Generation Plan - Auth Service + API Gateway + Shared Library

## Execution Plan

### Phase A: Project Setup & Shared Library
- [x] A1: Root build files (settings.gradle.kts, build.gradle.kts, gradle wrapper)
- [x] A2: Shared Library - Event schemas (Java records)
- [x] A3: Shared Library - DTOs (UserInfo, AuthResponse, ErrorResponse)
- [x] A4: Shared Library - Exception classes (common exceptions)
- [x] A5: Shared Library - build.gradle.kts

### Phase B: Auth Service - Domain Layer
- [x] B1: Domain models (User entity, RefreshToken, LoginHistory, AccountLockEvent, Role enum)
- [x] B2: Domain service (PasswordService - bcrypt hash/verify)

### Phase C: Auth Service - Application Layer
- [x] C1: Inbound ports (AuthUseCase, AccountManagementUseCase)
- [x] C2: Outbound ports (UserRepository, RefreshTokenRepository, LoginHistoryRepository, AccountLockEventRepository, EventPublisherPort)
- [x] C3: Application services (AuthApplicationService - login, logout, refresh, password change/reset, account lock/unlock, root init)

### Phase D: Auth Service - Adapter Layer
- [x] D1: Inbound adapters - REST controllers (AuthController, AccountController)
- [x] D2: Inbound adapters - Request/Response DTOs
- [x] D3: Outbound adapters - MongoDB repositories (MongoUserRepository, MongoRefreshTokenRepository, etc.)
- [x] D4: Outbound adapters - RabbitMQ event publisher (RabbitEventPublisher)
- [x] D5: Outbound adapters - MongoDB document classes

### Phase E: Auth Service - Configuration & Infrastructure
- [x] E1: Spring configuration classes (JwtConfig, RabbitConfig, SecurityConfig)
- [x] E2: JWT utility (RS256 token generation & validation)
- [x] E3: application.yml (default + docker profiles)
- [x] E4: Root admin initializer (ApplicationRunner)
- [x] E5: Dockerfile
- [x] E6: build.gradle.kts

### Phase F: API Gateway
- [x] F1: JWT authentication filter (GatewayFilter)
- [x] F2: Configuration classes (JwtConfig, CorsConfig, RouteConfig)
- [x] F3: application.yml (routes + JWT + CORS)
- [x] F4: Dockerfile
- [x] F5: build.gradle.kts

### Phase G: Docker Compose & Keys
- [x] G1: docker-compose.yml (auth-service, api-gateway, mongodb, rabbitmq)
- [x] G2: Key generation script (generate-keys.sh)
- [x] G3: .gitignore (keys/, build/, etc.)

### Phase H: Tests (PBT + Unit)
- [x] H1: JWT property-based tests (encode/decode round-trip, tamper detection, TTL invariant)
- [x] H2: bcrypt property-based tests (hash/verify round-trip)
- [x] H3: Account lock invariant tests (threshold, blocked login)
- [x] H4: Auth service unit tests (login flow, refresh flow, logout flow)
- [x] H5: API Gateway filter unit tests (valid token, expired token, missing header)

---

## File Summary

| Phase | Files | Count |
|---|---|---|
| A | Root build + Shared library (events, DTOs, exceptions) | ~8 |
| B | Domain models + domain service | ~6 |
| C | Ports + Application service | ~7 |
| D | Controllers + repos + publisher + DTOs + documents | ~12 |
| E | Config + JWT util + yml + Dockerfile + build | ~6 |
| F | Gateway filter + config + yml + Dockerfile + build | ~5 |
| G | docker-compose + script + gitignore | ~3 |
| H | PBT + unit tests | ~5 |
| **Total** | | **~52 files** |

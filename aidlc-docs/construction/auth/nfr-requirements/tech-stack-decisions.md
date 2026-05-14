# Tech Stack Decisions - Unit 1: Auth

## Backend Stack

| Component | Technology | Version | Rationale |
|-----------|-----------|---------|-----------|
| Framework | Spring Boot | 3.3.x | 안정적, 보안 기능 내장, JPA 지원 |
| Language | Java | 21 (LTS) | 최신 LTS, virtual threads 지원 |
| Build Tool | Gradle (Kotlin DSL) | 8.x | 빠른 빌드, 타입 안전 설정 |
| Security | Spring Security | 6.x | JWT 필터, RBAC, CORS, CSRF 설정 |
| JWT | jjwt (io.jsonwebtoken) | 0.12.x | 널리 사용, 안정적, Spring 호환 |
| Password Hashing | Spring Security Crypto (BCrypt) | (Spring Security 내장) | BCrypt 기본 지원 |
| Database | SQLite | 3.x | 경량, 파일 기반, 단일 머신에 적합 |
| ORM | Spring Data JPA + Hibernate | (Spring Boot 내장) | 엔티티 매핑, Repository 패턴 |
| SQLite JDBC | xerial/sqlite-jdbc | 3.x | Java용 SQLite JDBC 드라이버 |
| Validation | Jakarta Bean Validation | 3.x | @Valid, @NotBlank, @Email 등 |
| Logging | SLF4J + Logback | (Spring Boot 내장) | 구조적 JSON 로깅 (logstash-logback-encoder) |
| Testing | JUnit 5 + Mockito | (Spring Boot Test 내장) | 단위/통합 테스트 |
| PBT Testing | jqwik | 1.8.x | JUnit 5 통합, shrinking, seed 재현 |
| Rate Limiting | Bucket4j | 8.x | 인메모리 rate limiting, 경량 |

## Frontend Stack

| Component | Technology | Version | Rationale |
|-----------|-----------|---------|-----------|
| Framework | React | 18.x | 선택된 프론트엔드 프레임워크 |
| Language | TypeScript | 5.x | 타입 안전성 |
| Build Tool | Vite | 5.x | 빠른 HMR, 빌드 |
| Routing | React Router | 6.x | SPA 라우팅 |
| State Management | Zustand | 4.x | 경량, 간단한 API |
| HTTP Client | Axios | 1.x | Interceptor, 에러 핸들링 |
| UI Library | (추후 결정 - Unit 2에서) | - | Auth 페이지는 기본 HTML 폼 |
| Form Handling | React Hook Form | 7.x | 성능 좋은 폼 관리 |
| Testing | Vitest + React Testing Library | (Vite 생태계) | 빠른 테스트 실행 |
| PBT Testing | fast-check | 3.x | Vitest 통합, shrinking 지원 |

## Infrastructure Stack (Unit 1에서 초기 구성)

| Component | Technology | Version | Rationale |
|-----------|-----------|---------|-----------|
| Container Runtime | Docker | 24.x+ | 컨테이너화 |
| Orchestration | Docker Compose | 2.x | 단일 머신 다중 컨테이너 |
| Reverse Proxy | Nginx | 1.25.x | 정적 파일 서빙, 리버스 프록시, HTTPS |
| OS | Amazon Linux 2023 | (EC2) | EC2 기본 AMI |

## Dependency Versions (Lock File Strategy)

| Ecosystem | Lock File | Strategy |
|-----------|-----------|----------|
| Java/Gradle | gradle.lockfile | 의존성 버전 고정 |
| Node/npm | package-lock.json | 정확한 버전 고정 |
| Docker | Dockerfile | 특정 태그 사용 (no `latest`) |

## PBT Framework Configuration

### Backend (jqwik)

```
// build.gradle.kts
testImplementation("net.jqwik:jqwik:1.8.x")
```

- JUnit 5 Platform에서 자동 감지
- @Property 어노테이션으로 PBT 정의
- seed 로깅: jqwik 기본 지원 (실패 시 seed 출력)
- Shrinking: 기본 활성화

### Frontend (fast-check)

```
// package.json
"devDependencies": {
  "fast-check": "^3.x"
}
```

- Vitest에서 직접 사용
- fc.assert(fc.property(...)) 패턴
- seed 로깅: fc.configureGlobal({ seed }) 또는 실패 시 자동 출력
- Shrinking: 기본 활성화

## Security Configuration Summary

| Config | Value | Notes |
|--------|-------|-------|
| JWT Secret Key | 환경 변수 `JWT_SECRET` | 최소 256-bit |
| BCrypt Cost Factor | 10 | ~100ms per hash |
| Access Token TTL | 15분 | Short-lived |
| Refresh Token TTL | 7일 | Long-lived |
| CORS Allowed Origins | 환경 변수 `CORS_ORIGINS` | Production: 특정 도메인만 |
| Rate Limit Storage | In-memory (Bucket4j) | 단일 인스턴스에 적합 |
| Admin Email | 환경 변수 `ADMIN_EMAIL` | 초기 관리자 |
| Admin Password | 환경 변수 `ADMIN_PASSWORD` | 초기 관리자 |

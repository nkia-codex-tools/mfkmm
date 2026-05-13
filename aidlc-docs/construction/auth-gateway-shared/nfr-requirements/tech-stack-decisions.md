# Tech Stack Decisions - Auth Service + API Gateway + Shared Library

## Backend Framework
| Decision | Choice | Rationale |
|---|---|---|
| Language | Java 17+ | 팀 기술 스택 |
| Framework | Spring Boot 3.x | Production-ready, 풍부한 생태계 |
| Build Tool | Gradle (Kotlin DSL) | 모던 빌드, 멀티모듈 지원 |

## Auth Service Stack
| Component | Technology | Purpose |
|---|---|---|
| JWT Library | java-jwt (auth0) 또는 jjwt (io.jsonwebtoken) | RS256 JWT 서명/검증 |
| Password Hashing | Spring Security Crypto (BCryptPasswordEncoder) | bcrypt cost=10 |
| Database | MongoDB | Document store for users, tokens, history |
| MongoDB Driver | Spring Data MongoDB | Repository pattern |
| Messaging | Spring AMQP (RabbitMQ) | Event publishing (at-least-once) |

## API Gateway Stack
| Component | Technology | Purpose |
|---|---|---|
| Gateway | Spring Cloud Gateway | Reactive routing, filter chain |
| JWT Validation | Same JWT library as Auth | Public key로 토큰 검증 |
| CORS | Spring Cloud Gateway CORS filter | Frontend 도메인 허용 |

## Shared Library Stack
| Component | Technology | Purpose |
|---|---|---|
| Event DTOs | Plain Java records | 이벤트 스키마 정의 |
| Common DTOs | Plain Java records | UserInfo, AuthResponse, ErrorResponse |
| Serialization | Jackson | JSON 직렬화/역직렬화 |

## Testing Stack
| Component | Technology | Purpose |
|---|---|---|
| Unit Testing | JUnit 5 | 기본 테스트 프레임워크 |
| PBT | jqwik | Property-Based Testing |
| Mocking | Mockito | 단위 테스트 mock |
| Integration Test | Testcontainers | MongoDB, RabbitMQ 통합 테스트 |
| API Test | Spring WebFlux Test / MockMvc | 엔드포인트 테스트 |

## Infrastructure Stack
| Component | Technology | Purpose |
|---|---|---|
| Containerization | Docker | 서비스 패키징 |
| Orchestration | Docker Compose | 로컬 개발 + 배포 |
| Health Check | Spring Boot Actuator | `/actuator/health` |
| Restart Policy | Docker restart: on-failure | 자동 복구 |

## Key Configuration
| Config | Value | Note |
|---|---|---|
| JWT Algorithm | RS256 | 비대칭키 |
| JWT Access TTL | 15min | Access Token 만료 |
| JWT Refresh TTL | 7days | Refresh Token 만료 |
| bcrypt cost | 10 | 비밀번호 해싱 |
| RabbitMQ delivery | at-least-once | publisher confirms + manual ack |
| Docker healthcheck interval | 30s | 헬스체크 주기 |
| Docker restart max retries | 5 | 최대 재시작 횟수 |

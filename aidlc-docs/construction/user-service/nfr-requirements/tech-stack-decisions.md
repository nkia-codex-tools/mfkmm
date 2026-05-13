# Tech Stack Decisions - User Service

## Core Technology

| Layer | Technology | Version | Rationale |
|---|---|---|---|
| Runtime | Java 17+ | LTS | Spring Boot 3.x 요구사항 |
| Framework | Spring Boot 3.x | Latest stable | Hexagonal 구조, 자동 설정, 생산성 |
| Database | MongoDB 7.x | Latest | 유연한 사용자 스키마, 프로젝트 공통 결정 |
| DB Driver | Spring Data MongoDB | Boot 호환 | Repository 추상화, 페이지네이션 지원 |
| Messaging | RabbitMQ 3.x | Management | 이벤트 기반 통신 (At-least-once) |
| MQ Client | Spring AMQP | Boot 호환 | RabbitMQ 통합, 재시도/DLQ 지원 |
| Validation | Jakarta Validation | Boot 내장 | @NotBlank, @Pattern 등 선언적 검증 |
| Build | Maven | 3.9+ | 프로젝트 공통 빌드 도구 |

---

## Testing Technology

| Category | Technology | Rationale |
|---|---|---|
| Unit Test | JUnit 5 | 표준 Java 테스트 프레임워크 |
| PBT | jqwik | PBT-09: Java PBT 프레임워크 (JUnit 5 통합) |
| Mocking | Mockito | Port/Adapter 경계 모킹 |
| Integration Test | Spring Boot Test + Testcontainers | MongoDB/RabbitMQ 통합 테스트 |

---

## Dependencies (pom.xml)

```xml
<!-- Core -->
spring-boot-starter-web
spring-boot-starter-data-mongodb
spring-boot-starter-amqp
spring-boot-starter-validation

<!-- Shared Library -->
mkfmm-shared (project dependency)

<!-- Test -->
spring-boot-starter-test
net.jqwik:jqwik
org.testcontainers:mongodb
org.testcontainers:rabbitmq
```

---

## Configuration

| Property | Value | Purpose |
|---|---|---|
| server.port | 8082 | User Service 포트 |
| spring.data.mongodb.uri | mongodb://mongodb:27017/user-db | 사용자 데이터 DB |
| spring.rabbitmq.host | rabbitmq | 메시지 큐 호스트 |
| spring.rabbitmq.listener.simple.retry.max-attempts | 3 | 재시도 최대 횟수 |
| spring.rabbitmq.listener.simple.retry.initial-interval | 1000 | 초기 재시도 간격 (ms) |
| spring.rabbitmq.listener.simple.retry.multiplier | 2.0 | Exponential backoff 배수 |
| app.audit-log.retention-days | 730 | 감사 로그 보존 기간 (2년) |

---

## PBT-09 Compliance

| Criterion | Status |
|---|---|
| PBT framework selected | ✅ jqwik |
| Framework in dependencies | ✅ pom.xml |
| Custom generators support | ✅ jqwik @Provide |
| Automatic shrinking | ✅ jqwik built-in |
| Seed-based reproducibility | ✅ jqwik @Seed |
| Test runner integration | ✅ JUnit 5 Platform |

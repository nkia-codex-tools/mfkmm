# Tech Stack Decisions - Resource Service

## Core Technology

| Layer | Technology | Rationale |
|---|---|---|
| Runtime | Java 17+ | 프로젝트 공통 |
| Framework | Spring Boot 3.x | Hexagonal, 자동 설정 |
| Database | MongoDB 7.x (resource-db) | 유연한 리소스 스키마 |
| DB Driver | Spring Data MongoDB | Repository, 페이지네이션, text index |
| Messaging | RabbitMQ 3.x | 이벤트 기반 이력 전송 |
| MQ Client | Spring AMQP | 재시도, DLQ 지원 |
| Validation | Jakarta Validation | 선언적 입력 검증 |
| Build | Gradle (Kotlin DSL) | 프로젝트 공통 |

## Testing

| Category | Technology | Rationale |
|---|---|---|
| Unit Test | JUnit 5 | 표준 |
| PBT | jqwik | Levenshtein 속성 검증, 도메인 invariant |
| Mocking | Mockito | Port 경계 모킹 |
| Integration | Testcontainers (MongoDB) | 실제 DB 통합 테스트 |

## Dependencies (build.gradle.kts)

```kotlin
implementation(project(":shared"))
implementation("org.springframework.boot:spring-boot-starter-web")
implementation("org.springframework.boot:spring-boot-starter-data-mongodb")
implementation("org.springframework.boot:spring-boot-starter-amqp")
implementation("org.springframework.boot:spring-boot-starter-validation")
implementation("org.springframework.boot:spring-boot-starter-actuator")

testImplementation("org.springframework.boot:spring-boot-starter-test")
testImplementation("net.jqwik:jqwik:1.8.4")
testImplementation("org.testcontainers:mongodb:1.19.7")
```

## Configuration

| Property | Value |
|---|---|
| server.port | 8084 |
| spring.data.mongodb.uri | mongodb://mongodb:27017/resource-db |
| spring.rabbitmq.host | rabbitmq |
| app.similarity.threshold | 3 |
| app.similarity.max-results | 5 |
| app.hard-delete.retention-days | 30 |

## PBT-09 Compliance

| Criterion | Status |
|---|---|
| PBT framework selected | ✅ jqwik |
| Custom generators | ✅ Resource, ResourceKey generators |
| Shrinking | ✅ built-in |
| Seed reproducibility | ✅ @Seed |
| Oracle testing | ✅ Levenshtein oracle comparison |

# Tech Stack Decisions - DataIO Service + Deploy Service + Frontend

## DataIO Service Stack
| Component | Technology | Purpose |
|---|---|---|
| Language | Java 17+ | 팀 기술 스택 통일 |
| Framework | Spring Boot 3.x | Dev 1과 동일 |
| Build Tool | Gradle (Kotlin DSL) | 모노레포 멀티모듈 |
| Excel Processing | Apache POI 5.x | .xlsx 읽기/쓰기 |
| TSV Processing | 자체 구현 (BufferedReader/Writer) | 간단한 탭 구분 파싱 |
| JSON Processing | Jackson ObjectMapper | JSON 배열 직렬화/역직렬화 |
| Database | MongoDB (dataio-db) | ImportJob 상태 저장 |
| MongoDB Driver | Spring Data MongoDB | Repository pattern |
| Messaging | Spring AMQP (RabbitMQ) | ImportCompleted, ExportCompleted 이벤트 |
| REST Client | Spring WebClient (reactive) | Resource Service 호출 |
| File Upload | Spring Multipart (5MB limit) | 파일 업로드 처리 |

## Deploy Service Stack
| Component | Technology | Purpose |
|---|---|---|
| Language | Java 17+ | 팀 기술 스택 통일 |
| Framework | Spring Boot 3.x | Dev 1과 동일 |
| Build Tool | Gradle (Kotlin DSL) | 모노레포 멀티모듈 |
| Database | MongoDB (deploy-db) | Deployment 메타데이터 저장 |
| MongoDB Driver | Spring Data MongoDB | Repository pattern |
| Messaging | Spring AMQP (RabbitMQ) | DeployCompleted 이벤트 |
| REST Client | Spring WebClient | DataIO Service exportAll 호출 |
| File Storage | Local filesystem (Docker volume) | 배포 파일 영구 저장 |

## Frontend Stack
| Component | Technology | Purpose |
|---|---|---|
| Language | TypeScript 5.x | 타입 안전성 |
| Framework | React 18.x | UI 라이브러리 |
| Build Tool | Vite | 빌드/번들링 (fast HMR) |
| Styling | Tailwind CSS 3.x | 유틸리티 기반 CSS |
| UI Components | Headless UI | 접근성 지원 컴포넌트 (Modal, Menu, Dialog) |
| State Management | React Context + useReducer | 전역 상태 (AuthContext) |
| HTTP Client | Axios | API 호출 + interceptor (token refresh) |
| Routing | React Router v6 | 클라이언트 사이드 라우팅 |
| Form Handling | React Hook Form | 폼 관리 + 유효성 검증 |
| File Download | Blob + URL.createObjectURL | EXPORT/Deploy 파일 다운로드 |
| Code Splitting | React.lazy + Suspense | Route 기반 lazy loading |

## Testing Stack
| Component | Technology | Purpose |
|---|---|---|
| Backend Unit Test | JUnit 5 | 기본 테스트 프레임워크 |
| Backend PBT | jqwik | Property-Based Testing |
| Backend Mocking | Mockito | 서비스 간 의존성 mock |
| Backend Integration | Testcontainers | MongoDB 통합 테스트 |
| Frontend Unit Test | Vitest | Vite 기반 빠른 테스트 |
| Frontend PBT | fast-check | Property-Based Testing |
| Frontend Component | React Testing Library | 컴포넌트 렌더링 테스트 |
| Frontend E2E | (미적용 — MVP) | 향후 확장 가능 |

## Infrastructure Stack
| Component | Technology | Purpose |
|---|---|---|
| DataIO Container | Docker (eclipse-temurin:17-jre-alpine) | Dev 1과 동일 베이스 이미지 |
| Deploy Container | Docker (eclipse-temurin:17-jre-alpine) | Dev 1과 동일 베이스 이미지 |
| Frontend Container | Docker (nginx:alpine) | 정적 파일 서빙 |
| Deploy Volume | Docker named volume (deploy-data) | /data/deployments 마운트 |
| Health Check | Spring Boot Actuator | Backend 서비스 헬스체크 |
| Frontend Health | nginx stub_status 또는 기본 200 응답 | Frontend 헬스체크 |

## Key Configuration
| Config | Value | Note |
|---|---|---|
| Multipart max size | 5MB | Spring: spring.servlet.multipart.max-file-size |
| Multipart max request | 6MB | 약간 여유 (메타데이터 포함) |
| WebClient timeout | 30s | Resource Service 호출 타임아웃 |
| Deploy file path | /data/deployments/ | Docker volume mount point |
| Frontend dev port | 5173 | Vite 기본 개발 서버 포트 |
| Frontend prod port | 3000 | nginx 서빙 포트 |
| Nginx gzip | enabled | 정적 자산 압축 전송 |
| React Router | basename=/  | SPA fallback (nginx try_files) |

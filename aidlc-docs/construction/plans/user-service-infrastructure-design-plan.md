# Infrastructure Design Plan - User Service

## Unit Context
- **Unit**: User Service
- **Deployment**: Docker container (Docker Compose 내)
- **Database**: MongoDB (공유 인스턴스, 별도 DB: user-db)
- **Messaging**: RabbitMQ (공유 인스턴스)

## Infrastructure Questions

배포 환경은 이미 Docker Compose로 결정되어 있습니다. 추가 확인 사항:

---

## Question 1
MongoDB 인스턴스 구성은?

A) 단일 MongoDB 인스턴스, 서비스별 별도 DB (user-db, resource-db 등)
B) 서비스별 별도 MongoDB 컨테이너 (완전 격리)
C) 단일 MongoDB 인스턴스, 단일 DB에 collection으로만 분리
X) Other (please describe after [Answer]: tag below)

[Answer]: A

## Question 2
Docker 컨테이너 리소스 제한을 설정하시겠습니까?

A) 설정하지 않음 (개발/사내 환경, 리소스 여유)
B) 기본 제한 설정 (CPU: 0.5, Memory: 512MB per service)
C) 서비스별 차등 제한 (중요 서비스에 더 많은 리소스)
X) Other (please describe after [Answer]: tag below)

[Answer]: A

---

## Infrastructure Design Execution Plan

- [x] Step 1: Infrastructure Design 문서 생성 (Docker, MongoDB, RabbitMQ 매핑)
- [x] Step 2: Deployment Architecture 문서 생성 (Docker Compose 구성)

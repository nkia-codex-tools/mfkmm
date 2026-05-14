# Infrastructure Design - Unit 1: Auth

## Deployment Target
- **Platform**: AWS EC2 (단일 인스턴스)
- **Approach**: Docker Compose (모든 서비스를 단일 머신에서 운영)
- **OS**: Amazon Linux 2023

## Infrastructure Components

### 1. Compute

| Service | Technology | Role |
|---------|-----------|------|
| Backend | Docker Container (OpenJDK 21 slim) | Spring Boot 애플리케이션 |
| Frontend | Docker Container (Nginx Alpine) | React 빌드 결과 정적 파일 서빙 |

### 2. Storage

| Service | Technology | Role |
|---------|-----------|------|
| Database | SQLite (파일 기반) | 사용자 데이터 영속성 |
| Volume | Docker Named Volume | SQLite DB 파일 영속 저장 |

### 3. Networking

| Service | Technology | Role |
|---------|-----------|------|
| Reverse Proxy | Nginx (Frontend 컨테이너 내장) | 정적 파일 서빙 + API 리버스 프록시 |
| Port Mapping | Docker Compose ports | Host 80 → Nginx, Host 8080 → Backend (내부만) |

### 4. Logging

| Service | Technology | Role |
|---------|-----------|------|
| Application Logs | Docker stdout/stderr | 컨테이너 로그 출력 |
| Log Persistence | Docker logging driver (json-file) | 로그 파일 로테이션 |
| Log Retention | logrotate 또는 Docker max-size | 90일 보존 |

## Service Mapping

```
+------------------------------------------------------+
|               EC2 Instance (t3.small)                 |
|                                                      |
|  +------------------------------------------------+  |
|  |            Docker Compose Network              |  |
|  |                                                |  |
|  |  +------------------+   +------------------+   |  |
|  |  |  frontend        |   |  backend         |   |  |
|  |  |  (nginx:alpine)  |   |  (openjdk:21)    |   |  |
|  |  |                  |   |                   |   |  |
|  |  |  Port: 80 (ext)  |   |  Port: 8080 (int)|   |  |
|  |  |                  |   |                   |   |  |
|  |  |  - Static files  |   |  - Spring Boot   |   |  |
|  |  |  - /api/* proxy  |   |  - JWT Auth      |   |  |
|  |  |    → backend:8080|   |  - SQLite        |   |  |
|  |  +------------------+   +--------+---------+   |  |
|  |                                  |             |  |
|  +----------------------------------|-------------+  |
|                                     |                |
|  +----------------------------------v-------------+  |
|  |  Docker Volume: resource-manager-data          |  |
|  |  → /data/resource-manager.db (SQLite file)     |  |
|  +------------------------------------------------+  |
|                                                      |
|  +------------------------------------------------+  |
|  |  Docker Volume: app-logs                       |  |
|  |  → /var/log/resource-manager/ (log files)      |  |
|  +------------------------------------------------+  |
+------------------------------------------------------+
```

## Environment Variables

| Variable | Container | Description | Example |
|----------|-----------|-------------|---------|
| JWT_SECRET | backend | JWT 서명 키 (최소 256-bit) | `a-very-long-secret-key-at-least-32-characters` |
| ADMIN_EMAIL | backend | 초기 관리자 이메일 | `admin@company.com` |
| ADMIN_PASSWORD | backend | 초기 관리자 비밀번호 | `SecureP@ss123!` |
| CORS_ORIGINS | backend | 허용된 CORS origin | `http://localhost,http://your-domain.com` |
| SPRING_PROFILES_ACTIVE | backend | 활성 프로파일 | `production` |
| DB_PATH | backend | SQLite 파일 경로 | `/data/resource-manager.db` |

## Network Configuration

| Rule | Direction | Port | Source | Target |
|------|-----------|------|--------|--------|
| HTTP | Inbound | 80 | 0.0.0.0/0 (또는 사내 IP 대역) | Nginx |
| API (내부) | Internal | 8080 | frontend container | backend container |
| SSH | Inbound | 22 | 관리자 IP만 | EC2 |

## Resource Sizing

| Resource | Spec | Rationale |
|----------|------|-----------|
| EC2 Instance | t3.small (2 vCPU, 2GB RAM) | 50명 이하 사내 서비스에 충분 |
| EBS Volume | 20GB gp3 | SQLite + 로그 + Docker images |
| Backend Container | 512MB memory limit | Spring Boot + SQLite |
| Frontend Container | 128MB memory limit | Nginx 정적 서빙 |

## Backup Strategy

| Target | Method | Frequency |
|--------|--------|-----------|
| SQLite DB | Docker volume → 주기적 파일 복사 (cp to backup dir) | 일 1회 |
| Application Logs | Docker json-file driver + logrotate | 자동 로테이션 |

## Security Hardening

| Item | Implementation |
|------|---------------|
| Docker base image | 공식 slim/alpine 이미지, 특정 태그 사용 (no `latest`) |
| Non-root user | Dockerfile에서 non-root 사용자로 실행 |
| Read-only filesystem | 가능한 컨테이너는 read-only 마운트 |
| EC2 Security Group | 80(HTTP), 22(SSH) 만 오픈, SSH는 관리자 IP 제한 |
| Docker network | Internal network (backend는 외부 직접 접근 불가) |

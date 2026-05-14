# 리소스 관리 시스템 (Resource Manager)

사내 Google Sheets로 관리하던 리소스(기능, 메뉴, 리소스키)를 체계적으로 관리하는 웹 애플리케이션.

## 주요 기능

- **3개 리소스 탭**: 기능(Functions), 메뉴(Menus), 리소스 키(Message Resource)
- **인라인 편집**: AG Grid 기반 셀 클릭 즉시 편집
- **드롭다운 선택**: 기능(ACTION), 유형, TRUE/FALSE 등 미리 정의된 옵션
- **자동 계산 필드**: 기능 ID 리소스키 자동생성, full_resource_key, 중복/대문자 검증, 개수 집계
- **드래그 앤 드롭**: 행 순서 변경
- **TSV Import/Export**: 파일 업로드 즉시 등록, 전체/선택 내보내기
- **변경 이력**: 모든 수정/추가/삭제 자동 기록, 필터 조회
- **버전 관리**: 관리자 버전 태깅, 스냅샷 비교, 롤백
- **JWT 인증**: 회원가입 → 관리자 승인 → 역할별 접근 제어 (ADMIN/WRITER/READER)

## 기술 스택

| Layer | Technology |
|-------|-----------|
| Backend | Java 21, Spring Boot 3.3, Spring Security, JPA |
| Frontend | React 18, TypeScript, AG Grid Community, Zustand |
| Database | SQLite (WAL mode) |
| Deployment | Docker Compose, Nginx |

## 설치 및 실행

### 사전 요구사항

- Docker 24.x+
- Docker Compose v2.x+

### 빠른 시작 (Docker)

```bash
# 1. 프로젝트 클론
git clone <repository-url>
cd mfkmm

# 2. 환경 변수 설정
cp .env.example .env
# .env 파일을 편집하여 아래 값을 변경:
#   JWT_SECRET=최소32자이상의랜덤문자열
#   ADMIN_EMAIL=admin@company.com
#   ADMIN_PASSWORD=원하는비밀번호
#   CORS_ORIGINS=http://서버IP또는도메인

# 3. Docker Compose 실행
docker compose up -d --build

# 4. 브라우저에서 접속
# http://서버IP (포트 80)
```

### EC2 환경에서 처음부터 설치

```bash
# Docker 설치 (Amazon Linux 2023)
sudo yum install -y docker
sudo systemctl start docker && sudo systemctl enable docker
sudo usermod -aG docker $USER

# Docker Compose 플러그인 설치
sudo mkdir -p /usr/local/lib/docker/cli-plugins
sudo curl -SL "https://github.com/docker/compose/releases/download/v2.29.7/docker-compose-linux-x86_64" \
    -o /usr/local/lib/docker/cli-plugins/docker-compose
sudo chmod +x /usr/local/lib/docker/cli-plugins/docker-compose

# 프로젝트 빌드 및 실행
cp .env.example .env
# .env 편집 후:
sudo docker compose up -d --build
```

### 환경 변수 (.env)

| 변수 | 설명 | 예시 |
|------|------|------|
| `JWT_SECRET` | JWT 서명 키 (최소 32자) | `my-super-secret-key-at-least-32-chars` |
| `ADMIN_EMAIL` | 초기 관리자 이메일 | `admin@company.com` |
| `ADMIN_PASSWORD` | 초기 관리자 비밀번호 | `Admin1234!` |
| `CORS_ORIGINS` | 허용 origin | `http://98.81.253.40` |

## 관리 명령어

```bash
# 상태 확인
sudo docker compose ps

# 로그 확인
sudo docker compose logs -f
sudo docker compose logs -f backend

# 중지
sudo docker compose down

# 재시작
sudo docker compose restart

# 재빌드 후 실행
sudo docker compose up -d --build

# DB 초기화 (데이터 삭제)
sudo docker compose down
sudo docker volume rm mfkmm_db-data
sudo docker compose up -d --build
```

## 프로젝트 구조

```
mfkmm/
├── backend/                    # Spring Boot (Java 21)
│   ├── src/main/java/com/resourcemanager/
│   │   ├── auth/              # 인증, JWT, 사용자 관리
│   │   ├── resource/          # 리소스 CRUD (기능/메뉴/리소스키)
│   │   ├── transfer/          # TSV Import/Export
│   │   ├── history/           # 변경 이력
│   │   ├── version/           # 버전 태깅 및 롤백
│   │   └── common/            # 필터, 예외, 설정
│   ├── Dockerfile
│   └── build.gradle.kts
├── frontend/                   # React + TypeScript
│   ├── src/
│   │   ├── pages/             # 페이지 컴포넌트
│   │   ├── components/        # UI 컴포넌트
│   │   ├── stores/            # Zustand 상태
│   │   ├── api/               # API 서비스
│   │   └── types/             # TypeScript 타입
│   ├── Dockerfile
│   └── nginx.conf
├── docker-compose.yml
├── .env.example
└── README.md
```

## 사용자 역할

| 역할 | 권한 |
|------|------|
| ADMIN | 모든 기능 + 사용자 관리 + 버전 롤백 |
| WRITER | 리소스 조회/편집/추가/삭제 + Import/Export |
| READER | 리소스 조회 + Export만 가능 |
| PENDING | 가입 후 관리자 승인 대기 (접근 불가) |

## TSV Import 형식

**기능/메뉴**: `#`으로 시작하는 줄은 헤더(무시), 나머지가 데이터 행.

**리소스 키**: 숫자로 시작하는 줄만 데이터 행으로 인식.

# FeeL Backend Server

전북대학교 공과대학 FeeL 학생회 백엔드 서버

## 기술 스택

- Java 17
- Spring Boot 3.2.1
- Spring Data JPA
- H2 Database (개발)
- MySQL 8.0 (배포)
- Docker & Docker Compose

## 개발 환경 실행

### 1. H2 데이터베이스 사용 (로컬 개발)

```bash
# Maven을 사용한 실행
./mvnw spring-boot:run

# 또는 IDE에서 BackendApplication.java 실행
```

서버 실행 후:
- API: http://localhost:8080/api/notices
- H2 Console: http://localhost:8080/h2-console
  - JDBC URL: `jdbc:h2:mem:feeldb`
  - Username: `sa`
  - Password: (비워두기)

### 2. Docker를 사용한 실행

```bash
# 환경 변수 설정
cp .env.example .env
# .env 파일 수정하여 DB_USERNAME, DB_PASSWORD 설정

# Docker Compose로 실행
docker-compose up -d

# 로그 확인
docker-compose logs -f backend

# 종료
docker-compose down

# 볼륨까지 삭제
docker-compose down -v
```

## API 엔드포인트

### 공지사항 API

- `POST /api/notices` - 공지사항 생성
- `GET /api/notices` - 전체 공지사항 조회 (페이징)
- `GET /api/notices/{id}` - 특정 공지사항 조회
- `PUT /api/notices/{id}` - 공지사항 수정
- `DELETE /api/notices/{id}` - 공지사항 삭제
- `GET /api/notices/pinned` - 고정 공지사항 조회
- `GET /api/notices/category/{category}` - 카테고리별 조회
- `GET /api/notices/search?keyword=검색어` - 검색

### 요청 예시

```bash
# 공지사항 생성
curl -X POST http://localhost:8080/api/notices \
  -H "Content-Type: application/json" \
  -d '{
    "title": "공지사항 제목",
    "content": "공지사항 내용",
    "author": "관리자",
    "isPinned": false,
    "category": "공지"
  }'

# 전체 조회
curl http://localhost:8080/api/notices?page=0&size=10

# 특정 공지사항 조회
curl http://localhost:8080/api/notices/1
```

## 배포

### Docker를 이용한 배포

```bash
# 프로덕션 환경으로 실행
docker-compose up -d

# MySQL 사용을 위해 application-prod.properties 수정 필요
```

### 프로필 설정

- **개발**: `application.properties` (H2)
- **배포**: `application-prod.properties` (MySQL/PostgreSQL)

환경 변수로 프로필 전환:
```bash
export SPRING_PROFILES_ACTIVE=prod
```

## 데이터베이스 마이그레이션

개발 환경에서 H2를 사용하다가 MySQL로 전환 시:

1. `application-prod.properties`에서 MySQL 설정 주석 해제
2. Docker Compose로 MySQL 컨테이너 실행
3. `spring.jpa.hibernate.ddl-auto=update`로 자동 스키마 생성

## 프로젝트 구조

```
src/main/java/com/feel/backend/
├── BackendApplication.java
├── config/
│   └── WebConfig.java (CORS 설정)
├── controller/
│   └── NoticeController.java
├── dto/
│   ├── NoticeRequestDto.java
│   └── NoticeResponseDto.java
├── entity/
│   └── Notice.java
├── repository/
│   └── NoticeRepository.java
└── service/
    └── NoticeService.java
```

## CORS 설정

다음 Origin에서 접근 허용:
- http://localhost:3000 (React 개발 서버)
- https://m-se0k.github.io (배포된 프론트엔드)

## 📚 문서 및 가이드

자세한 설정 가이드와 문제 해결 방법은 [`md/`](./md/) 디렉토리를 참고하세요:

### 빠른 시작
- **[md/QUICK_START.md](./md/QUICK_START.md)** - 빠른 시작 가이드
- **[md/SETUP_COMPLETE.md](./md/SETUP_COMPLETE.md)** - 환경 구축 완료 보고서

### 문제 해결
- **[md/ERROR_REPORT.md](./md/ERROR_REPORT.md)** - 포트 충돌 에러 해결
- **[md/CURL_ERROR_ANALYSIS.md](./md/CURL_ERROR_ANALYSIS.md)** - curl 명령어 에러 해결
- **[md/H2_ERROR_500_ANALYSIS.md](./md/H2_ERROR_500_ANALYSIS.md)** - 500 에러 및 DB 문제 해결

### 전체 문서 목록
- **[md/README.md](./md/README.md)** - 문서 디렉토리 가이드

## 라이선스

MIT

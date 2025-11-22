# FeeL Backend Server

전북대학교 공과대학 FeeL 학생회 백엔드 서버 - 공지사항 및 갤러리 관리 시스템

## 기술 스택

- Java 17
- Spring Boot 3.2.1
- Spring Data JPA with Hibernate
- H2 Database (개발)
- MySQL 8.0 (배포)
- Docker & Docker Compose
- Nginx (Reverse Proxy)
- Certbot (SSL/TLS)
- Lombok

## 개발 환경 실행

### 1. H2 데이터베이스 사용 (로컬 개발)

```bash
# Maven을 사용한 실행
./mvnw spring-boot:run

# 또는 IDE에서 BackendApplication.java 실행
```

서버 실행 후:
- API Base URL: http://localhost:8080
  - 공지사항: http://localhost:8080/api/notices
  - 갤러리: http://localhost:8080/api/gallery
- H2 Console: http://localhost:8080/h2-console
  - JDBC URL: `jdbc:h2:file:./data/feeldb`
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

# 개별 서비스 상태 확인
docker-compose ps

# 종료
docker-compose down

# 볼륨까지 삭제
docker-compose down -v
```

**배포 환경 구성:**
- **Nginx**: Reverse Proxy (포트 80, 443)
- **Backend**: Spring Boot 애플리케이션 (내부 포트 8080)
- **MySQL**: 데이터베이스 (포트 3307 → 3306)
- **Certbot**: SSL/TLS 인증서 자동 갱신

## API 엔드포인트

### 공지사항 API (`/api/notices`)

| 메서드 | 엔드포인트 | 설명 |
|--------|-----------|------|
| POST | `/api/notices` | 공지사항 생성 (이미지 업로드 선택) |
| POST | `/api/notices/batch` | 여러 공지사항 일괄 생성 |
| GET | `/api/notices` | 전체 공지사항 조회 (페이징, 카테고리 필터) |
| GET | `/api/notices/{id}` | 특정 공지사항 조회 (조회수 증가) |
| GET | `/api/notices/pinned` | 고정 공지사항 조회 |
| GET | `/api/notices/category/{category}` | 카테고리별 조회 |
| GET | `/api/notices/search?keyword=검색어` | 검색 (카테고리 필터 지원) |
| PUT | `/api/notices/{id}` | 공지사항 수정 (이미지 업로드 선택) |
| DELETE | `/api/notices/{id}` | 공지사항 삭제 |

### 갤러리 API (`/api/gallery`)

| 메서드 | 엔드포인트 | 설명 |
|--------|-----------|------|
| POST | `/api/gallery` | 갤러리 생성 (이미지 업로드 필수) |
| GET | `/api/gallery` | 전체 갤러리 조회 (페이징, 카테고리 필터) |
| GET | `/api/gallery/recent` | 최근 갤러리 조회 (최대 10개) |
| GET | `/api/gallery/{id}` | 특정 갤러리 조회 (조회수 증가) |
| GET | `/api/gallery/search?keyword=검색어` | 검색 (카테고리 필터 지원) |
| PUT | `/api/gallery/{id}` | 갤러리 수정 (이미지 업로드 선택) |
| DELETE | `/api/gallery/{id}` | 갤러리 삭제 |

## curl 요청 예시

### 공지사항 (Notice) 예시

#### 1. 이미지 없는 공지사항 생성 (JSON)
```bash
curl -X POST http://https://jbnu-coe.github.io/api/notices \
  -H "Content-Type: application/json" \
  -d '{
    "title": "2024 FeeL 정기총회 안내",
    "content": "2024년도 정기총회를 다음과 같이 개최합니다.\n일시: 2024년 3월 15일\n장소: 공대 7호관 강당",
    "author": "학생회장",
    "isPinned": true,
    "category": "학과소식"
  }'
```

#### 2. 이미지 포함 공지사항 생성 (Multipart Form)
```bash
curl -X POST https://lovesade.duckdns.org/api/notices \
  -F "title=봄 축제 포스터" \
  -F "content=2024 FeeL 봄 축제가 열립니다!" \
  -F "author=홍보부장" \
  -F "isPinned=false" \
  -F "category=학과소식" \
  -F "image=@C:\Users\scheoleon\workspace\JBNU-COE.github.io\public\feel_logo.png"
```

#### 3. 전체 공지사항 조회 (페이징)
```bash
curl "http://localhost:8080/api/notices?page=0&size=10"
```

#### 4. 카테고리별 조회
```bash
curl "http://localhost:8080/api/notices?page=0&size=10&category=공지"
```

#### 5. 고정 공지사항 조회
```bash
curl http://localhost:8080/api/notices/pinned
```

#### 6. 특정 공지사항 조회
```bash
curl http://localhost:8080/api/notices/1
```

#### 7. 공지사항 검색
```bash
curl "http://localhost:8080/api/notices/search?keyword=축제&page=0&size=10"
```

#### 8. 카테고리 필터를 적용한 검색
```bash
curl "http://localhost:8080/api/notices/search?keyword=축제&category=행사&page=0&size=10"
```

#### 9. 공지사항 수정 (이미지 포함)
```bash
curl -X PUT https://lovesade.duckdns.org/api/notices/ \
  -F "title=수" \
  -F "content=수정된 내용" \
  -F "author=관리자" \
  -F "isPinned=true" \
  -F "category=학사공지" \
  -F "image=C:\Users\scheoleon\workspace\JBNU-COE.github.io\public\feel_logo.png"
```

#### 10. 공지사항 삭제
```bash
curl -X DELETE http://localhost:8080/api/notices/1
```

### 갤러리 (Gallery) 예시

#### 1. 갤러리 생성 (이미지 필수)
```bash
curl -X POST http://localhost:8080/api/gallery \
  -F "title=2024 FeeL MT 사진" \
  -F "description=2024년 봄 MT에서 찍은 단체 사진입니다." \
  -F "photographer=김철수" \
  -F "category=행사" \
  -F "image=@/path/to/mt_photo.jpg"
```

#### 2. 전체 갤러리 조회 (페이징)
```bash
curl "http://localhost:8080/api/gallery?page=0&size=12"
```

#### 3. 카테고리별 갤러리 조회
```bash
curl "http://localhost:8080/api/gallery?page=0&size=12&category=행사"
```

#### 4. 최근 갤러리 조회 (최대 10개)
```bash
curl http://localhost:8080/api/gallery/recent
```

#### 5. 특정 갤러리 조회
```bash
curl http://localhost:8080/api/gallery/1
```

#### 6. 갤러리 검색
```bash
curl "http://localhost:8080/api/gallery/search?keyword=MT&page=0&size=12"
```

#### 7. 카테고리 필터를 적용한 검색
```bash
curl "http://localhost:8080/api/gallery/search?keyword=축제&category=행사&page=0&size=12"
```

#### 8. 갤러리 수정 (이미지 변경)
```bash
curl -X PUT http://localhost:8080/api/gallery/1 \
  -F "title=수정된 제목" \
  -F "description=수정된 설명" \
  -F "photographer=홍길동" \
  -F "category=일상" \
  -F "image=@/path/to/new_photo.jpg"
```

#### 9. 갤러리 수정 (이미지 제외)
```bash
curl -X PUT http://localhost:8080/api/gallery/1 \
  -F "title=수정된 제목만" \
  -F "description=설명만 수정" \
  -F "photographer=작성자" \
  -F "category=일상"
```

#### 10. 갤러리 삭제
```bash
curl -X DELETE http://localhost:8080/api/gallery/1
```

### 참고사항
- **Windows CMD**: `curl` 명령어 사용 시 줄바꿈을 `^`로 표시해야 합니다.
- **이미지 경로**: `@` 뒤에 실제 파일 경로를 지정해야 합니다 (예: `@C:/Users/user/image.jpg`).
- **Multipart Form**: 이미지가 포함된 요청은 `-F` 플래그를 사용합니다.
- **JSON 요청**: 이미지가 없는 요청은 `-H "Content-Type: application/json"`과 `-d` 플래그를 사용합니다.

## 배포

### Docker를 이용한 배포

```bash
# 프로덕션 환경으로 실행
docker-compose up -d

# 서비스 상태 확인
docker-compose ps

# 백엔드 로그 확인
docker-compose logs -f backend

# Nginx 로그 확인
docker-compose logs -f nginx

# 모든 서비스 재시작
docker-compose restart
```

### SSL/TLS 인증서 설정

```bash
# SSL 인증서 발급 (최초 1회)
docker-compose run --rm certbot certonly --webroot \
  --webroot-path=/var/www/certbot \
  --email your-email@example.com \
  --agree-tos \
  --no-eff-email \
  -d yourdomain.com

# 인증서 자동 갱신은 Certbot 컨테이너가 12시간마다 자동 실행
```

### 프로필 설정

- **개발**: `application.properties` (H2)
- **배포**: `application-prod.properties` (MySQL)

환경 변수로 프로필 전환:
```bash
export SPRING_PROFILES_ACTIVE=prod
```

## 데이터베이스 마이그레이션

개발 환경에서 H2를 사용하다가 MySQL로 전환 시:

1. `application-prod.properties`에서 MySQL 설정 주석 해제
2. `.env` 파일에 데이터베이스 인증 정보 설정
3. Docker Compose로 MySQL 컨테이너 실행
4. `spring.jpa.hibernate.ddl-auto=update`로 자동 스키마 생성

### MySQL 직접 접속

```bash
# MySQL 컨테이너 접속
docker exec -it feel-mysql mysql -u feel -p

# 또는 로컬에서 접속
mysql -h localhost -P 3307 -u feel -p
```

## 프로젝트 구조

```
src/main/java/com/feel/backend/
├── BackendApplication.java
├── config/
│   ├── WebConfig.java           # CORS 설정
│   └── FileUploadConfig.java    # 파일 업로드 설정
├── controller/
│   ├── NoticeController.java    # 공지사항 API
│   └── GalleryController.java   # 갤러리 API
├── dto/
│   ├── NoticeRequestDto.java
│   ├── NoticeResponseDto.java
│   ├── GalleryRequestDto.java
│   └── GalleryResponseDto.java
├── entity/
│   ├── Notice.java              # 공지사항 엔티티
│   ├── NoticeCategory.java      # 공지사항 카테고리 Enum
│   └── Gallery.java             # 갤러리 엔티티
├── repository/
│   ├── NoticeRepository.java
│   └── GalleryRepository.java
└── service/
    ├── NoticeService.java       # 공지사항 비즈니스 로직
    ├── GalleryService.java      # 갤러리 비즈니스 로직
    └── FileStorageService.java  # 파일 업로드/삭제 로직
```

## CORS 설정

다음 Origin에서 접근 허용:
- http://localhost:3000 (React 개발 서버)
- https://m-se0k.github.io (배포된 프론트엔드)

새로운 프론트엔드 Origin을 추가하려면 `config/WebConfig.java` 또는 각 컨트롤러의 `@CrossOrigin` 어노테이션을 수정하세요.

## 파일 업로드

- **업로드 경로**: `uploads/` 디렉토리
- **지원 형식**: 이미지 파일 (JPEG, PNG, GIF 등)
- **최대 크기**: application.properties에서 설정 가능
- **공지사항**: 이미지 업로드 선택 사항
- **갤러리**: 이미지 업로드 필수

업로드된 파일은 `/uploads/{filename}` 경로로 접근 가능합니다.

## 빌드 및 테스트

```bash
# 클린 및 컴파일
./mvnw clean compile

# 테스트 실행
./mvnw test

# 패키징
./mvnw package

# 테스트 스킵하고 빌드
./mvnw package -DskipTests
```

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

## 주요 기능

### 공지사항 관리
- 공지사항 CRUD 작업
- 이미지 업로드 지원 (선택)
- 고정 공지사항 기능
- 카테고리별 분류 및 필터링
- 페이징 및 검색 기능
- 조회수 자동 추적
- 일괄 생성 기능

### 갤러리 관리
- 갤러리 CRUD 작업
- 이미지 업로드 지원 (필수)
- 카테고리별 분류 및 필터링
- 페이징 및 검색 기능
- 조회수 자동 추적
- 최근 갤러리 조회 기능

## 중요 참고사항

- **H2 파일 위치**: `./data/feeldb` (자동 생성됨)
- **포트 충돌**: 8080, 80, 443, 3307 포트가 사용 중이면 `docker-compose.yml` 수정 필요
- **Windows 호환성**: Windows CMD에서는 `mvnw.cmd` 사용 (Git Bash는 `./mvnw` 지원)
- **Lombok**: IDE에 Lombok 플러그인이 설치되어 있어야 어노테이션이 정상 처리됨
- **트랜잭션 관리**: 데이터를 수정하는 Service 메서드는 반드시 `@Transactional` 어노테이션 필요
- **파일 업로드**: `uploads/` 디렉토리는 자동으로 생성되며, Docker 볼륨으로 관리됨

## 라이선스

MIT

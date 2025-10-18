# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 프로젝트 개요

전북대학교 공과대학 FeeL 학생회 공지사항 관리 시스템 백엔드 서버 - Spring Boot REST API

**기술 스택:**
- Java 17 + Spring Boot 3.2.1
- Spring Data JPA with Hibernate
- H2 (개발 환경) / MySQL 8.0 (배포 환경)
- Maven 빌드 시스템
- Lombok

## 개발 명령어

### 애플리케이션 실행

**로컬 개발 환경 (H2):**
```bash
./mvnw spring-boot:run
```

**Docker + MySQL:**
```bash
# 환경 변수 설정
cp .env.example .env
# .env 파일 수정하여 DB_USERNAME, DB_PASSWORD 설정

# 서비스 시작
docker-compose up -d

# 로그 확인
docker-compose logs -f backend

# 서비스 종료
docker-compose down

# 볼륨까지 삭제
docker-compose down -v
```

### 빌드 및 테스트

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

### 데이터베이스 접근

**H2 Console (개발 환경 전용):**
- URL: http://localhost:8080/h2-console
- JDBC URL: `jdbc:h2:file:./data/feeldb`
- Username: `sa`
- Password: (비워두기)

**MySQL (Docker 실행 시):**
```bash
# MySQL 컨테이너 접속
docker exec -it feel-mysql mysql -u feel -p
```

## 아키텍처

### 패키지 구조

```
com.feel.backend/
├── config/          - CORS 및 애플리케이션 설정
├── controller/      - REST API 엔드포인트
├── dto/            - Request/Response 데이터 전송 객체
├── entity/         - JPA 엔티티
├── repository/     - Spring Data JPA 레포지토리
└── service/        - 비즈니스 로직 계층
```

### 핵심 컴포넌트

**Entity Layer (entity/Notice.java):**
- Lombok 어노테이션 사용 (@Entity, @Getter, @Setter, @Builder)
- @CreationTimestamp와 @UpdateTimestamp로 자동 타임스탬프 관리
- `incrementViewCount()` 메서드로 조회수 추적

**Service Layer (service/NoticeService.java):**
- @Transactional로 트랜잭션 경계 설정
- CRUD 작업의 비즈니스 로직
- 페이징 및 검색 기능
- 모든 읽기 작업은 @Transactional(readOnly = true) 사용

**Controller Layer (controller/NoticeController.java):**
- `/api/notices` 경로 하위의 RESTful 엔드포인트
- localhost:3000과 m-se0k.github.io에 대한 @CrossOrigin 설정
- DTO 검증을 위해 @Valid 사용

**Repository Layer (repository/NoticeRepository.java):**
- Spring Data JPA 인터페이스
- 명명 규칙을 따르는 커스텀 쿼리 메서드
- Pageable을 통한 페이징 지원

### 주요 디자인 패턴

1. **DTO 패턴**: 별도의 request/response DTO로 엔티티 노출 방지
2. **Service Layer**: 컨트롤러에서 비즈니스 로직 분리
3. **Repository 패턴**: Spring Data JPA를 통한 데이터 접근 추상화
4. **Builder 패턴**: Lombok @Builder를 사용한 엔티티 생성

## 데이터베이스 설정

### 프로필 기반 설정

- **개발 환경** (`application.properties`): `./data/feeldb`에 H2 파일 기반 데이터베이스
  - `spring.jpa.hibernate.ddl-auto=update` - 자동 스키마 업데이트
  - SQL 로깅 활성화

- **배포 환경** (`application-prod.properties`): MySQL/PostgreSQL
  - `spring.jpa.hibernate.ddl-auto=validate` - 스키마 변경 없음
  - SQL 로깅 비활성화
  - 환경 변수에서 데이터베이스 인증 정보 로드

### 프로필 전환

```bash
# 환경 변수로 설정
export SPRING_PROFILES_ACTIVE=prod

# 또는 커맨드 라인으로
./mvnw spring-boot:run -Dspring-profiles.active=prod
```

## API 엔드포인트

모든 엔드포인트는 `/api/notices` 하위:

- `POST /` - 공지사항 생성
- `GET /` - 전체 조회 (페이징: ?page=0&size=10)
- `GET /{id}` - ID로 조회 (조회수 증가)
- `PUT /{id}` - 공지사항 수정
- `DELETE /{id}` - 공지사항 삭제
- `GET /pinned` - 고정 공지사항 조회
- `GET /category/{category}` - 카테고리별 필터링
- `GET /search?keyword=검색어` - 제목/내용 검색

## CORS 설정

`config/WebConfig.java`에서 다음 Origin 허용:
- http://localhost:3000 (React 개발 서버)
- https://m-se0k.github.io (배포된 프론트엔드)

새로운 프론트엔드 Origin을 추가하려면 WebConfig.java 또는 NoticeController.java의 @CrossOrigin 어노테이션을 수정하세요.

## Docker 배포

`docker-compose.yml`은 두 개의 서비스를 조율:
- **backend**: Spring Boot 앱 (포트 8080)
- **db**: MySQL 8.0 (포트 3306)

두 서비스 모두 컨테이너 간 통신을 위해 `feel-network` 브리지 네트워크를 사용합니다. 백엔드 서비스는 데이터베이스에 의존하며 환경 변수를 통해 인증 정보를 사용합니다.

## 중요 참고사항

- **H2 파일 위치**: `./data/feeldb` (자동 생성됨)
- **포트 충돌**: 8080 또는 3306 포트가 사용 중이면 docker-compose.yml 또는 application.properties 수정
- **Windows 호환성**: Windows CMD에서는 `mvnw.cmd` 사용 (Git Bash는 `./mvnw` 지원)
- **Lombok**: IDE에 Lombok 플러그인이 설치되어 있어야 어노테이션이 정상 처리됨
- **트랜잭션 관리**: 데이터를 수정하는 Service 메서드는 반드시 @Transactional 어노테이션 필요
- **에러 핸들링**: 현재 RuntimeException 사용 중 - 프로덕션 환경에서는 커스텀 예외 처리 구현 권장

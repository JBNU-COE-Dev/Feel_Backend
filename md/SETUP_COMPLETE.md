# 백엔드 서버 환경 구축 완료 ✅

## 설치된 환경

### ✅ Java
- **버전**: Java 21.0.6 (요구사항: Java 17 이상)
- **상태**: 이미 설치됨
- **확인**: `java -version`

### ✅ Maven Wrapper
- **버전**: Maven 3.9.6 (Wrapper 사용)
- **상태**: 설치 완료
- **파일**:
  - `mvnw` (Unix/Linux/Mac)
  - `mvnw.cmd` (Windows)
  - `.mvn/wrapper/maven-wrapper.jar`
  - `.mvn/wrapper/maven-wrapper.properties`

### ✅ Spring Boot 백엔드
- **버전**: Spring Boot 3.2.1
- **상태**: 빌드 및 실행 성공
- **포트**: 8080
- **데이터베이스**: H2 (In-Memory)

## 빌드 테스트 결과

```bash
cd C:/Users/scheoleon/workspace/FeeL_Backend
./mvnw.cmd clean package -DskipTests
```

**결과**: ✅ BUILD SUCCESS

## 서버 실행 테스트 결과

```bash
cd C:/Users/scheoleon/workspace/FeeL_Backend
./mvnw.cmd spring-boot:run
```

**결과**: ✅ 서버 정상 실행
- 서버 시작 시간: 8.452초
- H2 Console: http://localhost:8080/h2-console
- API 엔드포인트: http://localhost:8080/api/notices

## API 테스트 결과

```bash
curl -X GET http://localhost:8080/api/notices
```

**결과**: ✅ API 응답 성공 (빈 목록 반환)

## 실행 방법

### 1. 백엔드 서버 시작 (Windows)

```bash
cd C:\Users\scheoleon\workspace\FeeL_Backend
.\mvnw.cmd spring-boot:run
```

### 2. 백엔드 서버 시작 (Git Bash/MSYS)

```bash
cd /c/Users/scheoleon/workspace/FeeL_Backend
./mvnw.cmd spring-boot:run
```

### 3. 서버 확인

브라우저에서 다음 URL 접속:
- API: http://localhost:8080/api/notices
- H2 Console: http://localhost:8080/h2-console
  - JDBC URL: `jdbc:h2:mem:feeldb`
  - Username: `sa`
  - Password: (비워두기)

### 4. 프론트엔드 연동

프론트엔드 프로젝트의 `.env` 파일에 이미 설정됨:
```
REACT_APP_API_URL=http://localhost:8080
```

React 개발 서버 실행:
```bash
cd C:\Users\scheoleon\workspace\FeeL_WEB
npm start
```

그 다음 브라우저에서:
- http://localhost:3000/notice/announcement

## 서버 중지 방법

백엔드 서버가 실행 중인 터미널에서:
- **Windows**: `Ctrl + C`
- **Git Bash**: `Ctrl + C`

## 테스트 데이터 생성

### ⚠️ 중요: Git Bash에서 한글 사용 시 인코딩 문제

Git Bash에서 curl로 한글을 전송하면 UTF-8 인코딩 에러가 발생합니다.
**영어로 테스트하거나 PowerShell을 사용하세요.**

### 공지사항 생성 (Git Bash - 영어)

```bash
curl -X POST http://localhost:8080/api/notices \
  -H "Content-Type: application/json" \
  -d "{\"title\":\"First Notice\",\"content\":\"This is a test notice.\",\"author\":\"Admin\",\"isPinned\":false,\"category\":\"Notice\"}"
```

**주의**:
- Git Bash에서는 작은따옴표(') 대신 **큰따옴표(")** 사용
- JSON 내부의 따옴표는 **백슬래시(\\)로 이스케이프** 필요
- 한글 사용 시 인코딩 에러 발생 → PowerShell 사용 권장

### 공지사항 생성 (Windows PowerShell - 한글 지원)

```powershell
# PowerShell에서는 한글 사용 가능
Invoke-RestMethod -Uri http://localhost:8080/api/notices -Method Post `
  -ContentType "application/json" `
  -Body '{"title":"첫 번째 공지사항","content":"테스트 공지사항입니다.","author":"관리자","isPinned":false,"category":"공지"}'
```

### 공지사항 생성 (Windows CMD)

```cmd
curl -X POST http://localhost:8080/api/notices -H "Content-Type: application/json" -d "{\"title\":\"Test Notice\",\"content\":\"Test content\",\"author\":\"Admin\",\"isPinned\":false,\"category\":\"Notice\"}"
```

### 여러 공지사항 생성 예제 (PowerShell)

```powershell
# 고정 공지사항
Invoke-RestMethod -Uri http://localhost:8080/api/notices -Method Post `
  -ContentType "application/json" `
  -Body '{"title":"[중요] FeeL 학생회 정기총회 안내","content":"2024학년도 FeeL 공과대학 학생회 정기총회를 개최합니다.","author":"집행부","isPinned":true,"category":"공지"}'

# 일반 공지사항
Invoke-RestMethod -Uri http://localhost:8080/api/notices -Method Post `
  -ContentType "application/json" `
  -Body '{"title":"봄 축제 부스 운영","content":"FeeL 학생회에서 봄 축제 부스를 운영합니다.","author":"문화예술국","isPinned":false,"category":"행사"}'
```

## Docker 관련 (선택 사항)

Docker와 Docker Compose는 현재 설치되지 않았습니다.
배포 시 필요하다면 다음을 설치하세요:

1. **Docker Desktop for Windows**: https://www.docker.com/products/docker-desktop
2. 설치 후 Docker Compose가 자동으로 포함됨

## 다음 단계

1. ✅ 백엔드 서버 구동 완료
2. ✅ React 프론트엔드 준비 완료
3. 🔄 프론트엔드에서 `/notice/announcement` 접속하여 통합 테스트
4. 📝 테스트 데이터 생성
5. 🎨 UI/UX 개선

## 트러블슈팅

### 포트 8080이 이미 사용 중인 경우

`src/main/resources/application.properties` 수정:
```properties
server.port=8081
```

그리고 프론트엔드 `.env` 파일도 수정:
```
REACT_APP_API_URL=http://localhost:8081
```

### 서버가 시작되지 않는 경우

1. Java 버전 확인: `java -version` (17 이상 필요)
2. 빌드 다시 실행: `./mvnw.cmd clean package -DskipTests`
3. 로그 확인하여 에러 메시지 파악

## 추가 정보

- 상세 가이드: `README.md`
- 빠른 시작: `QUICK_START.md`
- pom.xml: Maven 의존성 설정

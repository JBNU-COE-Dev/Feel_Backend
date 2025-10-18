# 빠른 시작 가이드

## 1. 백엔드 서버 실행 (개발 환경)

### Windows에서 실행

```bash
cd C:\Users\scheoleon\workspace\FeeL_Backend

# Maven Wrapper를 사용한 실행 (권장)
.\mvnw.cmd spring-boot:run

# 또는 Maven이 설치되어 있다면
mvn spring-boot:run
```

### Maven Wrapper가 없는 경우

Maven Wrapper 파일 다운로드:
```bash
# PowerShell에서 실행
Invoke-WebRequest -Uri https://repo.maven.apache.org/maven2/org/apache/maven/wrapper/maven-wrapper/3.2.0/maven-wrapper-3.2.0.jar -OutFile .mvn\wrapper\maven-wrapper.jar
```

또는 Maven 직접 설치: https://maven.apache.org/download.cgi

## 2. 서버 확인

서버가 정상적으로 실행되면:
- API 엔드포인트: http://localhost:8080/api/notices
- H2 Console: http://localhost:8080/h2-console

## 3. 테스트 데이터 생성

### Postman 또는 curl로 공지사항 생성

```bash
curl -X POST http://localhost:8080/api/notices ^
  -H "Content-Type: application/json" ^
  -d "{\"title\":\"두 번째 공지사항\",\"content\":\"테스트 공지사항입니다.\",\"author\":\"관리자\",\"isPinned\":false,\"category\":\"학과소식\"}"
```

### 여러 개 생성

```bash
curl -X POST http://localhost:8080/api/notices -H "Content-Type: application/json" -d "{\"title\":\"FeeL 학생회 정기총회 안내\",\"content\":\"2024학년도 FeeL 공과대학 학생회 정기총회를 다음과 같이 개최합니다.\n\n일시: 2024년 3월 15일 오후 2시\n장소: 공과대학 1호관 대강의실\n\n많은 참여 부탁드립니다.\",\"author\":\"집행부\",\"isPinned\":true,\"category\":\"공지\"}"

curl -X POST http://localhost:8080/api/notices -H "Content-Type: application/json" -d "{\"title\":\"봄 축제 부스 운영 안내\",\"content\":\"FeeL 학생회에서 봄 축제 기간 동안 부스를 운영합니다.\",\"author\":\"문화예술국\",\"isPinned\":false,\"category\":\"행사\"}"

curl -X POST http://localhost:8080/api/notices -H "Content-Type: application/json" -d "{\"title\":\"제휴업체 신규 등록\",\"content\":\"새로운 제휴업체가 추가되었습니다. 많은 이용 부탁드립니다.\",\"author\":\"대외협력국\",\"isPinned\":false,\"category\":\"제휴\"}"
```

## 4. 프론트엔드 연동

프론트엔드 프로젝트에서:

```bash
cd C:\Users\scheoleon\workspace\FeeL_WEB

# 백엔드 서버가 실행 중인지 확인 후
npm start
```

## 5. 공지사항 페이지 접속

브라우저에서:
- 공지사항 목록: http://localhost:3000/notice/announcement
- 특정 공지사항: http://localhost:3000/notice/announcement/1

## 트러블슈팅

### 포트 충돌 (8080 포트 사용 중)

`application.properties`에서 포트 변경:
```properties
server.port=8081
```

그리고 프론트엔드 `.env` 파일도 수정:
```
REACT_APP_API_URL=http://localhost:8081
```

### CORS 에러 발생 시

`WebConfig.java`의 allowedOrigins에 사용 중인 주소가 포함되어 있는지 확인

### H2 Console 접속 안 됨

`application.properties` 확인:
```properties
spring.h2.console.enabled=true
```

### Maven 빌드 실패

Java 버전 확인:
```bash
java -version
```

Java 17 이상이어야 합니다.

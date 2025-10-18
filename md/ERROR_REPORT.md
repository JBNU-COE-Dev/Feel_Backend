# 백엔드 서버 실행 에러 분석 보고서

## 📋 에러 개요

**증상**: `.\mvnw.cmd spring-boot:run` 실행 시 에러 발생

**발생 시각**: 2025-10-12

**원인**: **포트 8080이 이미 사용 중**

---

## 🔍 에러 원인 분석

### 1. 포트 충돌 (Port Conflict)

#### 현재 상태
```
TCP    0.0.0.0:8080           LISTENING       PID 4436
```

- **포트 8080**이 이미 **PID 4436** 프로세스에 의해 사용 중
- 이 프로세스는 이전에 제가 백그라운드로 실행한 Spring Boot 서버입니다

#### 에러 메시지 (예상)
새 터미널에서 실행했을 때 다음과 같은 에러가 발생했을 것입니다:

```
***************************
APPLICATION FAILED TO START
***************************

Description:

Web server failed to start. Port 8080 was already in use.

Action:

Identify and stop the process that's listening on port 8080 or configure this application to listen on another port.
```

또는:

```
Caused by: java.net.BindException: Address already in use: bind
```

---

## ✅ 해결 방법

### 방법 1: 기존 서버 종료 후 재시작 (권장)

#### 1-1. PID로 프로세스 종료
```bash
# Windows Command Prompt 또는 PowerShell
taskkill /F /PID 4436
```

#### 1-2. 포트로 프로세스 찾아서 종료
```bash
# 포트 8080을 사용하는 프로세스 찾기
netstat -ano | findstr :8080

# 출력된 PID로 종료
taskkill /F /PID [PID번호]
```

#### 1-3. 서버 재시작
```bash
cd C:\Users\scheoleon\workspace\FeeL_Backend
.\mvnw.cmd spring-boot:run
```

---

### 방법 2: 다른 포트 사용

#### 2-1. application.properties 수정

`src/main/resources/application.properties` 파일 수정:

```properties
# 기존
server.port=8080

# 변경
server.port=8081
```

#### 2-2. 프론트엔드 .env 파일도 수정

`C:\Users\scheoleon\workspace\FeeL_WEB\.env` 파일 수정:

```
REACT_APP_KAKAOAPIKEY=efd491b0c612b75c4158278dc900baeb
REACT_APP_API_URL=http://localhost:8081
```

#### 2-3. 서버 재시작
```bash
cd C:\Users\scheoleon\workspace\FeeL_Backend
.\mvnw.cmd spring-boot:run
```

---

### 방법 3: 프로필을 사용한 동적 포트 설정

#### 3-1. application-dev.properties 생성

`src/main/resources/application-dev.properties`:
```properties
server.port=8081
```

#### 3-2. 특정 프로필로 실행
```bash
.\mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=dev
```

---

## 🛠️ 현재 상황 해결 방법 (즉시 실행 가능)

### A. 기존 서버가 백그라운드에서 실행 중

현재 제가 백그라운드에서 실행한 서버(PID 4436)가 포트 8080을 사용하고 있습니다.

#### 옵션 1: 기존 서버 활용
기존 서버를 그대로 사용하세요:
- API: http://localhost:8080/api/notices
- H2 Console: http://localhost:8080/h2-console

#### 옵션 2: 기존 서버 종료 후 새로 시작
```bash
# 1. 기존 서버 종료
taskkill /F /PID 4436

# 2. 새 서버 시작
cd C:\Users\scheoleon\workspace\FeeL_Backend
.\mvnw.cmd spring-boot:run
```

---

## 📊 포트 사용 확인 명령어

### Windows에서 포트 확인
```bash
# 포트 8080 사용 확인
netstat -ano | findstr :8080

# 특정 PID 프로세스 정보 확인
tasklist | findstr [PID번호]
```

### Git Bash에서 포트 확인
```bash
netstat -ano | grep 8080
```

---

## 🚨 예방 방법

### 1. 서버 시작 전 포트 확인
```bash
netstat -ano | findstr :8080
```

### 2. 서버 종료 시 정상 종료
- **Ctrl + C**를 눌러 정상 종료
- 강제 종료(창 닫기)는 피할 것

### 3. 개발 환경과 포트 분리
- 개발(dev): 8080
- 테스트(test): 8081
- 프로덕션(prod): 설정된 포트

### 4. 서버 실행 전 체크리스트
- [ ] 포트 8080이 비어있는지 확인
- [ ] 이전 서버 프로세스가 종료되었는지 확인
- [ ] Java 버전 확인 (Java 17 이상)

---

## 📝 에러 로그 확인 방법

### 전체 에러 로그 보기
```bash
cd C:\Users\scheoleon\workspace\FeeL_Backend
.\mvnw.cmd spring-boot:run
```

실행 시 콘솔에 출력되는 전체 로그를 확인하세요.

### 일반적인 에러 패턴

#### 1. 포트 충돌
```
Caused by: java.net.BindException: Address already in use
```

#### 2. 데이터베이스 연결 실패
```
Caused by: java.sql.SQLException: Cannot connect to database
```

#### 3. 컴파일 에러
```
[ERROR] COMPILATION ERROR
[ERROR] /path/to/file.java:[line,column] error message
```

#### 4. 의존성 문제
```
[ERROR] Failed to execute goal ... Could not resolve dependencies
```

---

## 🎯 결론

### 에러 원인
**포트 8080이 이미 사용 중** (PID 4436 프로세스)

### 권장 해결책
1. **기존 프로세스 종료**: `taskkill /F /PID 4436`
2. **서버 재시작**: `.\mvnw.cmd spring-boot:run`

### 대안
- 다른 포트 사용 (8081 등)
- 기존 실행 중인 서버 활용

---

## 📞 추가 지원

추가 에러가 발생하면 다음 정보를 제공해주세요:
1. 전체 에러 로그 (콘솔 출력)
2. `java -version` 출력
3. `netstat -ano | findstr :8080` 출력
4. 실행한 정확한 명령어

# 500 Internal Server Error 분석 보고서

## 📋 에러 개요

**증상**: http://localhost:8080/api/notices 접속 시 500 Internal Server Error 발생

**에러 메시지**:
```
{"timestamp":"2025-10-12T15:42:53.216+00:00","status":500,"error":"Internal Server Error","path":"/api/notices"}
```

---

## 🔍 에러 원인 분석

### 서버 로그 확인:
```
ERROR: Table "NOTICES" not found (this database is empty)
SQL Error: 42104, SQLState: 42S04

org.h2.jdbc.JdbcSQLSyntaxErrorException: Table "NOTICES" not found (this database is empty)
org.springframework.dao.InvalidDataAccessResourceUsageException: could not prepare statement
```

### **근본 원인**: H2 인메모리 데이터베이스 초기화

#### 문제점:
1. **H2 인메모리 데이터베이스**는 서버가 실행되는 동안만 데이터를 메모리에 저장
2. 다음 상황에서 데이터베이스가 **자동으로 초기화**됨:
   - 서버 재시작
   - 장시간 유휴 상태
   - 메모리 정리

3. 서버가 처음 시작할 때는 `spring.jpa.hibernate.ddl-auto=update` 설정에 의해 테이블이 자동 생성됨
4. 하지만 서버가 계속 실행 중일 때 H2가 메모리를 정리하면 테이블이 사라짐
5. 이후 API 호출 시 테이블이 없어서 500 에러 발생

---

## ✅ 해결 방법

### **방법 1: 서버 재시작 (즉시 해결)**

#### 1-1. 기존 서버 종료
```bash
# PID 확인
netstat -ano | findstr :8080

# 종료 (PID 4436 예시)
taskkill /F /PID 4436
```

#### 1-2. 서버 재시작
```bash
cd C:\Users\scheoleon\workspace\FeeL_Backend
.\mvnw.cmd spring-boot:run
```

**결과**: 테이블이 자동으로 다시 생성되고 정상 작동 ✅

---

### **방법 2: H2 파일 기반 데이터베이스 사용 (권장)**

인메모리 대신 **파일 기반** H2를 사용하면 서버 재시작 후에도 데이터 유지

#### 2-1. application.properties 수정

현재:
```properties
spring.datasource.url=jdbc:h2:mem:feeldb
```

변경:
```properties
# 파일 기반 H2 (데이터 영구 저장)
spring.datasource.url=jdbc:h2:file:./data/feeldb
```

#### 2-2. 서버 재시작
```bash
.\mvnw.cmd spring-boot:run
```

**장점**:
- ✅ 서버 재시작 후에도 데이터 유지
- ✅ 개발 중 데이터 손실 방지
- ✅ 파일 시스템에 `./data/feeldb.mv.db` 파일로 저장

**단점**:
- ⚠️ 파일 크기 증가
- ⚠️ 메모리 기반보다 약간 느림

---

### **방법 3: DDL Auto 설정 변경**

#### 3-1. application.properties 수정

현재:
```properties
spring.jpa.hibernate.ddl-auto=update
```

변경:
```properties
# 매번 테이블 재생성
spring.jpa.hibernate.ddl-auto=create-drop
```

또는:
```properties
# 항상 새로 생성
spring.jpa.hibernate.ddl-auto=create
```

**주의**: 이 방법은 서버 재시작 시 **모든 데이터가 삭제**됩니다!

---

### **방법 4: H2 연결 유지 설정 추가**

#### 4-1. application.properties에 추가

```properties
# 기존
spring.datasource.url=jdbc:h2:mem:feeldb

# 변경 - DB_CLOSE_DELAY 옵션 추가
spring.datasource.url=jdbc:h2:mem:feeldb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE
```

**설명**:
- `DB_CLOSE_DELAY=-1`: 마지막 연결이 닫혀도 데이터베이스를 유지
- `DB_CLOSE_ON_EXIT=FALSE`: JVM 종료 시에만 DB 닫기

---

## 🎯 권장 솔루션

### **개발 환경**: 파일 기반 H2 (방법 2)

`application.properties`:
```properties
# 파일 기반 H2 - 데이터 영구 저장
spring.datasource.url=jdbc:h2:file:./data/feeldb
spring.datasource.driverClassName=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=

# H2 Console
spring.h2.console.enabled=true
spring.h2.console.path=/h2-console

# JPA Configuration
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect
```

### **테스트/임시 환경**: 인메모리 + 연결 유지 (방법 4)

```properties
spring.datasource.url=jdbc:h2:mem:feeldb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE
```

---

## 📊 각 방법 비교

| 방법 | 데이터 보존 | 성능 | 설정 난이도 | 권장도 |
|------|------------|------|-------------|--------|
| **서버 재시작** | ❌ 임시 | ⚡ 빠름 | ⭐ 쉬움 | ⭐⭐ |
| **파일 기반 H2** | ✅ 영구 | ⚡ 보통 | ⭐⭐ 쉬움 | ⭐⭐⭐⭐⭐ |
| **DDL Auto 변경** | ❌ 재시작마다 삭제 | ⚡ 빠름 | ⭐ 쉬움 | ⭐ |
| **연결 유지 설정** | ⚠️ 세션 중 | ⚡ 빠름 | ⭐⭐ 쉬움 | ⭐⭐⭐ |

---

## 🔧 즉시 해결 명령어

```bash
# 1. 기존 서버 중지
taskkill /F /PID 4436

# 2. 서버 재시작
cd C:\Users\scheoleon\workspace\FeeL_Backend
.\mvnw.cmd spring-boot:run
```

서버 재시작 후 테스트:
```bash
curl http://localhost:8080/api/notices
```

---

## 🚨 예방 방법

### 1. 파일 기반 H2 사용
- 개발 환경에서는 **파일 기반 H2** 사용 권장
- 데이터 손실 방지

### 2. 정기적인 데이터 백업
- 중요한 테스트 데이터는 SQL 파일로 export
- 서버 재시작 후 import

### 3. 초기 데이터 스크립트 작성
`src/main/resources/data.sql`:
```sql
-- 초기 데이터
INSERT INTO notices (title, content, author, is_pinned, category, view_count, created_at, updated_at)
VALUES ('Welcome', 'Welcome notice', 'Admin', true, 'Notice', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
```

Spring Boot가 서버 시작 시 자동으로 실행

---

## 📝 테스트 시나리오

### 시나리오 1: 인메모리 H2 (현재 설정)
1. 서버 시작 → 테이블 자동 생성 ✅
2. 데이터 입력 → 정상 작동 ✅
3. 장시간 유휴 또는 재시작 → 데이터 손실 ❌
4. API 호출 → 500 에러 발생 ❌

### 시나리오 2: 파일 기반 H2 (권장)
1. 서버 시작 → 테이블 자동 생성 ✅
2. 데이터 입력 → 정상 작동 ✅
3. 서버 재시작 → 데이터 유지 ✅
4. API 호출 → 정상 작동 ✅

---

## 🔗 관련 문서

- **ERROR_REPORT.md**: 포트 충돌 에러
- **CURL_ERROR_ANALYSIS.md**: curl 인코딩 에러
- **SETUP_COMPLETE.md**: 환경 설정 가이드
- **README.md**: 전체 프로젝트 가이드

---

## 💡 추가 팁

### H2 Console 접속
http://localhost:8080/h2-console

**인메모리 DB**:
- JDBC URL: `jdbc:h2:mem:feeldb`

**파일 기반 DB**:
- JDBC URL: `jdbc:h2:file:./data/feeldb`

공통:
- Username: `sa`
- Password: (비워두기)

### 데이터 확인 SQL
```sql
-- 테이블 목록
SHOW TABLES;

-- 공지사항 조회
SELECT * FROM NOTICES;

-- 테이블 구조
SHOW COLUMNS FROM NOTICES;
```

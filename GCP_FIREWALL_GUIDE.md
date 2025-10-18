# GCP 방화벽 규칙 설정 가이드

## 기본 방화벽 규칙 확인

GCP Compute Engine에서 **default** VPC 네트워크를 사용하는 경우, 다음 기본 규칙들이 자동으로 생성됩니다:

### 자동 생성되는 기본 규칙들

```
1. default-allow-internal (내부 통신)
2. default-allow-ssh (포트 22)
3. default-allow-rdp (포트 3389, Windows)
4. default-allow-icmp (Ping)
```

**주의:** `default-allow-http`와 `default-allow-https`는 **자동 생성되지 않습니다!**

VM 인스턴스 생성 시 "HTTP 트래픽 허용" 체크박스를 선택하면:
- 인스턴스에 `http-server` 태그가 추가됩니다
- 하지만 **포트 80**만 열리고, **포트 8080**은 열리지 않습니다

## FeeL Backend에 필요한 방화벽 규칙

### 필수 규칙 1: 백엔드 API (포트 8080)

**방법 1: 웹 콘솔에서 생성**

1. GCP Console > **VPC 네트워크** > **방화벽** 클릭
2. **방화벽 규칙 만들기** 클릭
3. 다음과 같이 입력:

```yaml
이름: allow-backend-api
설명: Allow access to Spring Boot backend on port 8080
로그: 사용 중지됨
네트워크: default
우선순위: 1000
트래픽 방향: 수신
일치 시 작업: 허용

대상:
  대상 유형: 네트워크의 모든 인스턴스
  또는
  대상 태그: backend-server (VM에 태그 추가 필요)

소스 필터:
  소스: IPv4 범위
  소스 IPv4 범위: 0.0.0.0/0

프로토콜 및 포트:
  ☑ 지정된 프로토콜 및 포트
  ☑ TCP
  포트: 8080
```

4. **만들기** 클릭

**방법 2: gcloud 명령어로 생성**

```bash
gcloud compute firewall-rules create allow-backend-api \
    --network=default \
    --allow=tcp:8080 \
    --source-ranges=0.0.0.0/0 \
    --description="Allow access to Spring Boot backend on port 8080"
```

### 선택 규칙 2: MySQL 외부 접속 (포트 3307)

**보안상 권장하지 않음** - MySQL은 백엔드 컨테이너에서만 접근해야 합니다.

하지만 개발/디버깅 목적으로 필요하다면:

```bash
gcloud compute firewall-rules create allow-mysql \
    --network=default \
    --allow=tcp:3307 \
    --source-ranges=YOUR_IP/32 \
    --description="Allow MySQL access from specific IP only"
```

**주의:** `YOUR_IP/32`를 본인 IP로 변경하여 특정 IP만 허용하세요!

## 방화벽 규칙 확인

### 웹 콘솔에서 확인
1. **VPC 네트워크** > **방화벽**
2. 다음 규칙들이 있는지 확인:
   - `default-allow-ssh` (기본)
   - `allow-backend-api` (직접 생성)

### gcloud 명령어로 확인

```bash
# 모든 방화벽 규칙 조회
gcloud compute firewall-rules list

# 특정 규칙 상세 보기
gcloud compute firewall-rules describe allow-backend-api

# 포트 8080을 허용하는 규칙 필터링
gcloud compute firewall-rules list --filter="allowed[]:8080"
```

## VM 인스턴스에 태그 추가 (선택사항)

방화벽 규칙을 특정 VM에만 적용하고 싶다면 네트워크 태그를 사용하세요.

### 1. VM에 태그 추가

**웹 콘솔:**
1. **Compute Engine** > **VM 인스턴스**
2. 인스턴스 이름 클릭
3. **수정** 버튼 클릭
4. **네트워크 태그** 섹션에 `backend-server` 입력
5. **저장** 클릭

**gcloud 명령어:**
```bash
gcloud compute instances add-tags feel-backend-vm \
    --tags=backend-server \
    --zone=asia-northeast3-a
```

### 2. 방화벽 규칙에서 태그 지정

```bash
gcloud compute firewall-rules create allow-backend-api \
    --network=default \
    --allow=tcp:8080 \
    --source-ranges=0.0.0.0/0 \
    --target-tags=backend-server \
    --description="Allow backend API access to tagged instances"
```

## 방화벽 규칙 테스트

### 1. VM에서 포트 리스닝 확인

```bash
# SSH로 VM 접속 후
sudo netstat -tlnp | grep 8080

# 또는
sudo ss -tlnp | grep 8080

# 예상 출력:
# tcp6  0  0 :::8080  :::*  LISTEN  12345/java
```

### 2. 외부에서 접속 테스트

**로컬 컴퓨터에서:**

```bash
# 헬스체크 테스트
curl http://EXTERNAL_IP:8080/actuator/health

# 예상 응답:
# {"status":"UP"}

# API 테스트
curl http://EXTERNAL_IP:8080/api/notices
```

**브라우저에서:**
```
http://EXTERNAL_IP:8080/actuator/health
http://EXTERNAL_IP:8080/api/notices
```

### 3. 포트 접근 테스트 (telnet)

```bash
# Windows PowerShell / Mac / Linux
telnet EXTERNAL_IP 8080

# 연결 성공 시:
# Trying EXTERNAL_IP...
# Connected to EXTERNAL_IP.

# 연결 실패 시 (방화벽 차단):
# Trying EXTERNAL_IP...
# telnet: Unable to connect to remote host: Connection refused
```

### 4. nmap으로 포트 스캔 (선택사항)

```bash
# nmap 설치 필요
nmap -p 8080 EXTERNAL_IP

# 예상 출력:
# PORT     STATE SERVICE
# 8080/tcp open  http-proxy
```

## 보안 강화 팁

### 1. 소스 IP 제한

특정 IP에서만 접근 허용:

```bash
gcloud compute firewall-rules update allow-backend-api \
    --source-ranges=YOUR_IP/32,ANOTHER_IP/32
```

### 2. 프론트엔드 도메인만 허용

방화벽 대신 Spring Boot CORS 설정으로 제어 (이미 구현됨):

```java
// WebConfig.java
.allowedOrigins(
    "http://localhost:3000",
    "https://m-se0k.github.io",
    "https://m-se0k.github.io/FeeL_WEB"
)
```

### 3. Cloud Armor 사용 (고급)

DDoS 방어 및 IP 기반 필터링:

```bash
# Cloud Armor 정책 생성
gcloud compute security-policies create backend-policy \
    --description="Backend API security policy"

# 특정 국가만 허용 (예: 한국)
gcloud compute security-policies rules create 1000 \
    --security-policy=backend-policy \
    --expression="origin.region_code == 'KR'" \
    --action=allow
```

## 일반적인 문제 해결

### 문제 1: 방화벽 규칙이 있는데도 접속 안됨

**확인 사항:**

1. VM 인스턴스가 실행 중인지 확인
   ```bash
   gcloud compute instances list
   ```

2. Docker 컨테이너가 실행 중인지 확인
   ```bash
   docker-compose ps
   ```

3. 애플리케이션이 8080 포트에서 리스닝 중인지 확인
   ```bash
   sudo netstat -tlnp | grep 8080
   ```

4. VM 인스턴스의 네트워크 태그 확인
   ```bash
   gcloud compute instances describe feel-backend-vm \
       --zone=asia-northeast3-a \
       --format="value(tags.items)"
   ```

### 문제 2: "Connection refused" 오류

**원인:** 애플리케이션이 시작되지 않았거나 포트가 닫혀있음

**해결:**
```bash
# 로그 확인
docker-compose logs -f backend

# 컨테이너 재시작
docker-compose restart backend
```

### 문제 3: "Connection timed out" 오류

**원인:** 방화벽 규칙이 없거나 잘못 설정됨

**해결:**
```bash
# 방화벽 규칙 재생성
gcloud compute firewall-rules delete allow-backend-api
gcloud compute firewall-rules create allow-backend-api \
    --network=default \
    --allow=tcp:8080 \
    --source-ranges=0.0.0.0/0
```

## 방화벽 규칙 삭제

더 이상 필요 없는 규칙 삭제:

```bash
# 웹 콘솔에서
VPC 네트워크 > 방화벽 > 규칙 선택 > 삭제

# gcloud 명령어
gcloud compute firewall-rules delete allow-backend-api
gcloud compute firewall-rules delete allow-mysql
```

## 최종 권장 설정

**최소 필수 규칙 (프로덕션):**

```bash
# 1. SSH 접속 (기본 규칙)
default-allow-ssh (포트 22)

# 2. 백엔드 API
allow-backend-api (포트 8080)

# MySQL 외부 접속은 차단 (Docker 네트워크 내부 통신만 사용)
```

**개발/디버깅 시 추가 규칙:**

```bash
# 특정 IP에서만 MySQL 접속
allow-mysql (포트 3307, 소스 IP 제한)
```

---

## 요약

1. **default-allow-http는 자동 생성 안됨** - 직접 만들어야 함
2. **포트 8080 방화벽 규칙 필수** - `allow-backend-api` 생성
3. **포트 3307은 선택사항** - 보안상 열지 않는 것 권장
4. **방화벽 규칙 테스트** - curl, telnet, nmap으로 확인
5. **보안 강화** - 소스 IP 제한, CORS 설정, Cloud Armor

방화벽 규칙 생성 후 2-3분 후에 적용되므로 약간의 대기 시간이 필요합니다.

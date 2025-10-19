# Oracle Cloud Free Tier 배포 가이드

전북대 FeeL 백엔드 서버를 Oracle Cloud Free Tier에 배포하는 단계별 가이드입니다.

## 목차
1. [Oracle Cloud 계정 생성 및 VM 인스턴스 생성](#1-oracle-cloud-계정-생성-및-vm-인스턴스-생성)
2. [VM 인스턴스 초기 설정](#2-vm-인스턴스-초기-설정)
3. [프로젝트 파일 업로드](#3-프로젝트-파일-업로드)
4. [애플리케이션 배포](#4-애플리케이션-배포)
5. [프론트엔드 연결 설정](#5-프론트엔드-연결-설정)
6. [트러블슈팅](#6-트러블슈팅)

---

## 1. Oracle Cloud 계정 생성 및 VM 인스턴스 생성

### 1.1 Oracle Cloud 계정 생성
1. [Oracle Cloud](https://www.oracle.com/kr/cloud/free/) 접속
2. "무료로 시작하기" 클릭
3. 계정 정보 입력 (신용카드 필요하지만 무료 티어 사용 시 과금 없음)
4. 이메일 인증 및 계정 활성화

### 1.2 VM 인스턴스 생성

#### 인스턴스 생성 절차
1. Oracle Cloud Console 로그인
2. 좌측 메뉴 > **Compute** > **Instances** 클릭
3. **Create Instance** 버튼 클릭

#### 인스턴스 설정
```
Name: feel-backend-server
Placement: 기본값 유지
Image: Oracle Linux 8 (기본값)
Shape: VM.Standard.E2.1.Micro (Always Free 무료 티어)
  - OCPU: 1
  - Memory: 1GB
  - Network Bandwidth: 0.48 Gbps
```

#### 네트워크 설정
```
Virtual Cloud Network: 기본 VCN 사용 또는 새로 생성
Subnet: Public Subnet 선택
Public IP Address: "Assign a public IPv4 address" 선택 ✓
```

#### SSH 키 설정
```
- "Generate SSH Key Pair" 선택
- Private Key 다운로드 (중요! 분실 시 서버 접속 불가)
- Public Key 자동 등록
```

4. **Create** 버튼 클릭
5. 인스턴스 상태가 "Running"이 될 때까지 대기 (약 1-2분)
6. **Public IP Address** 확인 및 메모

---

## 2. VM 인스턴스 초기 설정

### 2.1 SSH 접속

#### Windows (Git Bash 또는 PowerShell)
```bash
# Private Key 권한 설정 (Git Bash)
chmod 400 ~/Downloads/ssh-key-*.key

# SSH 접속
ssh -i ~/Downloads/ssh-key-*.key opc@YOUR_PUBLIC_IP
```

#### Mac/Linux
```bash
# Private Key 권한 설정
chmod 400 ~/Downloads/ssh-key-*.key

# SSH 접속
ssh -i ~/Downloads/ssh-key-*.key opc@YOUR_PUBLIC_IP
```

### 2.2 방화벽 규칙 설정 (Oracle Cloud Console)

#### Ingress Rules 추가
1. Compute > Instances > 인스턴스 클릭
2. Primary VNIC의 **Subnet** 클릭
3. **Default Security List** 클릭
4. **Add Ingress Rules** 클릭

**규칙 1: HTTP (백엔드 API)**
```
Source Type: CIDR
Source CIDR: 0.0.0.0/0
IP Protocol: TCP
Source Port Range: All
Destination Port Range: 8080
Description: FeeL Backend API
```

**규칙 2: MySQL (선택사항 - 외부 DB 접속 필요 시)**
```
Source Type: CIDR
Source CIDR: 0.0.0.0/0
IP Protocol: TCP
Source Port Range: All
Destination Port Range: 3307
Description: MySQL Database
```

5. **Add Ingress Rules** 클릭하여 저장

### 2.3 서버 OS 방화벽 설정

SSH로 서버 접속 후 실행:

```bash
# 포트 개방
sudo firewall-cmd --permanent --add-port=8080/tcp
sudo firewall-cmd --permanent --add-port=3307/tcp
sudo firewall-cmd --reload

# 방화벽 상태 확인
sudo firewall-cmd --list-all
```

---

## 3. 프로젝트 파일 업로드

### 방법 1: Git Clone (추천)

서버에서 실행:
```bash
# Git 설치 (Oracle Linux 8)
sudo yum install -y git

# 프로젝트 클론
cd ~
git clone https://github.com/YOUR_USERNAME/FeeL_Backend.git
cd FeeL_Backend
```

### 방법 2: SCP로 직접 업로드

로컬 컴퓨터에서 실행:
```bash
# Windows (Git Bash)
cd /c/Users/scheoleon/workspace/FeeL_Backend
scp -i ~/Downloads/ssh-key-*.key -r ./* opc@YOUR_PUBLIC_IP:~/FeeL_Backend/

# Mac/Linux
cd ~/workspace/FeeL_Backend
scp -i ~/Downloads/ssh-key-*.key -r ./* opc@YOUR_PUBLIC_IP:~/FeeL_Backend/
```

---

## 4. 애플리케이션 배포

### 4.1 환경 변수 설정

서버에서 실행:
```bash
cd ~/FeeL_Backend

# .env 파일 생성
cp .env.production .env

# 환경 변수 편집
nano .env
```

`.env` 파일 내용:
```bash
DB_USERNAME=feel
DB_PASSWORD=YOUR_SECURE_PASSWORD_HERE  # 강력한 비밀번호로 변경!
SPRING_PROFILES_ACTIVE=prod
```

저장: `Ctrl + O` → Enter → `Ctrl + X`

### 4.2 배포 스크립트 실행

```bash
# 스크립트 실행 권한 부여
chmod +x deploy-oracle.sh

# 배포 시작
./deploy-oracle.sh
```

스크립트가 자동으로 수행하는 작업:
1. 시스템 패키지 업데이트
2. Docker 및 Docker Compose 설치
3. 방화벽 포트 개방
4. 애플리케이션 디렉토리 설정
5. Docker Compose로 백엔드 + MySQL 시작

### 4.3 배포 확인

```bash
# 컨테이너 상태 확인
docker-compose ps

# 예상 출력:
#     Name                   Command               State           Ports
# --------------------------------------------------------------------------------
# feel-backend     java -jar app.jar                Up      0.0.0.0:8080->8080/tcp
# feel-mysql       docker-entrypoint.sh mysqld      Up      0.0.0.0:3307->3306/tcp

# 로그 확인
docker-compose logs -f backend

# 빠져나오기: Ctrl + C
```

### 4.4 API 테스트

```bash
# 서버에서 테스트
curl http://localhost:8080/actuator/health

# 로컬 컴퓨터에서 테스트 (브라우저 또는 터미널)
curl http://YOUR_PUBLIC_IP:8080/actuator/health

# 예상 응답:
# {"status":"UP"}
```

브라우저에서 접속:
```
http://YOUR_PUBLIC_IP:8080/api/notices
```

---

## 5. 프론트엔드 연결 설정

### 5.1 서버 IP 확인

```bash
# 서버 공인 IP 확인
curl ifconfig.me
```

### 5.2 백엔드 CORS 설정 업데이트

`src/main/java/com/feel/backend/config/WebConfig.java` 파일 수정:

```java
@Override
public void addCorsMappings(CorsRegistry registry) {
    registry.addMapping("/api/**")
        .allowedOrigins(
            "http://localhost:3000",
            "https://m-se0k.github.io",
            "https://m-se0k.github.io/FeeL_WEB",
            "http://YOUR_PUBLIC_IP:3000"  // 서버 IP 추가
        )
        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
        .allowedHeaders("*")
        .allowCredentials(true)
        .maxAge(3600);

    registry.addMapping("/uploads/**")
        .allowedOrigins(
            "http://localhost:3000",
            "https://m-se0k.github.io",
            "https://m-se0k.github.io/FeeL_WEB",
            "http://YOUR_PUBLIC_IP:3000"  // 서버 IP 추가
        )
        .allowedMethods("GET", "OPTIONS")
        .allowedHeaders("*")
        .allowCredentials(true)
        .maxAge(3600);
}
```

수정 후 재배포:
```bash
git add .
git commit -m "Update CORS settings for production"
git push

# 서버에서
cd ~/FeeL_Backend
git pull
docker-compose down
docker-compose up -d --build
```

### 5.3 프론트엔드 API URL 설정

프론트엔드 환경 변수에 백엔드 URL 설정:

```javascript
// .env.production
REACT_APP_API_URL=http://YOUR_PUBLIC_IP:8080
```

---

## 6. 트러블슈팅

### 문제 1: 컨테이너가 시작되지 않음

```bash
# 로그 확인
docker-compose logs backend

# 컨테이너 재시작
docker-compose restart backend

# 완전히 재배포
docker-compose down -v
docker-compose up -d --build
```

### 문제 2: 외부에서 API 접속 안됨

```bash
# 1. 방화벽 규칙 확인
sudo firewall-cmd --list-all

# 2. 포트 리스닝 확인
sudo netstat -tlnp | grep 8080

# 3. Oracle Cloud Security List 확인
# Console > Networking > Virtual Cloud Networks > Security Lists
```

### 문제 3: MySQL 연결 오류

```bash
# MySQL 로그 확인
docker-compose logs db

# MySQL 컨테이너 접속
docker exec -it feel-mysql mysql -u feel -p
# 비밀번호 입력

# 데이터베이스 확인
SHOW DATABASES;
USE feeldb;
SHOW TABLES;
```

### 문제 4: Out of Memory

Oracle Free Tier는 1GB RAM만 제공하므로 메모리 부족 발생 가능:

```bash
# 메모리 사용량 확인
free -h

# Swap 메모리 생성 (2GB)
sudo dd if=/dev/zero of=/swapfile bs=1M count=2048
sudo chmod 600 /swapfile
sudo mkswap /swapfile
sudo swapon /swapfile

# 영구 적용
echo '/swapfile none swap sw 0 0' | sudo tee -a /etc/fstab
```

### 문제 5: Docker 빌드 실패

```bash
# Docker 로그 확인
docker-compose logs --tail=100 backend

# 수동 빌드 시도
docker-compose build --no-cache backend
docker-compose up -d
```

---

## 유용한 명령어 모음

```bash
# 서비스 상태 확인
docker-compose ps

# 실시간 로그 확인
docker-compose logs -f

# 서비스 재시작
docker-compose restart

# 서비스 중지
docker-compose down

# 서비스 시작
docker-compose up -d

# 볼륨 포함 완전 삭제
docker-compose down -v

# 디스크 사용량 확인
df -h

# 메모리 사용량 확인
free -h

# Docker 디스크 정리
docker system prune -a
```

---

## 보안 권장사항

1. **SSH 키 관리**
   - Private Key 안전하게 보관
   - 필요 시 추가 SSH 키 생성 및 등록

2. **데이터베이스 비밀번호**
   - 강력한 비밀번호 사용 (영문 대소문자, 숫자, 특수문자 혼합)
   - 정기적으로 변경

3. **방화벽 설정**
   - 필요한 포트만 개방
   - MySQL 포트(3307)는 가급적 외부 접근 차단

4. **정기 업데이트**
   ```bash
   sudo yum update -y
   docker-compose pull
   docker-compose up -d --build
   ```

5. **백업**
   ```bash
   # 데이터베이스 백업
   docker exec feel-mysql mysqldump -u feel -p feeldb > backup_$(date +%Y%m%d).sql
   ```

---

## 비용 관련 정보

### Oracle Cloud Free Tier 무료 제공 내역

- **VM 인스턴스**: 2개 (각 1 OCPU, 1GB RAM)
- **Block Volume**: 200GB
- **Object Storage**: 20GB
- **Outbound Data Transfer**: 10TB/월
- **Load Balancer**: 1개
- **Autonomous Database**: 2개 (각 1 OCPU, 20GB)

**주의사항:**
- Free Tier 리소스 초과 시 과금 가능
- 항상 Free Tier 자원만 사용하도록 주의
- Billing 대시보드에서 사용량 정기 확인

---

## 추가 개선 사항

### 1. HTTPS 설정 (Let's Encrypt)

```bash
# Certbot 설치
sudo yum install -y certbot

# 인증서 발급 (도메인 필요)
sudo certbot certonly --standalone -d yourdomain.com

# Nginx 리버스 프록시 설정 (선택사항)
```

### 2. 도메인 연결

1. 도메인 구입 (freenom.com에서 무료 도메인 가능)
2. DNS A 레코드에 서버 Public IP 등록
3. HTTPS 인증서 발급

### 3. 모니터링 설정

```bash
# Docker 리소스 모니터링
docker stats

# 시스템 모니터링 (htop)
sudo yum install -y htop
htop
```

---

## 참고 자료

- [Oracle Cloud 공식 문서](https://docs.oracle.com/en-us/iaas/Content/home.htm)
- [Docker 공식 문서](https://docs.docker.com/)
- [Spring Boot 배포 가이드](https://spring.io/guides/gs/spring-boot-docker/)

---

## 문제 발생 시 연락처

- GitHub Issues: [FeeL_Backend Issues](https://github.com/YOUR_USERNAME/FeeL_Backend/issues)
- 전북대 FeeL 학생회 이메일: feel@jbnu.ac.kr

---

**배포 완료 후 확인 사항:**
- [ ] API 엔드포인트 접속 확인
- [ ] 프론트엔드 연동 테스트
- [ ] 데이터베이스 연결 확인
- [ ] 이미지 업로드 기능 테스트
- [ ] CORS 설정 확인
- [ ] 로그 모니터링 설정

# Google Cloud Platform 배포 가이드

전북대 FeeL 백엔드 서버를 Google Cloud Platform(GCP)에 배포하는 단계별 가이드입니다.

## 목차
1. [GCP 계정 생성 및 초기 설정](#1-gcp-계정-생성-및-초기-설정)
2. [Compute Engine VM 인스턴스 생성](#2-compute-engine-vm-인스턴스-생성)
3. [방화벽 규칙 설정](#3-방화벽-규칙-설정)
4. [VM 인스턴스 접속 및 초기 설정](#4-vm-인스턴스-접속-및-초기-설정)
5. [프로젝트 배포](#5-프로젝트-배포)
6. [프론트엔드 연결 설정](#6-프론트엔드-연결-설정)
7. [트러블슈팅](#7-트러블슈팅)
8. [부록: Cloud Run 배포 (선택사항)](#8-부록-cloud-run-배포-선택사항)

---

## 1. GCP 계정 생성 및 초기 설정

### 1.1 GCP 무료 체험 시작

1. [Google Cloud](https://cloud.google.com/free) 접속
2. **무료로 시작하기** 클릭
3. Google 계정으로 로그인
4. 국가 선택: **대한민국**
5. 약관 동의 후 계속

### 1.2 결제 정보 입력

```
카드 정보 입력 필요 (무료 체험 기간 동안 자동 청구되지 않음)
- $300 무료 크레딧 제공 (90일간 유효)
- 무료 등급(Free Tier) 제품은 크레딧 소진 후에도 무료 사용 가능
```

**중요: 학생이라면 Google Cloud 교육 크레딧 신청 가능**
- [Google Cloud 학생 프로그램](https://edu.google.com/programs/students/)
- 추가 $50-$100 크레딧 제공

### 1.3 프로젝트 생성

1. GCP Console 상단의 프로젝트 선택 드롭다운 클릭
2. **새 프로젝트** 클릭
3. 프로젝트 정보 입력:
   ```
   프로젝트 이름: feel-backend
   프로젝트 ID: feel-backend-XXXXXX (자동 생성 또는 커스텀)
   위치: 조직 없음
   ```
4. **만들기** 클릭

---

## 2. Compute Engine VM 인스턴스 생성

### 2.1 Compute Engine API 활성화

1. 좌측 메뉴 > **Compute Engine** > **VM 인스턴스** 클릭
2. Compute Engine API 활성화 (처음 사용 시 자동 안내)
3. 1-2분 대기

### 2.2 VM 인스턴스 만들기

**인스턴스 만들기** 버튼 클릭 후 다음과 같이 설정:

#### 기본 설정
```
이름: feel-backend-vm
리전: asia-northeast3 (서울)
영역: asia-northeast3-a
```

#### 머신 구성
```
시리즈: E2
머신 유형: e2-micro (무료 등급)
  - vCPU: 2개 (공유)
  - 메모리: 1GB
  - 월 예상 비용: 무료 (730시간/월 무료 제공)
```

**무료 등급 안내:**
- 미국 리전(us-west1, us-central1, us-east1)에서 e2-micro 사용 시 완전 무료
- 서울 리전 사용 시 일부 비용 발생 가능 (약 $7-8/월)
- 비용 절감을 원하면 **us-central1** 리전 선택 권장

#### 부팅 디스크
```
운영체제: Debian
버전: Debian GNU/Linux 11 (bullseye)
부팅 디스크 유형: 표준 영구 디스크
크기: 30GB (무료 등급: 30GB까지 무료)
```

**변경** 버튼 클릭하여 설정 후 **선택** 클릭

#### ID 및 API 액세스
```
서비스 계정: Compute Engine 기본 서비스 계정
액세스 범위: 모든 Cloud API에 대한 전체 액세스 허용
```

#### 방화벽
```
☑ HTTP 트래픽 허용
☐ HTTPS 트래픽 허용 (선택사항)
```

### 2.3 인스턴스 생성

1. **만들기** 버튼 클릭
2. 인스턴스 생성 대기 (약 30초~1분)
3. **외부 IP** 주소 확인 및 메모 (예: 34.64.123.456)

---

## 3. 방화벽 규칙 설정

### 3.1 백엔드 API 포트(8080) 개방

1. 좌측 메뉴 > **VPC 네트워크** > **방화벽** 클릭
2. **방화벽 규칙 만들기** 클릭

#### 규칙 1: Backend API (포트 8080)
```
이름: allow-backend-8080
로그: 사용 중지됨
네트워크: default
우선순위: 1000
트래픽 방향: 수신
일치 시 작업: 허용
대상: 네트워크의 모든 인스턴스
소스 필터: IPv4 범위
소스 IPv4 범위: 0.0.0.0/0
프로토콜 및 포트:
  ☑ 지정된 프로토콜 및 포트
  ☑ tcp: 8080
```

**만들기** 클릭

#### 규칙 2: MySQL (포트 3307) - 선택사항
```
이름: allow-mysql-3307
로그: 사용 중지됨
네트워크: default
우선순위: 1000
트래픽 방향: 수신
일치 시 작업: 허용
대상: 네트워크의 모든 인스턴스
소스 필터: IPv4 범위
소스 IPv4 범위: 0.0.0.0/0
프로토콜 및 포트:
  ☑ 지정된 프로토콜 및 포트
  ☑ tcp: 3307
```

**만들기** 클릭

### 3.2 방화벽 규칙 확인

```
방화벽 페이지에서 다음 규칙들이 생성되었는지 확인:
- allow-backend-8080
- allow-mysql-3307 (선택사항)
- default-allow-http (기본 규칙)
- default-allow-ssh (기본 규칙)
```

---

## 4. VM 인스턴스 접속 및 초기 설정

### 4.1 SSH 접속

#### 방법 1: 브라우저 SSH (가장 간단)

1. **Compute Engine** > **VM 인스턴스** 페이지
2. 인스턴스 목록에서 **SSH** 버튼 클릭
3. 브라우저에서 터미널 창 자동 열림

#### 방법 2: 로컬 SSH 클라이언트 (Windows/Mac/Linux)

**SSH 키 등록:**
1. 로컬에서 SSH 키 생성 (이미 있으면 skip)
   ```bash
   # Windows Git Bash / Mac / Linux
   ssh-keygen -t rsa -f ~/.ssh/gcp-feel-backend -C "your-email@example.com"
   ```

2. 공개 키 복사
   ```bash
   cat ~/.ssh/gcp-feel-backend.pub
   ```

3. GCP Console에서 SSH 키 등록
   - **Compute Engine** > **메타데이터** > **SSH 키** 탭
   - **수정** 클릭 > **항목 추가** > 공개 키 붙여넣기 > **저장**

4. SSH 접속
   ```bash
   ssh -i ~/.ssh/gcp-feel-backend your-username@EXTERNAL_IP
   ```

### 4.2 시스템 초기 설정

SSH 접속 후 실행:

```bash
# 시스템 업데이트
sudo apt-get update -y
sudo apt-get upgrade -y

# 필수 패키지 설치
sudo apt-get install -y git curl wget nano
```

---

## 5. 프로젝트 배포

### 5.1 프로젝트 파일 업로드

#### 방법 1: Git Clone (추천)

```bash
# GitHub에 프로젝트가 있는 경우
cd ~
git clone https://github.com/YOUR_USERNAME/FeeL_Backend.git
cd FeeL_Backend
```

#### 방법 2: gcloud CLI 사용

**로컬 컴퓨터에서:**

1. gcloud CLI 설치
   - Windows: https://cloud.google.com/sdk/docs/install
   - Mac: `brew install google-cloud-sdk`
   - Linux: `curl https://sdk.cloud.google.com | bash`

2. gcloud 초기화
   ```bash
   gcloud init
   gcloud auth login
   gcloud config set project feel-backend
   ```

3. 파일 업로드
   ```bash
   cd C:/Users/scheoleon/workspace/FeeL_Backend
   gcloud compute scp --recurse ./* feel-backend-vm:~/FeeL_Backend/ --zone=asia-northeast3-a
   ```

#### 방법 3: SCP 직접 사용

```bash
# Windows Git Bash
cd /c/Users/scheoleon/workspace/FeeL_Backend
scp -i ~/.ssh/gcp-feel-backend -r ./* your-username@EXTERNAL_IP:~/FeeL_Backend/

# Mac/Linux
cd ~/workspace/FeeL_Backend
scp -i ~/.ssh/gcp-feel-backend -r ./* your-username@EXTERNAL_IP:~/FeeL_Backend/
```

### 5.2 환경 변수 설정

VM 인스턴스에서 실행:

```bash
cd ~/FeeL_Backend

# .env 파일 생성
cp .env.gcp .env

# 환경 변수 편집
nano .env
```

`.env` 파일 내용:
```bash
DB_USERNAME=feel
DB_PASSWORD=YourSecurePasswordHere123!  # 강력한 비밀번호로 변경!
SPRING_PROFILES_ACTIVE=prod
```

저장: `Ctrl + O` → Enter → `Ctrl + X`

### 5.3 배포 스크립트 실행

```bash
# 스크립트 실행 권한 부여
chmod +x deploy-gcp.sh

# 배포 실행
./deploy-gcp.sh
```

배포 스크립트가 자동으로 수행하는 작업:
1. ✓ Docker 및 Docker Compose 설치
2. ✓ 시스템 패키지 업데이트
3. ✓ 애플리케이션 디렉토리 설정
4. ✓ .env 파일 검증
5. ✓ Docker Compose로 백엔드 + MySQL 시작

### 5.4 배포 확인

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

### 5.5 API 테스트

**VM에서 테스트:**
```bash
curl http://localhost:8080/actuator/health

# 예상 응답:
# {"status":"UP"}
```

**로컬 컴퓨터 브라우저에서 테스트:**
```
http://EXTERNAL_IP:8080/actuator/health
http://EXTERNAL_IP:8080/api/notices
```

---

## 6. 프론트엔드 연결 설정

### 6.1 외부 IP 확인

GCP Console에서 또는 다음 명령어로 확인:
```bash
# VM에서 실행
curl -H "Metadata-Flavor: Google" \
  http://metadata.google.internal/computeMetadata/v1/instance/network-interfaces/0/access-configs/0/external-ip
```

### 6.2 백엔드 CORS 설정 업데이트

`src/main/java/com/feel/backend/config/WebConfig.java` 수정:

```java
@Override
public void addCorsMappings(CorsRegistry registry) {
    registry.addMapping("/api/**")
        .allowedOrigins(
            "http://localhost:3000",
            "https://m-se0k.github.io",
            "https://m-se0k.github.io/FeeL_WEB",
            "http://34.64.189.241:3000"  // GCP 외부 IP 추가
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
            "http://YOUR_EXTERNAL_IP:3000"  // GCP 외부 IP 추가
        )
        .allowedMethods("GET", "OPTIONS")
        .allowedHeaders("*")
        .allowCredentials(true)
        .maxAge(3600);
}
```

**재배포:**
```bash
cd ~/FeeL_Backend
git pull  # Git 사용 시
docker-compose down
docker-compose up -d --build
```

### 6.3 프론트엔드 환경 변수 설정

프론트엔드 `.env.production` 파일:
```javascript
REACT_APP_API_URL=http://YOUR_EXTERNAL_IP:8080
```

---

## 7. 트러블슈팅

### 문제 1: 외부에서 API 접속 안됨

**확인 사항:**
1. 방화벽 규칙 확인
   ```bash
   # GCP Console > VPC 네트워크 > 방화벽 규칙
   # allow-backend-8080 규칙 확인
   ```

2. 컨테이너 실행 상태 확인
   ```bash
   docker-compose ps
   docker-compose logs backend
   ```

3. 포트 리스닝 확인
   ```bash
   sudo netstat -tlnp | grep 8080
   # 또는
   sudo ss -tlnp | grep 8080
   ```

### 문제 2: 컨테이너 시작 실패

```bash
# 로그 확인
docker-compose logs backend

# 컨테이너 재시작
docker-compose restart backend

# 완전 재배포
docker-compose down -v
docker-compose up -d --build
```

### 문제 3: MySQL 연결 오류

```bash
# MySQL 로그 확인
docker-compose logs db

# MySQL 컨테이너 접속
docker exec -it feel-mysql mysql -u feel -p
# .env에 설정한 비밀번호 입력

# 데이터베이스 확인
SHOW DATABASES;
USE feeldb;
SHOW TABLES;
```

### 문제 4: Out of Memory (메모리 부족)

e2-micro는 1GB RAM만 제공하므로 메모리 부족 가능:

```bash
# 메모리 사용량 확인
free -h

# Swap 메모리 생성 (2GB)
sudo fallocate -l 2G /swapfile
sudo chmod 600 /swapfile
sudo mkswap /swapfile
sudo swapon /swapfile

# 영구 적용
echo '/swapfile none swap sw 0 0' | sudo tee -a /etc/fstab

# Swap 확인
free -h
```

### 문제 5: Docker 권한 오류

```bash
# 현재 사용자를 docker 그룹에 추가
sudo usermod -aG docker $USER

# 로그아웃 후 재로그인
exit
# SSH 재접속

# Docker 명령어 확인
docker ps
```

### 문제 6: 디스크 공간 부족

```bash
# 디스크 사용량 확인
df -h

# Docker 이미지 및 컨테이너 정리
docker system prune -a

# 불필요한 패키지 정리
sudo apt-get autoremove -y
sudo apt-get clean
```

---

## 8. 부록: Cloud Run 배포 (선택사항)

서버리스 방식으로 배포하고 싶다면 **Cloud Run** 사용 가능:

### 8.1 Cloud Run 장점
- 자동 스케일링 (트래픽에 따라 0→N 인스턴스)
- 사용한 만큼만 과금
- HTTPS 자동 제공
- 관리 부담 적음

### 8.2 Cloud Run 배포 준비

**Dockerfile이 이미 있으므로 바로 배포 가능**

#### 1. Cloud Build API 활성화
```bash
gcloud services enable cloudbuild.googleapis.com
gcloud services enable run.googleapis.com
```

#### 2. 컨테이너 이미지 빌드
```bash
cd ~/FeeL_Backend

# Container Registry에 이미지 빌드 및 푸시
gcloud builds submit --tag gcr.io/PROJECT_ID/feel-backend

# 예시:
gcloud builds submit --tag gcr.io/feel-backend-123456/feel-backend
```

#### 3. Cloud Run 배포
```bash
gcloud run deploy feel-backend \
  --image gcr.io/PROJECT_ID/feel-backend \
  --platform managed \
  --region asia-northeast3 \
  --allow-unauthenticated \
  --set-env-vars="SPRING_PROFILES_ACTIVE=prod,DB_USERNAME=feel,DB_PASSWORD=YOUR_PASSWORD"
```

#### 4. Cloud SQL 연동 (MySQL)
```bash
# Cloud SQL 인스턴스 생성
gcloud sql instances create feel-mysql \
  --database-version=MYSQL_8_0 \
  --tier=db-f1-micro \
  --region=asia-northeast3

# Cloud Run에서 Cloud SQL 연결
gcloud run services update feel-backend \
  --add-cloudsql-instances PROJECT_ID:asia-northeast3:feel-mysql
```

**주의:** Cloud SQL은 무료 등급이 아니므로 비용 발생

---

## 유용한 명령어 모음

```bash
# === Docker 관련 ===
docker-compose ps                    # 컨테이너 상태 확인
docker-compose logs -f               # 실시간 로그 확인
docker-compose logs -f backend       # 백엔드 로그만 확인
docker-compose restart               # 서비스 재시작
docker-compose down                  # 서비스 중지
docker-compose up -d                 # 서비스 시작
docker-compose down -v               # 볼륨 포함 완전 삭제

# === 시스템 모니터링 ===
free -h                              # 메모리 사용량
df -h                                # 디스크 사용량
top                                  # 프로세스 모니터링
htop                                 # 고급 프로세스 모니터링 (설치 필요)

# === GCP 관련 ===
gcloud compute instances list        # VM 인스턴스 목록
gcloud compute firewall-rules list   # 방화벽 규칙 목록
gcloud compute ssh feel-backend-vm   # SSH 접속

# === 네트워크 ===
curl http://localhost:8080/actuator/health    # 로컬 헬스체크
netstat -tlnp | grep 8080                     # 포트 리스닝 확인
```

---

## 비용 관리

### GCP 무료 등급 제공 내역

**항상 무료:**
- e2-micro (US 리전만): 730시간/월
- 30GB 표준 영구 디스크
- 1GB 네트워크 송신 (북미 → 전 세계, 중국/호주 제외)

**90일 $300 크레딧:**
- 모든 GCP 서비스 사용 가능
- 크레딧 소진 전 알림

### 비용 절감 팁

1. **US 리전 사용** (완전 무료)
   ```
   리전: us-central1, us-west1, us-east1
   ```

2. **예산 알림 설정**
   - GCP Console > 결제 > 예산 및 알림
   - 월 예산 $10 설정, 50%, 90%, 100% 알림

3. **VM 자동 종료 스케줄 설정**
   ```bash
   # 매일 새벽 2시~8시 VM 중지 (선택사항)
   gcloud compute instances stop feel-backend-vm --zone=asia-northeast3-a
   ```

4. **사용하지 않는 리소스 삭제**
   ```bash
   # 불필요한 디스크, 스냅샷, 로드밸런서 정기 확인
   ```

---

## 보안 권장사항

### 1. SSH 키 관리
```bash
# SSH 키 정기 교체
ssh-keygen -t rsa -f ~/.ssh/gcp-feel-backend-new -C "your-email@example.com"
# GCP Console에서 기존 키 삭제 후 새 키 등록
```

### 2. 방화벽 소스 IP 제한
```bash
# 특정 IP만 허용 (선택사항)
# VPC 네트워크 > 방화벽 > allow-backend-8080 수정
# 소스 IPv4 범위: 0.0.0.0/0 → YOUR_IP/32
```

### 3. 데이터베이스 비밀번호 강화
```bash
# .env 파일 비밀번호
# - 최소 16자
# - 영문 대소문자, 숫자, 특수문자 혼합
# 예: DB_PASSWORD=F3eL!S3cur3P@ssw0rd2024
```

### 4. 정기 업데이트
```bash
# 시스템 업데이트
sudo apt-get update -y
sudo apt-get upgrade -y

# Docker 이미지 업데이트
docker-compose pull
docker-compose up -d --build
```

### 5. 백업
```bash
# 데이터베이스 백업
docker exec feel-mysql mysqldump -u feel -p feeldb > backup_$(date +%Y%m%d).sql

# GCP Storage에 백업 업로드 (선택사항)
gsutil cp backup_*.sql gs://YOUR_BUCKET_NAME/backups/
```

---

## HTTPS 설정 (Let's Encrypt)

### 1. 도메인 연결

**무료 도메인 획득:**
- Freenom: https://www.freenom.com
- Google Domains (유료, 연 $12)

**DNS 설정:**
```
Type: A
Name: @
Value: YOUR_EXTERNAL_IP
TTL: 3600
```

### 2. Nginx 리버스 프록시 설치

```bash
# Nginx 설치
sudo apt-get install -y nginx

# Nginx 설정
sudo nano /etc/nginx/sites-available/feel-backend
```

설정 파일 내용:
```nginx
server {
    listen 80;
    server_name yourdomain.com;

    location / {
        proxy_pass http://localhost:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }
}
```

```bash
# 심볼릭 링크 생성
sudo ln -s /etc/nginx/sites-available/feel-backend /etc/nginx/sites-enabled/

# Nginx 재시작
sudo systemctl restart nginx
```

### 3. SSL 인증서 발급

```bash
# Certbot 설치
sudo apt-get install -y certbot python3-certbot-nginx

# SSL 인증서 발급
sudo certbot --nginx -d yourdomain.com

# 자동 갱신 설정 (이미 자동 설정됨)
sudo certbot renew --dry-run
```

---

## 참고 자료

- [Google Cloud 공식 문서](https://cloud.google.com/docs)
- [Compute Engine 가이드](https://cloud.google.com/compute/docs)
- [GCP 무료 등급](https://cloud.google.com/free/docs/gcp-free-tier)
- [Docker 공식 문서](https://docs.docker.com/)
- [Spring Boot 배포 가이드](https://spring.io/guides/gs/spring-boot-docker/)

---

## 문제 발생 시 연락처

- GitHub Issues: [FeeL_Backend Issues](https://github.com/YOUR_USERNAME/FeeL_Backend/issues)
- GCP 지원: https://cloud.google.com/support
- 전북대 FeeL 학생회 이메일: feel@jbnu.ac.kr

---

## 배포 완료 체크리스트

배포 완료 후 다음 항목들을 확인하세요:

- [ ] GCP 프로젝트 생성 완료
- [ ] VM 인스턴스 생성 및 실행 중
- [ ] 방화벽 규칙 설정 (포트 8080, 3307)
- [ ] SSH 접속 확인
- [ ] Docker 및 Docker Compose 설치
- [ ] 프로젝트 파일 업로드
- [ ] .env 파일 설정 (DB 비밀번호 변경)
- [ ] 배포 스크립트 실행 성공
- [ ] 컨테이너 정상 실행 (`docker-compose ps`)
- [ ] API 엔드포인트 접속 확인
- [ ] 프론트엔드 연동 테스트
- [ ] CORS 설정 확인
- [ ] 이미지 업로드 기능 테스트
- [ ] 예산 알림 설정
- [ ] 백업 전략 수립

---

**축하합니다! 🎉 FeeL 백엔드가 GCP에 성공적으로 배포되었습니다!**

배포 URL: `http://YOUR_EXTERNAL_IP:8080`

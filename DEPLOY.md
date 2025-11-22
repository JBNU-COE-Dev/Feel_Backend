# Google Cloud 재배포 가이드

Google Cloud에서 Docker로 배포 중인 FeeL Backend 서버를 재배포하는 방법을 안내합니다.

## 재배포 프로세스

### **방법 1: Git을 통한 배포 (권장)**

#### 1단계: 로컬에서 변경사항 커밋 및 푸시
```bash
# 로컬에서 변경사항 커밋
git add .
git commit -m "변경사항 설명"
git push origin main
```

#### 2단계: GCP VM에 SSH 접속
```bash
# GCP Console에서 SSH 버튼 클릭하거나
gcloud compute ssh [인스턴스명] --zone=[존]

# 또는 로컬에서 직접 SSH
ssh -i [키파일] [사용자명]@[외부IP]
```

#### 3단계: 서버에서 코드 업데이트 및 재배포
```bash
# 프로젝트 디렉토리로 이동
cd ~/FeeL_Backend  # 또는 실제 설치 경로

# 최신 코드 받기
git pull origin main

# Docker 컨테이너 재빌드 및 재시작
docker-compose down
docker-compose up -d --build

# 로그 확인
docker-compose logs -f backend
```

---

### **방법 2: 빠른 재시작 (코드 변경 없이 설정만 변경한 경우)**

```bash
# SSH로 서버 접속 후
cd ~/FeeL_Backend

# 재시작만
docker-compose restart backend

# 또는 완전히 다시 시작
docker-compose down
docker-compose up -d
```

---

### **방법 3: 완전 클린 배포 (캐시 없이 처음부터)**

```bash
# SSH로 서버 접속 후
cd ~/FeeL_Backend

# 모든 컨테이너와 볼륨 삭제
docker-compose down -v

# 이미지까지 삭제
docker system prune -af

# 최신 코드 받기
git pull origin main

# 처음부터 빌드
docker-compose up -d --build --force-recreate

# 로그 확인
docker-compose logs -f backend
```

---

### **자동화된 배포 스크립트 사용**

프로젝트에 이미 `deploy-gcp.sh` 스크립트가 있으므로:

```bash
# SSH로 서버 접속 후
cd ~/FeeL_Backend
git pull origin main
bash deploy-gcp.sh
```

---

## 배포 후 확인사항

```bash
# 1. 컨테이너 상태 확인
docker-compose ps

# 2. 로그 확인
docker-compose logs -f backend

# 3. Health Check
curl http://localhost:8080/actuator/health

# 4. API 테스트
curl http://localhost:8080/api/notices
```

---

## 주의사항

### 1. 데이터베이스 백업
변경사항이 DB 스키마에 영향을 주는 경우 먼저 백업하세요.
```bash
docker exec feel-mysql mysqldump -u feel -p feeldb > backup_$(date +%Y%m%d).sql
```

### 2. 환경 변수 확인
`.env` 파일이 최신 상태이고 올바른 값을 가지고 있는지 확인하세요.

### 3. 다운타임 최소화
Blue-Green 배포를 원하면 추가 설정이 필요합니다.

### 4. SSL 인증서
Nginx + Certbot 사용 중이므로 인증서 갱신 상태를 확인하세요.

---

## 트러블슈팅

### 컨테이너가 시작되지 않는 경우
```bash
# 상세 로그 확인
docker-compose logs backend

# 컨테이너 상태 확인
docker ps -a

# 특정 컨테이너 로그
docker logs feel-backend
```

### 포트 충돌
```bash
# 포트 사용 확인
sudo netstat -tulpn | grep :8080

# 기존 프로세스 종료 후 재시작
docker-compose down
docker-compose up -d
```

### 빌드 캐시 문제
```bash
# 캐시 없이 빌드
docker-compose build --no-cache
docker-compose up -d
```

---

## 빠른 참조

| 작업 | 명령어 |
|------|--------|
| 로그 실시간 보기 | `docker-compose logs -f backend` |
| 서비스 재시작 | `docker-compose restart backend` |
| 전체 중지 | `docker-compose down` |
| 볼륨까지 삭제 | `docker-compose down -v` |
| 컨테이너 접속 | `docker exec -it feel-backend bash` |
| MySQL 접속 | `docker exec -it feel-mysql mysql -u feel -p` |

# Docker 배포 명령어 가이드

FeeL 백엔드 서버의 Docker 배포를 위한 명령어 모음입니다.

## 목차
- [초기 설정](#초기-설정)
- [서비스 실행](#서비스-실행)
- [서비스 관리](#서비스-관리)
- [로그 확인](#로그-확인)
- [데이터베이스 접근](#데이터베이스-접근)
- [문제 해결](#문제-해결)
- [유지보수](#유지보수)

---

## 초기 설정

### 1. 환경 변수 파일 생성

```bash
# .env.example을 .env로 복사
cp .env.example .env
```

### 2. 환경 변수 설정

`.env` 파일을 편집하여 데이터베이스 인증 정보를 설정합니다:

```bash
# .env 파일 내용
DB_USERNAME=feel
DB_PASSWORD=your_secure_password_here
SPRING_PROFILES_ACTIVE=prod
```

---

## 서비스 실행

### 전체 서비스 시작 (백엔드 + MySQL)

```bash
# 백그라운드에서 실행
docker-compose up -d

# 포그라운드에서 실행 (로그 실시간 확인)
docker-compose up
```

### 특정 서비스만 시작

```bash
# MySQL만 시작
docker-compose up -d db

# 백엔드만 시작
docker-compose up -d backend
```

### 이미지 재빌드 후 시작

```bash
# 캐시 무시하고 완전히 새로 빌드
docker-compose up -d --build

# 특정 서비스만 재빌드
docker-compose up -d --build backend
```

---

## 서비스 관리

### 서비스 상태 확인

```bash
# 실행 중인 컨테이너 확인
docker-compose ps

# 모든 컨테이너 확인 (중지된 것 포함)
docker-compose ps -a
```

### 서비스 중지

```bash
# 서비스 중지 (컨테이너는 유지)
docker-compose stop

# 특정 서비스만 중지
docker-compose stop backend
```

### 서비스 시작 (중지된 컨테이너 재시작)

```bash
# 모든 서비스 시작
docker-compose start

# 특정 서비스만 시작
docker-compose start backend
```

### 서비스 재시작

```bash
# 모든 서비스 재시작
docker-compose restart

# 특정 서비스만 재시작
docker-compose restart backend
```

### 서비스 종료 및 제거

```bash
# 컨테이너 중지 및 삭제 (볼륨은 유지)
docker-compose down

# 컨테이너, 네트워크, 볼륨 모두 삭제
docker-compose down -v

# 컨테이너, 네트워크, 이미지 모두 삭제
docker-compose down --rmi all
```

---

## 로그 확인

### 실시간 로그 보기

```bash
# 모든 서비스 로그
docker-compose logs -f

# 특정 서비스 로그
docker-compose logs -f backend
docker-compose logs -f db

# 최근 100줄만 보기
docker-compose logs --tail=100 backend
```

### 특정 시점 이후 로그 보기

```bash
# 최근 10분간의 로그
docker-compose logs --since 10m backend

# 특정 시간 이후의 로그
docker-compose logs --since 2024-01-01T00:00:00 backend
```

### Docker 명령어로 로그 확인

```bash
# 컨테이너 이름으로 직접 접근
docker logs feel-backend
docker logs feel-mysql

# 실시간 로그
docker logs -f feel-backend

# 최근 100줄
docker logs --tail=100 feel-backend
```

---

## 데이터베이스 접근

### MySQL 컨테이너 접속

```bash
# MySQL CLI 접속 (비밀번호 입력 필요)
docker exec -it feel-mysql mysql -u feel -p

# root 계정으로 접속
docker exec -it feel-mysql mysql -u root -p
```

### MySQL 명령어 직접 실행

```bash
# 데이터베이스 목록 확인
docker exec feel-mysql mysql -u feel -p${DB_PASSWORD} -e "SHOW DATABASES;"

# 테이블 목록 확인
docker exec feel-mysql mysql -u feel -p${DB_PASSWORD} feeldb -e "SHOW TABLES;"

# 공지사항 개수 확인
docker exec feel-mysql mysql -u feel -p${DB_PASSWORD} feeldb -e "SELECT COUNT(*) FROM notice;"
```

### 데이터베이스 백업

```bash
# 전체 데이터베이스 백업
docker exec feel-mysql mysqldump -u feel -p${DB_PASSWORD} feeldb > backup_$(date +%Y%m%d).sql

# 특정 테이블만 백업
docker exec feel-mysql mysqldump -u feel -p${DB_PASSWORD} feeldb notice > notice_backup.sql
```

### 데이터베이스 복원

```bash
# 백업 파일로부터 복원
docker exec -i feel-mysql mysql -u feel -p${DB_PASSWORD} feeldb < backup_20240101.sql
```

---

## 문제 해결

### 컨테이너 셸 접속

```bash
# 백엔드 컨테이너 bash 접속
docker exec -it feel-backend bash

# MySQL 컨테이너 bash 접속
docker exec -it feel-mysql bash
```

### 컨테이너 상세 정보 확인

```bash
# 컨테이너 상세 정보
docker inspect feel-backend
docker inspect feel-mysql

# IP 주소 확인
docker inspect -f '{{range .NetworkSettings.Networks}}{{.IPAddress}}{{end}}' feel-backend
```

### 네트워크 확인

```bash
# 네트워크 목록
docker network ls

# feel-network 상세 정보
docker network inspect feel_feel-network
```

### 볼륨 확인

```bash
# 볼륨 목록
docker volume ls

# 볼륨 상세 정보
docker volume inspect feel_mysql-data
```

### 포트 충돌 해결

```bash
# 포트 사용 중인 프로세스 확인 (Windows)
netstat -ano | findstr :8080
netstat -ano | findstr :3307

# 포트 사용 중인 프로세스 확인 (Linux/Mac)
lsof -i :8080
lsof -i :3307
```

### 캐시 정리 및 재빌드

```bash
# Docker 빌드 캐시 정리
docker builder prune

# 사용하지 않는 이미지 삭제
docker image prune -a

# 모든 정지된 컨테이너, 네트워크, 이미지 정리
docker system prune -a

# 볼륨까지 모두 정리 (주의: 데이터 손실!)
docker system prune -a --volumes
```

---

## 유지보수

### 서비스 리소스 사용량 확인

```bash
# 실시간 리소스 사용량
docker stats

# 특정 컨테이너만 확인
docker stats feel-backend feel-mysql
```

### 이미지 업데이트

```bash
# 최신 MySQL 이미지 pull
docker pull mysql:8.0

# 백엔드 이미지 재빌드 후 재시작
docker-compose up -d --build backend
```

### 데이터베이스 볼륨 백업

```bash
# 볼륨 백업 (tar 파일로)
docker run --rm -v feel_mysql-data:/data -v $(pwd):/backup alpine tar czf /backup/mysql-data-backup.tar.gz -C /data .

# 볼륨 복원
docker run --rm -v feel_mysql-data:/data -v $(pwd):/backup alpine tar xzf /backup/mysql-data-backup.tar.gz -C /data
```

### 로그 파일 크기 관리

```bash
# 로그 파일 크기 확인
docker inspect --format='{{.LogPath}}' feel-backend

# 로그 파일 정리 (컨테이너 재시작 필요)
truncate -s 0 $(docker inspect --format='{{.LogPath}}' feel-backend)
```

---

## 배포 체크리스트

1. **환경 설정 확인**
   ```bash
   cat .env
   ```

2. **이미지 빌드**
   ```bash
   docker-compose build
   ```

3. **서비스 시작**
   ```bash
   docker-compose up -d
   ```

4. **상태 확인**
   ```bash
   docker-compose ps
   ```

5. **로그 확인**
   ```bash
   docker-compose logs -f
   ```

6. **데이터베이스 연결 테스트**
   ```bash
   docker exec feel-mysql mysql -u feel -p${DB_PASSWORD} -e "SELECT 1;"
   ```

7. **API 테스트**
   ```bash
   curl http://localhost:8080/api/notices
   ```

---

## 참고 사항

- **포트 매핑**: 백엔드(8080), MySQL(3307→3306)
- **네트워크**: `feel-network` (bridge)
- **볼륨**: `mysql-data` (데이터 영구 저장)
- **재시작 정책**: `unless-stopped`

## 관련 문서

- [CLAUDE.md](../CLAUDE.md) - 프로젝트 전체 가이드
- [docker-compose.yml](../docker-compose.yml) - Docker Compose 설정 파일
- [.env.example](../.env.example) - 환경 변수 예시

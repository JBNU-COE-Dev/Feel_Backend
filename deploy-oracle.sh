#!/bin/bash

# Oracle Cloud Free Tier 배포 스크립트
# FeeL Backend 배포 자동화

set -e

echo "=========================================="
echo "FeeL Backend - Oracle Cloud 배포 시작"
echo "=========================================="

# 컬러 출력
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# 1. 시스템 업데이트
echo -e "${YELLOW}[1/8] 시스템 패키지 업데이트...${NC}"
sudo yum update -y

# 2. Docker 설치
echo -e "${YELLOW}[2/8] Docker 설치...${NC}"
if ! command -v docker &> /dev/null; then
    sudo yum install -y yum-utils
    sudo yum-config-manager --add-repo https://download.docker.com/linux/centos/docker-ce.repo
    sudo yum install -y docker-ce docker-ce-cli containerd.io
    sudo systemctl start docker
    sudo systemctl enable docker
    sudo usermod -aG docker $USER
    echo -e "${GREEN}Docker 설치 완료${NC}"
else
    echo -e "${GREEN}Docker 이미 설치됨${NC}"
fi

# 3. Docker Compose 설치
echo -e "${YELLOW}[3/8] Docker Compose 설치...${NC}"
if ! command -v docker-compose &> /dev/null; then
    sudo curl -L "https://github.com/docker/compose/releases/latest/download/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
    sudo chmod +x /usr/local/bin/docker-compose
    echo -e "${GREEN}Docker Compose 설치 완료${NC}"
else
    echo -e "${GREEN}Docker Compose 이미 설치됨${NC}"
fi

# 4. 방화벽 설정
echo -e "${YELLOW}[4/8] 방화벽 포트 개방...${NC}"
sudo firewall-cmd --permanent --add-port=8080/tcp
sudo firewall-cmd --permanent --add-port=3307/tcp
sudo firewall-cmd --reload
echo -e "${GREEN}포트 8080, 3307 개방 완료${NC}"

# 5. 애플리케이션 디렉토리 생성
echo -e "${YELLOW}[5/8] 애플리케이션 디렉토리 설정...${NC}"
APP_DIR="/home/opc/feel-backend"
mkdir -p $APP_DIR
cd $APP_DIR

# 6. .env 파일 확인
echo -e "${YELLOW}[6/8] 환경 변수 설정 확인...${NC}"
if [ ! -f .env ]; then
    echo -e "${RED}ERROR: .env 파일이 없습니다.${NC}"
    echo -e "${YELLOW}.env.example을 복사하여 .env 파일을 생성하고 데이터베이스 비밀번호를 설정하세요.${NC}"
    echo -e "${YELLOW}예시: cp .env.example .env && nano .env${NC}"
    exit 1
fi
echo -e "${GREEN}.env 파일 확인 완료${NC}"

# 7. 기존 컨테이너 중지 및 제거
echo -e "${YELLOW}[7/8] 기존 컨테이너 정리...${NC}"
if [ "$(docker ps -a -q)" ]; then
    docker-compose down -v 2>/dev/null || true
fi

# 8. 애플리케이션 시작
echo -e "${YELLOW}[8/8] 애플리케이션 시작...${NC}"
docker-compose up -d --build

# 대기
echo -e "${YELLOW}서비스 시작 대기 중 (30초)...${NC}"
sleep 30

# 상태 확인
echo ""
echo "=========================================="
echo -e "${GREEN}배포 완료!${NC}"
echo "=========================================="
docker-compose ps

echo ""
echo "접속 정보:"
echo "- Backend API: http://$(curl -s ifconfig.me):8080"
echo "- Health Check: http://$(curl -s ifconfig.me):8080/actuator/health"
echo ""
echo "로그 확인: docker-compose logs -f backend"
echo "서비스 중지: docker-compose down"
echo "서비스 재시작: docker-compose restart"

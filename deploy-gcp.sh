#!/bin/bash

# Google Cloud Platform 배포 스크립트
# FeeL Backend 배포 자동화 (Compute Engine)

set -e

echo "=========================================="
echo "FeeL Backend - Google Cloud 배포 시작"
echo "=========================================="

# 컬러 출력
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# 1. 시스템 업데이트
echo -e "${YELLOW}[1/8] 시스템 패키지 업데이트...${NC}"
sudo apt-get update -y
sudo apt-get upgrade -y

# 2. Docker 설치
echo -e "${YELLOW}[2/8] Docker 설치...${NC}"
if ! command -v docker &> /dev/null; then
    # Docker 설치
    sudo apt-get install -y ca-certificates curl gnupg lsb-release
    sudo mkdir -p /etc/apt/keyrings
    curl -fsSL https://download.docker.com/linux/debian/gpg | sudo gpg --dearmor -o /etc/apt/keyrings/docker.gpg
    echo \
      "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.gpg] https://download.docker.com/linux/debian \
      $(lsb_release -cs) stable" | sudo tee /etc/apt/sources.list.d/docker.list > /dev/null
    sudo apt-get update -y
    sudo apt-get install -y docker-ce docker-ce-cli containerd.io docker-compose-plugin

    # 현재 사용자를 docker 그룹에 추가
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

# 4. 방화벽 설정 (GCP는 Firewall Rules로 관리)
echo -e "${YELLOW}[4/8] 방화벽 확인...${NC}"
echo -e "${YELLOW}GCP Console에서 Firewall Rules 설정이 필요합니다.${NC}"
echo -e "${YELLOW}포트 8080, 3307 개방 확인${NC}"

# 5. 애플리케이션 디렉토리 설정
echo -e "${YELLOW}[5/8] 애플리케이션 디렉토리 설정...${NC}"

# 스크립트가 실행되는 현재 디렉토리를 APP_DIR로 설정
SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
APP_DIR="$SCRIPT_DIR"

echo -e "${GREEN}작업 디렉토리: $APP_DIR${NC}"
cd "$APP_DIR"

# 6. .env 파일 확인
echo -e "${YELLOW}[6/8] 환경 변수 설정 확인...${NC}"
echo -e "${YELLOW}현재 디렉토리: $(pwd)${NC}"
echo -e "${YELLOW}.env 파일 검색 중...${NC}"

if [ ! -f .env ]; then
    echo -e "${RED}ERROR: .env 파일이 없습니다.${NC}"
    echo -e "${YELLOW}현재 디렉토리: $(pwd)${NC}"
    echo -e "${YELLOW}파일 목록:${NC}"
    ls -la .env* 2>/dev/null || echo "  .env* 파일이 없습니다."
    echo ""
    echo -e "${YELLOW}다음 중 하나를 실행하세요:${NC}"

    if [ -f .env.gcp ]; then
        echo -e "${GREEN}  cp .env.gcp .env && nano .env${NC}"
    elif [ -f .env.production ]; then
        echo -e "${GREEN}  cp .env.production .env && nano .env${NC}"
    else
        echo -e "${YELLOW}  cat > .env << 'EOF'
DB_USERNAME=feel
DB_PASSWORD=CHANGE_THIS_PASSWORD
SPRING_PROFILES_ACTIVE=prod
EOF${NC}"
        echo -e "${YELLOW}  nano .env  # 비밀번호 변경${NC}"
    fi
    exit 1
fi

echo -e "${GREEN}.env 파일 확인 완료${NC}"
echo -e "${YELLOW}.env 파일 내용 (비밀번호 제외):${NC}"
grep -v PASSWORD .env || cat .env

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
EXTERNAL_IP=$(curl -s -H "Metadata-Flavor: Google" http://metadata.google.internal/computeMetadata/v1/instance/network-interfaces/0/access-configs/0/external-ip)
echo "- Backend API: http://$EXTERNAL_IP:8080"
echo "- Health Check: http://$EXTERNAL_IP:8080/actuator/health"
echo "- API Endpoint: http://$EXTERNAL_IP:8080/api/notices"
echo ""
echo "유용한 명령어:"
echo "- 로그 확인: docker-compose logs -f backend"
echo "- 서비스 중지: docker-compose down"
echo "- 서비스 재시작: docker-compose restart"

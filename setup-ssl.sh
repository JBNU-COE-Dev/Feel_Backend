#!/bin/bash

# HTTPS 설정 자동화 스크립트

set -e

echo "=== FeeL Backend HTTPS 설정 ==="
echo ""

# 도메인 입력
read -p "도메인 이름을 입력하세요 (예: feel.example.com): " DOMAIN
read -p "이메일 주소를 입력하세요: " EMAIL

if [ -z "$DOMAIN" ] || [ -z "$EMAIL" ]; then
    echo "에러: 도메인과 이메일은 필수입니다."
    exit 1
fi

echo ""
echo "설정 정보:"
echo "  도메인: $DOMAIN"
echo "  이메일: $EMAIL"
echo ""
read -p "계속하시겠습니까? (y/n): " CONFIRM

if [ "$CONFIRM" != "y" ]; then
    echo "취소되었습니다."
    exit 0
fi

# 1. Nginx 설정 파일 업데이트
echo ""
echo "[1/5] Nginx 설정 파일 업데이트 중..."
sed -i "s/your-domain.com/$DOMAIN/g" nginx/conf.d/feel.conf
sed -i "s/your-domain.com/$DOMAIN/g" nginx/conf.d/feel-https.conf.template
echo "완료!"

# 2. Docker 컨테이너 시작
echo ""
echo "[2/5] Docker 컨테이너 시작 중..."
docker-compose down
docker-compose up -d
sleep 5
echo "완료!"

# 3. 인증서 발급 테스트
echo ""
echo "[3/5] SSL 인증서 발급 테스트 중 (dry-run)..."
docker-compose run --rm certbot certonly \
  --webroot \
  --webroot-path=/var/www/certbot \
  --email "$EMAIL" \
  --agree-tos \
  --no-eff-email \
  --dry-run \
  -d "$DOMAIN"

if [ $? -ne 0 ]; then
    echo "에러: 인증서 발급 테스트에 실패했습니다."
    echo "DNS 설정과 방화벽을 확인해주세요."
    exit 1
fi
echo "완료!"

# 4. 실제 인증서 발급
echo ""
echo "[4/5] 실제 SSL 인증서 발급 중..."
docker-compose run --rm certbot certonly \
  --webroot \
  --webroot-path=/var/www/certbot \
  --email "$EMAIL" \
  --agree-tos \
  --no-eff-email \
  -d "$DOMAIN"

if [ $? -ne 0 ]; then
    echo "에러: 인증서 발급에 실패했습니다."
    exit 1
fi
echo "완료!"

# 5. HTTPS 설정 활성화
echo ""
echo "[5/5] HTTPS 설정 활성화 중..."
cp nginx/conf.d/feel-https.conf.template nginx/conf.d/feel.conf
docker-compose restart nginx
echo "완료!"

echo ""
echo "=== HTTPS 설정이 완료되었습니다! ==="
echo ""
echo "다음 주소로 접속 가능합니다:"
echo "  https://$DOMAIN/api/notices"
echo ""
echo "프론트엔드 API URL을 다음과 같이 변경하세요:"
echo "  https://$DOMAIN"
echo ""

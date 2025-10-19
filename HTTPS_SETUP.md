# HTTPS 설정 가이드

이 가이드는 Nginx 리버스 프록시와 Let's Encrypt SSL 인증서를 사용하여 백엔드 서버에 HTTPS를 적용하는 방법을 설명합니다.

## 전제 조건

1. **도메인 이름**: SSL 인증서를 발급받기 위해서는 도메인이 필요합니다.
   - 도메인이 없는 경우: IP 주소만으로는 Let's Encrypt 인증서를 발급받을 수 없습니다.
   - 무료 도메인 서비스: Freenom, DuckDNS 등을 사용할 수 있습니다.

2. **VM 방화벽 설정**:
   ```bash
   # HTTP (80) 포트 열기 (이미 열려있음)
   # HTTPS (443) 포트 열기 (새로 추가 필요)
   ```

3. **DNS 설정**: 도메인의 A 레코드가 VM의 공인 IP (34.50.30.56)를 가리켜야 합니다.

## 1단계: VM 방화벽에서 HTTPS 포트 열기

Google Cloud Platform을 사용하는 경우:

```bash
# GCP 콘솔에서:
# VPC 네트워크 > 방화벽 > 방화벽 규칙 만들기
# 이름: allow-https
# 대상: 네트워크의 모든 인스턴스
# 소스 IPv4 범위: 0.0.0.0/0
# 프로토콜 및 포트: tcp:443
```

또는 gcloud CLI:

```bash
gcloud compute firewall-rules create allow-https \
  --allow tcp:443 \
  --source-ranges 0.0.0.0/0 \
  --description "Allow HTTPS traffic"
```

## 2단계: Nginx 설정 파일 수정

`nginx/conf.d/feel.conf` 파일에서 `your-domain.com`을 실제 도메인으로 변경:

```bash
# 예시: feel.example.com으로 변경
sed -i 's/your-domain.com/feel.example.com/g' nginx/conf.d/feel.conf
```

또는 텍스트 에디터로 직접 수정:
- `server_name your-domain.com;` → `server_name feel.example.com;`

## 3단계: Docker 컨테이너 시작

```bash
# 기존 컨테이너 중지 (실행 중인 경우)
docker-compose down

# 새 설정으로 컨테이너 시작
docker-compose up -d

# 로그 확인
docker-compose logs -f nginx
```

## 4단계: Let's Encrypt SSL 인증서 발급

### 방법 1: Dry-run 테스트 (권장)

먼저 테스트 모드로 실행하여 문제가 없는지 확인:

```bash
docker-compose run --rm certbot certonly \
  --webroot \
  --webroot-path=/var/www/certbot \
  --email cheoleooon@gmail.com \
  --agree-tos \
  --no-eff-email \
  --dry-run \
  -d http://lovesade.duckdns.org
```

### 방법 2: 실제 인증서 발급

테스트가 성공하면 실제 인증서 발급:

```bash
docker-compose run --rm certbot certonly \
  --webroot \
  --webroot-path=/var/www/certbot \
  --email cheoleooon@gmail.com \
  --agree-tos \
  --no-eff-email \
  -d http://lovesade.duckdns.org
```

**중요**: 
- `your-email@example.com`을 실제 이메일로 변경
- `your-domain.com`을 실제 도메인으로 변경
- Let's Encrypt는 발급 횟수 제한이 있으므로 신중하게 진행

## 5단계: Nginx 설정에서 HTTPS 활성화

인증서 발급이 성공하면 `nginx/conf.d/feel.conf` 파일을 수정:

1. HTTPS 서버 블록의 주석 제거 (# 삭제)
2. HTTP → HTTPS 리다이렉트 서버 블록의 주석 제거

```bash
# 또는 미리 준비된 HTTPS 설정 파일 사용
# (여기에 HTTPS가 활성화된 설정 파일을 별도로 준비해두면 좋습니다)
```

## 6단계: Nginx 재시작

```bash
# Nginx 설정 테스트
docker exec feel-nginx nginx -t

# 설정이 올바르면 Nginx 재시작
docker-compose restart nginx

# 로그 확인
docker-compose logs -f nginx
```

## 7단계: 프론트엔드 API URL 변경

프론트엔드 코드에서 API URL을 HTTPS로 변경:

```javascript
// 기존
const API_URL = 'http://34.50.30.56:8080';

// 변경
const API_URL = 'https://your-domain.com';
```

## 테스트

1. **HTTP 접속 테스트**:
   ```bash
   curl http://your-domain.com/api/notices
   ```

2. **HTTPS 접속 테스트**:
   ```bash
   curl https://your-domain.com/api/notices
   ```

3. **브라우저에서 테스트**:
   - `https://your-domain.com/api/notices`로 접속
   - 브라우저 주소창에 자물쇠 아이콘이 표시되는지 확인

## 인증서 자동 갱신

Docker Compose 설정에 포함된 certbot 서비스가 12시간마다 인증서 갱신을 시도합니다.

수동으로 갱신하려면:

```bash
docker-compose run --rm certbot renew
docker-compose restart nginx
```

## 트러블슈팅

### 문제 1: 도메인이 없는 경우

**해결책**: 
- DuckDNS (https://www.duckdns.org/) 같은 무료 동적 DNS 서비스 사용
- 또는 IP 주소로 자체 서명 인증서 사용 (프로덕션 비권장)

### 문제 2: Let's Encrypt 발급 실패

**원인**:
- DNS 설정이 올바르지 않음
- 방화벽에서 80/443 포트가 차단됨
- 도메인이 VM IP를 가리키지 않음

**확인**:
```bash
# DNS 확인
nslookup your-domain.com

# 포트 확인
curl -I http://your-domain.com
```

### 문제 3: Mixed Content 에러 여전히 발생

**원인**: 프론트엔드가 여전히 HTTP URL을 사용 중

**확인**:
- 프론트엔드 환경변수 확인
- 브라우저 캐시 삭제
- 프론트엔드 재배포

## 도메인 없이 임시로 테스트하는 방법

도메인 없이 자체 서명 인증서로 테스트할 수 있지만, 브라우저에서 보안 경고가 표시됩니다.

```bash
# 자체 서명 인증서 생성
docker-compose run --rm nginx sh -c "apk add openssl && \
  openssl req -x509 -nodes -days 365 -newkey rsa:2048 \
  -keyout /etc/letsencrypt/live/selfsigned.key \
  -out /etc/letsencrypt/live/selfsigned.crt \
  -subj '/CN=34.50.30.56'"
```

이 방법은 **개발/테스트 용도로만** 사용하고, 프로덕션에서는 반드시 정식 도메인과 Let's Encrypt 인증서를 사용하세요.

## 참고 사항

1. **Let's Encrypt 발급 제한**:
   - 도메인당 주당 50개 인증서
   - 실패 횟수 제한 있음
   - 테스트는 반드시 `--dry-run` 옵션 사용

2. **인증서 갱신 주기**:
   - Let's Encrypt 인증서는 90일 유효
   - Certbot이 자동으로 30일 전에 갱신 시도

3. **포트 변경**:
   - HTTPS 사용 시 Nginx가 443 포트를 사용
   - 프론트엔드는 `:8080` 포트 없이 도메인만 사용
   - 예: `https://your-domain.com/api/notices`

## 다음 단계

HTTPS 설정 완료 후:
1. 프론트엔드 API URL을 HTTPS로 변경
2. 프론트엔드 재배포
3. 브라우저에서 Mixed Content 에러가 해결되었는지 확인

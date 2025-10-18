# 테스트 데이터셋 전송 가이드

Docker 환경에 배포된 FeeL 백엔드 서버에 테스트 데이터를 전송하는 방법을 안내합니다.

## 전제 조건

1. Docker 서비스가 실행 중이어야 합니다
2. 백엔드 서버가 정상적으로 동작 중이어야 합니다

```bash
# 서비스 상태 확인
docker-compose ps

# 백엔드 로그 확인
docker-compose logs -f backend
```

## 1. 공지사항 테스트 데이터 전송

### 1.1 텍스트만 포함된 공지사항 (JSON)

```bash
curl -X POST http://localhost:8080/api/notices \
  -H "Content-Type: application/json" \
  -d '{
    "title": "첫번째 공지사항",
    "content": "공지사항 내용입니다.",
    "author": "관리자",
    "isPinned": false,
    "category": "일반공지"
  }'
```

### 1.2 이미지가 포함된 공지사항 (Multipart Form)

```bash
curl -X POST http://localhost:8080/api/notices \
  -F "title=이미지 포함 공지사항" \
  -F "content=테스트 이미지가 포함된 공지사항입니다." \
  -F "author=관리자" \
  -F "isPinned=false" \
  -F "category=학과소식" \
  -F "image=@C:\Users\scheoleon\workspace\FeeL_Backend\test_img.jpg"
```

**Windows 환경 (Git Bash):**
```bash
curl -X POST http://localhost:8080/api/notices \
  -F "title=이미지 포함 공지사항" \
  -F "content=테스트 이미지가 포함된 공지사항입니다." \
  -F "author=관리자" \
  -F "isPinned=false" \
  -F "category=학과소식" \
  -F "image=@C:/Users/username/test_image.jpg"
```

### 1.3 고정된 공지사항

```bash
curl -X POST http://localhost:8080/api/notices \
  -H "Content-Type: application/json" \
  -d '{
    "title": "중요 공지 - 상단 고정",
    "content": "상단에 고정될 중요 공지사항입니다.",
    "author": "학생회장",
    "isPinned": true,
    "category": "일반공지"
  }'
```

### 1.4 여러 공지사항 일괄 생성

```bash
curl -X POST http://localhost:8080/api/notices/batch \
  -H "Content-Type: application/json" \
  -d '[
    {
      "title": "학과소식 공지 1",
      "content": "학과 소식입니다.",
      "author": "학과장",
      "isPinned": false,
      "category": "학과소식"
    },
    {
      "title": "학사공지 1",
      "content": "학사 일정 안내입니다.",
      "author": "행정실",
      "isPinned": false,
      "category": "학사공지"
    },
    {
      "title": "취업정보 공지",
      "content": "채용 정보를 공유합니다.",
      "author": "취업지원센터",
      "isPinned": false,
      "category": "취업정보"
    }
  ]'
```

### 1.5 사용 가능한 카테고리

- `학과소식` (DEPARTMENT)
- `일반공지` (GENERAL)
- `학사공지` (ACADEMIC)
- `사업단공지` (PROJECT)
- `취업정보` (EMPLOYMENT)

## 2. 갤러리 테스트 데이터 전송

### 2.1 기본 갤러리 이미지 업로드

```bash
curl -X POST http://localhost:8080/api/gallery \
  -F "title=2024 학과 MT" \
  -F "description=즐거웠던 학과 MT 사진입니다." \
  -F "photographer=홍길동" \
  -F "category=행사" \
  -F "image=@C:\Users\scheoleon\workspace\FeeL_Backend\test_img.jpg"
```

**Windows 환경 (Git Bash):**
```bash
curl -X POST http://localhost:8080/api/gallery \
  -F "title=2024 학과 MT" \
  -F "description=즐거웠던 학과 MT 사진입니다." \
  -F "photographer=홍길동" \
  -F "category=행사" \
  -F "image=@C:/Users/username/photo.jpg"
```

### 2.2 설명 없는 갤러리

```bash
curl -X POST http://localhost:8080/api/gallery \
  -F "title=캠퍼스 풍경" \
  -F "photographer=김철수" \
  -F "category=일상" \
  -F "image=@/path/to/your/landscape.jpg"
```

### 2.3 카테고리별 갤러리 예시

**행사 사진:**
```bash
curl -X POST http://localhost:8080/api/gallery \
  -F "title=개강총회" \
  -F "description=2024년 1학기 개강총회" \
  -F "photographer=학생회" \
  -F "category=행사" \
  -F "image=@/path/to/event.jpg"
```

**일상 사진:**
```bash
curl -X POST http://localhost:8080/api/gallery \
  -F "title=학생회실 일상" \
  -F "description=바쁜 학생회 일상" \
  -F "photographer=간부" \
  -F "category=일상" \
  -F "image=@/path/to/daily.jpg"
```

## 3. 테스트 데이터 스크립트 예시

### 3.1 Bash 스크립트로 여러 공지사항 생성

`create_test_notices.sh` 파일 생성:

```bash
#!/bin/bash

# 공지사항 카테고리 배열
categories=("학과소식" "일반공지" "학사공지" "사업단공지" "취업정보")
authors=("관리자" "학과장" "행정실" "학생회장" "취업지원센터")

# 10개의 테스트 공지사항 생성
for i in {1..10}
do
  category=${categories[$((RANDOM % ${#categories[@]}))]}
  author=${authors[$((RANDOM % ${#authors[@]}))]}

  curl -X POST http://localhost:8080/api/notices \
    -H "Content-Type: application/json" \
    -d "{
      \"title\": \"테스트 공지사항 #$i\",
      \"content\": \"이것은 $i번째 테스트 공지사항의 내용입니다. 카테고리: $category\",
      \"author\": \"$author\",
      \"isPinned\": false,
      \"category\": \"$category\"
    }"

  echo "공지사항 #$i 생성 완료"
  sleep 0.5
done
```

**실행:**
```bash
chmod +x create_test_notices.sh
./create_test_notices.sh
```

### 3.2 이미지 포함 갤러리 일괄 생성 스크립트

`create_test_galleries.sh` 파일 생성:

```bash
#!/bin/bash

# 이미지 파일들이 있는 디렉토리
IMAGE_DIR="/path/to/images"

# 카테고리 배열
categories=("행사" "일상" "공연" "대회")

# 이미지 파일 목록 가져오기
count=1
for image_file in "$IMAGE_DIR"/*.{jpg,jpeg,png,JPG,JPEG,PNG}
do
  if [ -f "$image_file" ]; then
    category=${categories[$((RANDOM % ${#categories[@]}))]}

    curl -X POST http://localhost:8080/api/gallery \
      -F "title=갤러리 사진 #$count" \
      -F "description=테스트용 갤러리 이미지입니다." \
      -F "photographer=테스터" \
      -F "category=$category" \
      -F "image=@$image_file"

    echo "갤러리 #$count 생성 완료: $image_file"
    count=$((count + 1))
    sleep 0.5
  fi
done
```

**실행:**
```bash
chmod +x create_test_galleries.sh
./create_test_galleries.sh
```

## 4. 데이터 확인 방법

### 4.1 공지사항 조회

```bash
# 전체 공지사항 조회 (첫 페이지, 10개)
curl http://localhost:8080/api/notices?page=0&size=10

# 고정된 공지사항만 조회
curl http://localhost:8080/api/notices/pinned

# 카테고리별 조회
curl http://localhost:8080/api/notices/category/학과소식?page=0&size=10

# 검색
curl "http://localhost:8080/api/notices/search?keyword=테스트&page=0&size=10"

# 특정 공지사항 조회
curl http://localhost:8080/api/notices/1
```

### 4.2 갤러리 조회

```bash
# 전체 갤러리 조회
curl http://localhost:8080/api/gallery?page=0&size=12

# 최근 갤러리 조회 (최대 10개)
curl http://localhost:8080/api/gallery/recent

# 카테고리 필터
curl http://localhost:8080/api/gallery?category=행사&page=0&size=12

# 검색
curl "http://localhost:8080/api/gallery/search?keyword=MT&page=0&size=12"

# 특정 갤러리 조회
curl http://localhost:8080/api/gallery/1
```

## 5. 데이터 삭제

### 5.1 특정 공지사항 삭제

```bash
curl -X DELETE http://localhost:8080/api/notices/1
```

### 5.2 특정 갤러리 삭제

```bash
curl -X DELETE http://localhost:8080/api/gallery/1
```

## 6. 주의사항

1. **이미지 파일 경로**: 실제 존재하는 이미지 파일의 절대 경로를 사용해야 합니다
2. **카테고리 정확성**: 공지사항의 경우 정의된 5개 카테고리만 사용 가능합니다
3. **갤러리 이미지 필수**: 갤러리 생성 시 이미지는 필수입니다
4. **한글 인코딩**: Windows에서는 Git Bash 사용을 권장합니다
5. **서버 상태**: 요청 전 `docker-compose ps`로 서버가 실행 중인지 확인하세요

## 7. 트러블슈팅

### 서버 연결 실패
```bash
# 서비스 재시작
docker-compose restart

# 로그 확인
docker-compose logs -f backend
```

### 데이터베이스 초기화
```bash
# 모든 데이터 삭제 및 재시작
docker-compose down -v
docker-compose up -d
```

### 포트 충돌
```bash
# 8080 포트 사용 중인 프로세스 확인 (Linux/Mac)
lsof -i :8080

# Windows
netstat -ano | findstr :8080
```

## 8. API 응답 예시

### 성공 응답 (공지사항 생성)
```json
{
  "id": 1,
  "title": "첫번째 공지사항",
  "content": "공지사항 내용입니다.",
  "author": "관리자",
  "isPinned": false,
  "category": "일반공지",
  "viewCount": 0,
  "imageUrl": null,
  "createdAt": "2024-01-15T10:30:00",
  "updatedAt": "2024-01-15T10:30:00"
}
```

### 성공 응답 (갤러리 생성)
```json
{
  "id": 1,
  "title": "2024 학과 MT",
  "description": "즐거웠던 학과 MT 사진입니다.",
  "photographer": "홍길동",
  "category": "행사",
  "imageUrl": "/uploads/gallery/uuid-filename.jpg",
  "viewCount": 0,
  "createdAt": "2024-01-15T10:30:00"
}
```

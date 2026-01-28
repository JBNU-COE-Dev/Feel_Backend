# H2 DB 테스트용 curl 명령어

서버 기본 URL: `http://localhost:8080`

## 공지사항(Notice) API 테스트

### 1. 공지사항 생성 (이미지 없이)
```bash
curl -X POST http://localhost:8080/api/notices \
  -H "Content-Type: multipart/form-data" \
  -F "title=테스트 공지사항 제목" \
  -F "content=테스트 공지사항 내용입니다." \
  -F "author=관리자" \
  -F "isPinned=false" \
  -F "category=일반공지"
```

### 2. 공지사항 생성 (이미지 포함)
```bash
curl -X POST http://localhost:8080/api/notices \
  -H "Content-Type: multipart/form-data" \
  -F "title=이미지 포함 공지사항" \
  -F "content=이미지가 포함된 공지사항입니다." \
  -F "author=관리자" \
  -F "isPinned=true" \
  -F "category=학과소식" \
  -F "image=@/path/to/your/image.jpg"
```

### 3. 공지사항 일괄 생성
```bash
curl -X POST http://localhost:8080/api/notices/batch \
  -H "Content-Type: application/json" \
  -d '[
    {
      "title": "첫 번째 공지사항",
      "content": "첫 번째 공지사항 내용",
      "author": "관리자1",
      "isPinned": false,
      "category": "일반공지"
    },
    {
      "title": "두 번째 공지사항",
      "content": "두 번째 공지사항 내용",
      "author": "관리자2",
      "isPinned": true,
      "category": "학사공지"
    }
  ]'
```

### 4. 전체 공지사항 조회 (페이징)
```bash
curl -X GET "http://localhost:8080/api/notices?page=0&size=10"
```

### 5. 전체 공지사항 조회 (카테고리 필터)
```bash
curl -X GET "http://localhost:8080/api/notices?page=0&size=10&category=일반공지"
```

### 6. 고정 공지사항 조회
```bash
curl -X GET http://localhost:8080/api/notices/pinned
```

### 7. 특정 공지사항 조회
```bash
curl -X GET http://localhost:8080/api/notices/1
```

### 8. 공지사항 수정 (이미지 없이)
```bash
curl -X PUT http://localhost:8080/api/notices/1 \
  -H "Content-Type: multipart/form-data" \
  -F "title=수정된 제목" \
  -F "content=수정된 내용" \
  -F "author=관리자" \
  -F "isPinned=true" \
  -F "category=학사공지"
```

### 9. 공지사항 수정 (이미지 포함)
```bash
curl -X PUT http://localhost:8080/api/notices/1 \
  -H "Content-Type: multipart/form-data" \
  -F "title=수정된 제목" \
  -F "content=수정된 내용" \
  -F "author=관리자" \
  -F "isPinned=true" \
  -F "category=학사공지" \
  -F "image=@/path/to/your/new-image.jpg"
```

### 10. 공지사항 삭제
```bash
curl -X DELETE http://localhost:8080/api/notices/1
```

### 11. 카테고리별 조회
```bash
curl -X GET "http://localhost:8080/api/notices/category/일반공지?page=0&size=10"
```

### 12. 공지사항 검색
```bash
curl -X GET "http://localhost:8080/api/notices/search?keyword=테스트&page=0&size=10"
```

### 13. 공지사항 검색 (카테고리 필터 포함)
```bash
curl -X GET "http://localhost:8080/api/notices/search?keyword=테스트&page=0&size=10&category=일반공지"
```

### 유효한 공지사항 카테고리
- `학과소식`
- `일반공지`
- `학사공지`
- `사업단공지`
- `취업정보`

---

## 갤러리(Gallery) API 테스트

### 1. 갤러리 생성 (이미지 필수)
```bash
curl -X POST http://localhost:8080/api/gallery \
  -H "Content-Type: multipart/form-data" \
  -F "title=갤러리 제목" \
  -F "description=갤러리 설명입니다." \
  -F "photographer=사진작가명" \
  -F "category=행사" \
  -F "image=@/path/to/your/image.jpg"
```

### 2. 갤러리 생성 (최소 필수 항목만)
```bash
curl -X POST http://localhost:8080/api/gallery \
  -H "Content-Type: multipart/form-data" \
  -F "title=갤러리 제목" \
  -F "image=@/path/to/your/image.jpg"
```

### 3. 전체 갤러리 조회 (페이징)
```bash
curl -X GET "http://localhost:8080/api/gallery?page=0&size=12"
```

### 4. 전체 갤러리 조회 (카테고리 필터)
```bash
curl -X GET "http://localhost:8080/api/gallery?page=0&size=12&category=행사"
```

### 5. 최근 갤러리 조회
```bash
curl -X GET http://localhost:8080/api/gallery/recent
```

### 6. 특정 갤러리 조회
```bash
curl -X GET http://localhost:8080/api/gallery/1
```

### 7. 갤러리 수정 (이미지 없이)
```bash
curl -X PUT http://localhost:8080/api/gallery/1 \
  -H "Content-Type: multipart/form-data" \
  -F "title=수정된 갤러리 제목" \
  -F "description=수정된 설명" \
  -F "photographer=새 사진작가명" \
  -F "category=전시"
```

### 8. 갤러리 수정 (이미지 포함)
```bash
curl -X PUT http://localhost:8080/api/gallery/1 \
  -H "Content-Type: multipart/form-data" \
  -F "title=수정된 갤러리 제목" \
  -F "description=수정된 설명" \
  -F "photographer=새 사진작가명" \
  -F "category=전시" \
  -F "image=@/path/to/your/new-image.jpg"
```

### 9. 갤러리 삭제
```bash
curl -X DELETE http://localhost:8080/api/gallery/1
```

### 10. 갤러리 검색
```bash
curl -X GET "http://localhost:8080/api/gallery/search?keyword=행사&page=0&size=12"
```

### 11. 갤러리 검색 (카테고리 필터 포함)
```bash
curl -X GET "http://localhost:8080/api/gallery/search?keyword=행사&page=0&size=12&category=행사"
```

---

## 테스트 시나리오 예시

### 시나리오 1: 공지사항 CRUD 테스트
```bash
# 1. 공지사항 생성
NOTICE_ID=$(curl -s -X POST http://localhost:8080/api/notices \
  -H "Content-Type: multipart/form-data" \
  -F "title=테스트 공지사항" \
  -F "content=테스트 내용" \
  -F "author=테스터" \
  -F "category=일반공지" | jq -r '.id')

echo "생성된 공지사항 ID: $NOTICE_ID"

# 2. 생성된 공지사항 조회
curl -X GET "http://localhost:8080/api/notices/$NOTICE_ID"

# 3. 공지사항 수정
curl -X PUT "http://localhost:8080/api/notices/$NOTICE_ID" \
  -H "Content-Type: multipart/form-data" \
  -F "title=수정된 제목" \
  -F "content=수정된 내용" \
  -F "author=테스터" \
  -F "category=학사공지"

# 4. 공지사항 삭제
curl -X DELETE "http://localhost:8080/api/notices/$NOTICE_ID"
```

### 시나리오 2: 갤러리 CRUD 테스트
```bash
# 1. 갤러리 생성 (이미지 파일 필요)
GALLERY_ID=$(curl -s -X POST http://localhost:8080/api/gallery \
  -H "Content-Type: multipart/form-data" \
  -F "title=테스트 갤러리" \
  -F "description=테스트 설명" \
  -F "photographer=테스트 작가" \
  -F "category=테스트" \
  -F "image=@/path/to/test-image.jpg" | jq -r '.id')

echo "생성된 갤러리 ID: $GALLERY_ID"

# 2. 생성된 갤러리 조회
curl -X GET "http://localhost:8080/api/gallery/$GALLERY_ID"

# 3. 갤러리 수정
curl -X PUT "http://localhost:8080/api/gallery/$GALLERY_ID" \
  -H "Content-Type: multipart/form-data" \
  -F "title=수정된 갤러리 제목" \
  -F "description=수정된 설명" \
  -F "photographer=수정된 작가" \
  -F "category=수정된카테고리"

# 4. 갤러리 삭제
curl -X DELETE "http://localhost:8080/api/gallery/$GALLERY_ID"
```

---

## 참고사항

1. **이미지 파일 경로**: 위 예시의 `/path/to/your/image.jpg`를 실제 이미지 파일 경로로 변경하세요.

2. **jq 설치**: JSON 응답을 파싱하려면 `jq`가 필요합니다.
   ```bash
   # Ubuntu/Debian
   sudo apt-get install jq
   
   # macOS
   brew install jq
   ```

3. **테스트 이미지 생성**: 이미지 파일이 없다면 간단한 테스트 이미지를 생성할 수 있습니다.
   ```bash
   # 1x1 픽셀 PNG 이미지 생성
   convert -size 1x1 xc:white test-image.png
   ```

4. **H2 콘솔 접속**: 
   - URL: `http://localhost:8080/h2-console`
   - JDBC URL: `jdbc:h2:file:./data/feeldb`
   - 사용자명: `sa`
   - 비밀번호: (비워두기)

5. **응답 확인**: 모든 curl 명령어에 `-v` 옵션을 추가하면 상세한 HTTP 응답을 확인할 수 있습니다.
   ```bash
   curl -v -X GET http://localhost:8080/api/notices
   ```

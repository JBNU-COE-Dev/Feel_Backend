# FeeL 공지사항 API 테스트 명령어 모음

## 목차
- [기본 공지사항 생성 (텍스트만)](#기본-공지사항-생성-텍스트만)
- [이미지 첨부 공지사항 생성](#이미지-첨부-공지사항-생성)
- [대량 데이터 생성](#대량-데이터-생성)
- [공지사항 조회](#공지사항-조회)
- [공지사항 수정](#공지사항-수정)
- [공지사항 삭제](#공지사항-삭제)
- [검색 및 필터링](#검색-및-필터링)

---

## 기본 공지사항 생성 (텍스트만)

### 1. 일반 공지사항 생성
```bash
curl -X POST http://localhost:8080/api/notices \
  -H "Content-Type: application/json" \
  -d '{
    "title": "첫 번째 공지사항",
    "content": "공지사항 내용입니다.",
    "author": "관리자",
    "isPinned": false,
    "category": "일반공지"
  }'
```

### 2. 고정 공지사항 생성
```bash
curl -X POST http://localhost:8080/api/notices \
  -H "Content-Type: application/json" \
  -d '{
    "title": "중요 공지사항 (상단 고정)",
    "content": "이 공지사항은 상단에 고정됩니다.",
    "author": "학생회장",
    "isPinned": true,
    "category": "긴급공지"
  }'
```

### 3. 카테고리별 공지사항 생성
```bash
# 학사공지
curl -X POST http://localhost:8080/api/notices \
  -H "Content-Type: application/json" \
  -d '{
    "title": "중간고사 일정 안내",
    "content": "중간고사는 4월 15일부터 시작됩니다.",
    "author": "학사팀",
    "isPinned": false,
    "category": "학사공지"
  }'

# 행사공지
curl -X POST http://localhost:8080/api/notices \
  -H "Content-Type: application/json" \
  -d '{
    "title": "FeeL 축제 개최",
    "content": "5월 20일 공과대학 축제가 열립니다.",
    "author": "행사부",
    "isPinned": false,
    "category": "행사공지"
  }'
```

---

## 이미지 첨부 공지사항 생성

### 1. 단일 이미지 첨부
```bash
curl -X POST http://localhost:8080/api/notices \
  -F "title=이미지가 포함된 공지사항" \
  -F "content=첨부된 이미지를 확인해주세요." \
  -F "author=관리자" \
  -F "isPinned=false" \
  -F "category=일반공지" \
  -F "image=@C:/Users/scheoleon/workspace/FeeL_Backend/test_img.jpg"
```

### 2. 고정 공지 + 이미지
```bash
curl -X POST http://localhost:8080/api/notices \
  -F "title=긴급 공지 (이미지 포함)" \
  -F "content=반드시 확인해주세요!" \
  -F "author=학생회장" \
  -F "isPinned=true" \
  -F "category=긴급공지" \
  -F "image=@C:/Users/scheoleon/workspace/FeeL_Backend/test_img.jpg"
```

### 3. 다양한 카테고리 + 이미지
```bash
# 학사공지 + 이미지
curl -X POST http://localhost:8080/api/notices \
  -F "title=시간표 안내" \
  -F "content=2024년 1학기 시간표입니다." \
  -F "author=학사팀" \
  -F "isPinned=false" \
  -F "category=학사공지" \
  -F "image=@C:/path/to/timetable.jpg"

# 행사공지 + 이미지
curl -X POST http://localhost:8080/api/notices \
  -F "title=축제 포스터" \
  -F "content=FeeL 축제 포스터를 공개합니다." \
  -F "author=홍보부" \
  -F "isPinned=true" \
  -F "category=행사공지" \
  -F "image=@C:/path/to/poster.png"
```

---

## 대량 데이터 생성

### 1. 반복문을 이용한 대량 생성 (Bash/Git Bash)
```bash
# 10개의 테스트 공지사항 생성
for i in {1..10}; do
  curl -X POST http://localhost:8080/api/notices \
    -H "Content-Type: application/json" \
    -d "{
      \"title\": \"테스트 공지사항 #$i\",
      \"content\": \"이것은 $i번째 테스트 공지사항입니다.\",
      \"author\": \"테스터\",
      \"isPinned\": false,
      \"category\": \"일반공지\"
    }"
  echo "공지사항 $i 생성 완료"
done
```

### 2. Windows CMD용 반복 생성
```cmd
@echo off
for /L %%i in (1,1,10) do (
  curl -X POST http://localhost:8080/api/notices ^
    -H "Content-Type: application/json" ^
    -d "{\"title\": \"테스트 공지사항 #%%i\", \"content\": \"%%i번째 테스트 공지사항\", \"author\": \"테스터\", \"isPinned\": false, \"category\": \"일반공지\"}"
  echo 공지사항 %%i 생성 완료
)
```

### 3. 다양한 카테고리 혼합 생성 (Bash)
```bash
categories=("일반공지" "학사공지" "행사공지" "긴급공지")
for i in {1..20}; do
  category=${categories[$((i % 4))]}
  curl -X POST http://localhost:8080/api/notices \
    -H "Content-Type: application/json" \
    -d "{
      \"title\": \"$category - 테스트 #$i\",
      \"content\": \"카테고리: $category / 번호: $i\",
      \"author\": \"자동생성\",
      \"isPinned\": false,
      \"category\": \"$category\"
    }"
  sleep 0.5
done
```

---

## 공지사항 조회

### 1. 전체 공지사항 조회
```bash
# 첫 페이지 (기본: 10개)
curl -X GET http://localhost:8080/api/notices

# 페이징 옵션 지정
curl -X GET "http://localhost:8080/api/notices?page=0&size=20"

# 두 번째 페이지
curl -X GET "http://localhost:8080/api/notices?page=1&size=10"
```

### 2. 특정 공지사항 조회
```bash
# ID로 조회 (조회수 증가)
curl -X GET http://localhost:8080/api/notices/1

# 여러 개 조회
curl -X GET http://localhost:8080/api/notices/2
curl -X GET http://localhost:8080/api/notices/3
```

### 3. 고정 공지사항만 조회
```bash
curl -X GET http://localhost:8080/api/notices/pinned
```

---

## 공지사항 수정

### 1. 텍스트 정보만 수정
```bash
curl -X PUT http://localhost:8080/api/notices/1 \
  -H "Content-Type: application/json" \
  -d '{
    "title": "수정된 제목",
    "content": "수정된 내용입니다.",
    "author": "관리자",
    "isPinned": true,
    "category": "긴급공지"
  }'
```

### 2. 고정 상태 변경
```bash
# 고정으로 변경
curl -X PUT http://localhost:8080/api/notices/2 \
  -H "Content-Type: application/json" \
  -d '{
    "title": "기존 제목",
    "content": "기존 내용",
    "author": "관리자",
    "isPinned": true,
    "category": "일반공지"
  }'

# 고정 해제
curl -X PUT http://localhost:8080/api/notices/2 \
  -H "Content-Type: application/json" \
  -d '{
    "title": "기존 제목",
    "content": "기존 내용",
    "author": "관리자",
    "isPinned": false,
    "category": "일반공지"
  }'
```

### 3. 카테고리 변경
```bash
curl -X PUT http://localhost:8080/api/notices/3 \
  -H "Content-Type: application/json" \
  -d '{
    "title": "제목 유지",
    "content": "내용 유지",
    "author": "관리자",
    "isPinned": false,
    "category": "학사공지"
  }'
```

---

## 공지사항 삭제

### 1. 단일 삭제
```bash
curl -X DELETE http://localhost:8080/api/notices/1
```

### 2. 여러 개 삭제
```bash
curl -X DELETE http://localhost:8080/api/notices/1
curl -X DELETE http://localhost:8080/api/notices/2
curl -X DELETE http://localhost:8080/api/notices/3
```

### 3. 범위 삭제 (Bash)
```bash
# ID 10~20 삭제
for i in {10..20}; do
  curl -X DELETE http://localhost:8080/api/notices/$i
  echo "공지사항 $i 삭제 완료"
done
```

---

## 검색 및 필터링

### 1. 키워드 검색
```bash
# 제목 또는 내용에서 검색
curl -X GET "http://localhost:8080/api/notices/search?keyword=축제"

# 페이징과 함께 검색
curl -X GET "http://localhost:8080/api/notices/search?keyword=공지&page=0&size=5"
```

### 2. 카테고리별 조회
```bash
# 일반공지
curl -X GET http://localhost:8080/api/notices/category/일반공지

# 학사공지
curl -X GET http://localhost:8080/api/notices/category/학사공지

# 행사공지
curl -X GET http://localhost:8080/api/notices/category/행사공지

# 긴급공지
curl -X GET http://localhost:8080/api/notices/category/긴급공지

# 페이징과 함께
curl -X GET "http://localhost:8080/api/notices/category/일반공지?page=0&size=10"
```

### 3. 복합 조회 예시
```bash
# 1. 고정 공지 확인
curl -X GET http://localhost:8080/api/notices/pinned

# 2. 특정 카테고리 조회
curl -X GET http://localhost:8080/api/notices/category/행사공지

# 3. 키워드로 검색
curl -X GET "http://localhost:8080/api/notices/search?keyword=시험"
```

---

## 응답 형식 예시

### 성공 응답
```json
{
  "id": 1,
  "title": "공지사항 제목",
  "content": "공지사항 내용",
  "author": "관리자",
  "isPinned": false,
  "category": "일반공지",
  "viewCount": 0,
  "createdAt": "2024-01-15T10:30:00",
  "updatedAt": "2024-01-15T10:30:00",
  "imageUrl": null
}
```

### 페이징 응답
```json
{
  "content": [...],
  "pageable": {...},
  "totalPages": 5,
  "totalElements": 50,
  "size": 10,
  "number": 0
}
```

---

## 유용한 팁

### 1. JSON 예쁘게 출력 (jq 사용)
```bash
curl -X GET http://localhost:8080/api/notices/1 | jq
```

### 2. 응답 헤더 확인
```bash
curl -i -X GET http://localhost:8080/api/notices/1
```

### 3. 자세한 요청/응답 확인
```bash
curl -v -X POST http://localhost:8080/api/notices \
  -H "Content-Type: application/json" \
  -d '{"title": "테스트", "content": "내용", "author": "작성자", "isPinned": false, "category": "일반공지"}'
```

### 4. 타임아웃 설정
```bash
curl --max-time 10 -X GET http://localhost:8080/api/notices
```

### 5. 파일로 응답 저장
```bash
curl -X GET http://localhost:8080/api/notices > notices.json
```

---

## 테스트 시나리오

### 전체 기능 테스트
```bash
# 1. 공지사항 3개 생성
curl -X POST http://localhost:8080/api/notices \
  -H "Content-Type: application/json" \
  -d '{"title": "공지1", "content": "내용1", "author": "작성자1", "isPinned": false, "category": "일반공지"}'

curl -X POST http://localhost:8080/api/notices \
  -H "Content-Type: application/json" \
  -d '{"title": "공지2", "content": "내용2", "author": "작성자2", "isPinned": true, "category": "긴급공지"}'

curl -X POST http://localhost:8080/api/notices \
  -H "Content-Type: application/json" \
  -d '{"title": "공지3", "content": "내용3", "author": "작성자3", "isPinned": false, "category": "학사공지"}'

# 2. 전체 조회
curl -X GET http://localhost:8080/api/notices

# 3. 고정 공지 조회
curl -X GET http://localhost:8080/api/notices/pinned

# 4. 특정 공지 조회 (조회수 증가)
curl -X GET http://localhost:8080/api/notices/1

# 5. 수정
curl -X PUT http://localhost:8080/api/notices/1 \
  -H "Content-Type: application/json" \
  -d '{"title": "수정된 제목", "content": "수정된 내용", "author": "작성자1", "isPinned": true, "category": "일반공지"}'

# 6. 검색
curl -X GET "http://localhost:8080/api/notices/search?keyword=수정"

# 7. 삭제
curl -X DELETE http://localhost:8080/api/notices/1
```

---

## 주의사항

1. **이미지 경로**: 이미지 첨부 시 `@` 뒤에 실제 파일 경로를 지정해야 합니다.
2. **인코딩**: 한글이 포함된 URL은 인코딩이 필요할 수 있습니다.
3. **서버 실행**: 테스트 전에 서버가 `http://localhost:8080`에서 실행 중인지 확인하세요.
4. **데이터베이스**: H2 콘솔(`http://localhost:8080/h2-console`)에서 데이터 확인 가능합니다.
5. **CORS**: 프론트엔드에서 테스트 시 CORS 설정을 확인하세요.

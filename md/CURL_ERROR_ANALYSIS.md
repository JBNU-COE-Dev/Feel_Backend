# curl 공지사항 생성 에러 분석 보고서

## 📋 에러 개요

**발생한 에러**:
```
{"timestamp":"2025-10-12T11:02:01.969+00:00","status":400,"error":"Bad Request","path":"/api/notices"}
curl: (3) URL rejected: Bad hostname
```

**실행한 명령어** (SETUP_COMPLETE.md):
```bash
curl -X POST http://localhost:8080/api/notices \
  -H "Content-Type: application/json" \
  -d '{"title":"첫 번째 공지사항","content":"테스트 공지사항입니다.","author":"관리자","isPinned":false,"category":"공지"}'
```

---

## 🔍 에러 원인 분석

### 1. **Git Bash에서 한글 UTF-8 인코딩 문제**

#### 서버 로그 확인:
```
WARN: JSON parse error: Invalid UTF-8 middle byte 0xd7
WARN: Unexpected character (''') (code 39)
```

#### 문제점:
- **Git Bash**에서 curl로 한글을 전송할 때 UTF-8 인코딩이 깨짐
- JSON 파서가 잘못된 UTF-8 바이트를 인식하여 **400 Bad Request** 반환
- 작은따옴표(') 처리 문제도 함께 발생

### 2. **Git Bash의 MSYS2 경로 변환 문제**

Git Bash(MSYS2)는 Windows 경로를 자동으로 변환하려다 JSON 문자열을 손상시킬 수 있습니다.

---

## ✅ 해결 방법

### **방법 1: Git Bash에서 영어 사용 (즉시 해결)**

```bash
curl -X POST http://localhost:8080/api/notices \
  -H "Content-Type: application/json" \
  -d "{\"title\":\"First Notice\",\"content\":\"This is a test notice.\",\"author\":\"Admin\",\"isPinned\":false,\"category\":\"Notice\"}"
```

**주의사항**:
- **큰따옴표(")** 사용 (작은따옴표 대신)
- JSON 내부 따옴표는 **백슬래시(\\)로 이스케이프**
- 한글 대신 **영어** 사용

**테스트 결과**: ✅ 성공
```json
{
  "id": 1,
  "title": "First Notice",
  "content": "This is a test notice.",
  "author": "Admin",
  "isPinned": false,
  "viewCount": 0,
  "category": "Notice"
}
```

---

### **방법 2: Windows PowerShell 사용 (한글 지원)**

```powershell
Invoke-RestMethod -Uri http://localhost:8080/api/notices -Method Post `
  -ContentType "application/json" `
  -Body '{"title":"첫 번째 공지사항","content":"테스트 공지사항입니다.","author":"관리자","isPinned":false,"category":"공지"}'
```

**장점**:
- ✅ 한글 완벽 지원
- ✅ 인코딩 문제 없음
- ✅ JSON 응답 자동 파싱

**추천**: Windows에서 한글 데이터 테스트 시 **PowerShell 사용 권장**

---

### **방법 3: Windows CMD 사용**

```cmd
curl -X POST http://localhost:8080/api/notices -H "Content-Type: application/json" -d "{\"title\":\"Test Notice\",\"content\":\"Test content\",\"author\":\"Admin\",\"isPinned\":false,\"category\":\"Notice\"}"
```

**주의**: 한글 지원 제한적

---

### **방법 4: JSON 파일 사용 (복잡한 데이터)**

#### 1. JSON 파일 생성 (`notice.json`):
```json
{
  "title": "첫 번째 공지사항",
  "content": "테스트 공지사항입니다.",
  "author": "관리자",
  "isPinned": false,
  "category": "공지"
}
```

#### 2. Git Bash에서 실행:
```bash
curl -X POST http://localhost:8080/api/notices \
  -H "Content-Type: application/json" \
  -d @notice.json
```

**장점**:
- ✅ 인코딩 문제 우회
- ✅ 복잡한 JSON 구조 지원
- ✅ 재사용 가능

---

## 📊 환경별 비교

| 환경 | 한글 지원 | 명령어 복잡도 | 권장도 |
|------|----------|--------------|--------|
| **PowerShell** | ✅ 완벽 | ⭐⭐⭐ 보통 | ⭐⭐⭐⭐⭐ |
| **Git Bash (영어)** | ❌ 불가 | ⭐⭐⭐⭐ 복잡 | ⭐⭐⭐ |
| **Git Bash (파일)** | ✅ 가능 | ⭐⭐ 간단 | ⭐⭐⭐⭐ |
| **Windows CMD** | ⚠️ 제한적 | ⭐⭐⭐⭐ 복잡 | ⭐⭐ |

---

## 🎯 권장 사항

### Windows 환경에서:
1. **한글 데이터 테스트**: **PowerShell** 사용
2. **영어 데이터 테스트**: Git Bash 또는 CMD 사용
3. **복잡한 JSON**: JSON 파일 + Git Bash 사용

### Git Bash 사용 시 체크리스트:
- [ ] 한글 대신 영어 사용
- [ ] 큰따옴표(") 사용
- [ ] JSON 내부 따옴표 이스케이프 (\\")
- [ ] 멀티라인 명령어 시 백슬래시(\\) 사용

---

## 🔧 실전 예제

### 고정 공지사항 생성 (PowerShell)
```powershell
Invoke-RestMethod -Uri http://localhost:8080/api/notices -Method Post `
  -ContentType "application/json" `
  -Body '{"title":"[중요] 학생회 정기총회","content":"정기총회가 3월 15일 개최됩니다.","author":"집행부","isPinned":true,"category":"공지"}'
```

### 일반 공지사항 생성 (Git Bash - 영어)
```bash
curl -X POST http://localhost:8080/api/notices \
  -H "Content-Type: application/json" \
  -d "{\"title\":\"Spring Festival\",\"content\":\"Join our booth at the spring festival.\",\"author\":\"Culture Team\",\"isPinned\":false,\"category\":\"Event\"}"
```

### JSON 파일 사용 (모든 환경)
```bash
# 1. notice.json 파일 생성 (UTF-8 인코딩)
# 2. curl로 전송
curl -X POST http://localhost:8080/api/notices \
  -H "Content-Type: application/json" \
  -d @notice.json
```

---

## 📝 테스트 결과

### 생성된 공지사항:
1. ✅ ID: 1 - "First Notice" (영어, Git Bash)
2. ✅ ID: 2 - "Welcome to FeeL" (영어, 고정)
3. ✅ ID: 3 - "Spring Festival Event" (영어, 일반)

### API 응답 확인:
```bash
curl http://localhost:8080/api/notices
```

**결과**: 총 3개 공지사항 정상 조회 ✅

---

## 🚨 주의사항

### Git Bash에서 피해야 할 것:
- ❌ 한글 직접 입력
- ❌ 작은따옴표(') 사용 (JSON 데이터에)
- ❌ 인코딩 지정 없이 복잡한 문자열 전송

### 올바른 사용법:
- ✅ 영어 또는 JSON 파일 사용
- ✅ 큰따옴표(") + 이스케이프
- ✅ PowerShell 활용 (한글 필요 시)

---

## 🔗 관련 문서

- **SETUP_COMPLETE.md**: 업데이트된 테스트 데이터 생성 가이드
- **ERROR_REPORT.md**: 포트 충돌 에러 분석
- **README.md**: 전체 프로젝트 가이드

# 분석 및 보고서 문서

이 디렉토리는 프로젝트 개발 중 발생한 문제 분석, 에러 보고서, 설정 가이드 등의 문서를 포함합니다.

## 📁 문서 목록

### 에러 분석 보고서

1. **[ERROR_REPORT.md](./ERROR_REPORT.md)**
   - 포트 8080 충돌 에러 분석
   - 서버 실행 시 "Address already in use" 문제 해결

2. **[CURL_ERROR_ANALYSIS.md](./CURL_ERROR_ANALYSIS.md)**
   - curl 명령어 실행 시 400 Bad Request 에러
   - Git Bash에서 한글 UTF-8 인코딩 문제 분석
   - Windows 환경별 해결 방법 (PowerShell, CMD, Git Bash)

3. **[H2_ERROR_500_ANALYSIS.md](./H2_ERROR_500_ANALYSIS.md)**
   - 500 Internal Server Error 분석
   - H2 인메모리 데이터베이스 초기화 문제
   - 파일 기반 H2로 영구 해결 방법

### 설정 가이드

4. **[QUICK_START.md](./QUICK_START.md)**
   - 빠른 시작 가이드
   - 서버 실행, 테스트 데이터 생성
   - 트러블슈팅 기본 가이드

5. **[SETUP_COMPLETE.md](./SETUP_COMPLETE.md)**
   - 환경 구축 완료 보고서
   - 설치된 환경 정보
   - 실행 방법 및 테스트 결과

---

## 📊 문서 카테고리별 분류

### 🔥 긴급 문제 해결
- 서버 실행 실패 → **ERROR_REPORT.md**
- API 호출 실패 (500) → **H2_ERROR_500_ANALYSIS.md**
- curl 에러 → **CURL_ERROR_ANALYSIS.md**

### 🚀 시작하기
- 처음 시작 → **QUICK_START.md**
- 환경 설정 확인 → **SETUP_COMPLETE.md**

### 📚 상세 분석
- 포트 충돌 상세 분석 → **ERROR_REPORT.md**
- 인코딩 문제 상세 분석 → **CURL_ERROR_ANALYSIS.md**
- 데이터베이스 문제 상세 분석 → **H2_ERROR_500_ANALYSIS.md**

---

## 🔍 에러별 빠른 찾기

### "Port 8080 was already in use"
→ [ERROR_REPORT.md](./ERROR_REPORT.md)

### "400 Bad Request" (curl 실행 시)
→ [CURL_ERROR_ANALYSIS.md](./CURL_ERROR_ANALYSIS.md)

### "500 Internal Server Error" + "Table not found"
→ [H2_ERROR_500_ANALYSIS.md](./H2_ERROR_500_ANALYSIS.md)

### "JSON parse error: Invalid UTF-8"
→ [CURL_ERROR_ANALYSIS.md](./CURL_ERROR_ANALYSIS.md)

---

## 📝 문서 작성 규칙

앞으로 이 디렉토리에 추가되는 문서는 다음 형식을 따릅니다:

### 파일명 규칙
- 분석 보고서: `[주제]_ANALYSIS.md`
- 에러 보고서: `[에러명]_ERROR_REPORT.md`
- 가이드: `[주제]_GUIDE.md`
- 설정: `[기능]_SETUP.md`

### 문서 구조
1. **개요**: 문제 요약
2. **원인 분석**: 상세 분석
3. **해결 방법**: 단계별 해결책
4. **예방 방법**: 재발 방지
5. **관련 문서**: 참고 링크

---

## 🔗 메인 문서

프로젝트 전체 가이드는 상위 디렉토리의 [README.md](../README.md)를 참고하세요.

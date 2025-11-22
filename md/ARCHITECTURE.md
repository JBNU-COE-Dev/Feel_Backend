# FeeL Backend 아키텍처 및 데이터 흐름 분석

## 📁 프로젝트 폴더 구조

```
src/main/java/com/feel/backend/
│
├── 🚀 BackendApplication.java              # 애플리케이션 진입점
│
├── 📋 config/                              # 설정 계층
│   └── WebConfig.java                      # CORS 설정
│
├── 🌐 controller/                          # Presentation 계층
│   ├── NoticeController.java               # 공지사항 REST API
│   └── GalleryController.java              # 갤러리 REST API
│
├── 📦 dto/                                 # 데이터 전송 객체
│   ├── NoticeRequestDto.java               # 공지사항 요청 (입력)
│   ├── NoticeResponseDto.java              # 공지사항 응답 (출력)
│   ├── GalleryRequestDto.java              # 갤러리 요청
│   └── GalleryResponseDto.java             # 갤러리 응답
│
├── 🗃️ entity/                              # Domain 계층
│   ├── Notice.java                         # 공지사항 엔티티
│   ├── Gallery.java                        # 갤러리 엔티티
│   └── NoticeCategory.java                 # 카테고리 Enum
│
├── 💾 repository/                          # Persistence 계층
│   ├── NoticeRepository.java               # 공지사항 데이터 접근
│   └── GalleryRepository.java              # 갤러리 데이터 접근
│
├── ⚙️ service/                             # Business 계층
│   ├── NoticeService.java                  # 공지사항 비즈니스 로직
│   ├── GalleryService.java                 # 갤러리 비즈니스 로직
│   └── FileStorageService.java             # 파일 처리 로직
│
└── ✅ validation/                          # 검증 계층
    ├── ValidCategory.java                  # 커스텀 검증 어노테이션
    └── CategoryValidator.java              # 카테고리 검증 로직
```

---

## 🏗️ 계층별 아키텍처 (Layered Architecture)

```
┌─────────────────────────────────────────────────────────┐
│           Client (React/Browser)                        │
└─────────────────────────────────────────────────────────┘
                         ↓ HTTP Request
                         ↓ (JSON/Multipart Form)
┌─────────────────────────────────────────────────────────┐
│  ① Controller Layer (Presentation)                      │
│  ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━  │
│  - REST API 엔드포인트 노출                             │
│  - HTTP 요청/응답 처리                                  │
│  - RequestDto → Service 전달                           │
│  - @RestController, @RequestMapping                    │
│  NoticeController.java                                  │
└─────────────────────────────────────────────────────────┘
                         ↓ RequestDto + MultipartFile
┌─────────────────────────────────────────────────────────┐
│  ② Service Layer (Business Logic)                       │
│  ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━  │
│  - 비즈니스 로직 처리                                   │
│  - 트랜잭션 관리 (@Transactional)                       │
│  - DTO ↔ Entity 변환                                   │
│  - Repository 호출                                      │
│  NoticeService.java, FileStorageService.java            │
└─────────────────────────────────────────────────────────┘
                         ↓ Entity
┌─────────────────────────────────────────────────────────┐
│  ③ Repository Layer (Data Access)                       │
│  ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━  │
│  - 데이터베이스 CRUD 작업                               │
│  - Spring Data JPA 쿼리 메서드                         │
│  - @Repository, extends JpaRepository                   │
│  NoticeRepository.java                                  │
└─────────────────────────────────────────────────────────┘
                         ↓ SQL/JDBC
┌─────────────────────────────────────────────────────────┐
│  ④ Database (H2/MySQL)                                  │
│  ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━  │
│  - notices 테이블                                       │
│  - gallery 테이블                                       │
└─────────────────────────────────────────────────────────┘
```

---

## 🔄 데이터 입/출력 흐름 (공지사항 생성 예시)

### **[입력] 공지사항 생성 요청 (POST /api/notices)**

```
┌────────────────────────────────────────────────────────────────┐
│  1️⃣ HTTP Request (Client → Server)                            │
└────────────────────────────────────────────────────────────────┘

POST /api/notices
Content-Type: multipart/form-data

{
  "title": "2024 봄 축제 안내",
  "content": "봄 축제가 열립니다!",
  "author": "학생회장",
  "isPinned": true,
  "category": "행사",
  "image": [파일 데이터]
}

                         ↓

┌────────────────────────────────────────────────────────────────┐
│  2️⃣ NoticeController.createNotice()                           │
│  (NoticeController.java:24-31)                                 │
└────────────────────────────────────────────────────────────────┘

@PostMapping
public ResponseEntity<NoticeResponseDto> createNotice(
    @Valid @ModelAttribute NoticeRequestDto requestDto,     // ← 검증됨
    @RequestParam(required = false) MultipartFile image     // ← 파일
) {
    NoticeResponseDto response = noticeService.createNotice(requestDto, image);
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
}

역할:
✅ @Valid로 입력 검증 (제목/내용 필수, 길이 제한)
✅ RequestDto와 MultipartFile을 Service로 전달
✅ HTTP 201 Created 응답 생성

                         ↓ RequestDto + MultipartFile

┌────────────────────────────────────────────────────────────────┐
│  3️⃣ NoticeService.createNotice()                              │
│  (NoticeService.java:29-50)                                    │
└────────────────────────────────────────────────────────────────┘

@Transactional  // ← 트랜잭션 시작
public NoticeResponseDto createNotice(NoticeRequestDto requestDto, MultipartFile imageFile) {

    // 3-1. 파일 저장 (FileStorageService)
    String imageUrl = null;
    if (imageFile != null && !imageFile.isEmpty()) {
        String fileName = fileStorageService.storeFile(imageFile);  // ← 파일 시스템에 저장
        imageUrl = "/uploads/" + fileName;
    }

    // 3-2. DTO → Entity 변환
    Notice notice = Notice.builder()
        .title(requestDto.getTitle())
        .content(requestDto.getContent())
        .author(requestDto.getAuthor())
        .isPinned(requestDto.getIsPinned())
        .category(requestDto.getCategory())
        .imageUrl(imageUrl)  // ← 파일 경로 저장
        .viewCount(0)
        .build();

    // 3-3. Entity 저장
    Notice savedNotice = noticeRepository.save(notice);  // ← DB INSERT

    // 3-4. Entity → ResponseDto 변환
    return NoticeResponseDto.fromEntity(savedNotice);
}

역할:
✅ 파일 업로드 처리 (uploads/ 디렉토리에 저장)
✅ DTO → Entity 변환 (Builder 패턴)
✅ DB 저장 호출
✅ Entity → ResponseDto 변환

                         ↓ Notice Entity

┌────────────────────────────────────────────────────────────────┐
│  4️⃣ NoticeRepository.save()                                   │
│  (NoticeRepository.java:12)                                    │
└────────────────────────────────────────────────────────────────┘

@Repository
public interface NoticeRepository extends JpaRepository<Notice, Long> {
    // save() 메서드는 JpaRepository가 제공
}

Spring Data JPA가 자동으로 생성하는 SQL:
INSERT INTO notices (title, content, author, is_pinned, category,
                     image_url, view_count, created_at, updated_at)
VALUES ('2024 봄 축제 안내', '봄 축제가 열립니다!', '학생회장',
        true, '행사', '/uploads/abc123.jpg', 0, NOW(), NOW());

역할:
✅ JPA를 통해 SQL 생성 및 실행
✅ DB에 실제 데이터 저장
✅ ID가 자동 생성되어 Entity에 반영

                         ↓ Saved Entity (ID 포함)

┌────────────────────────────────────────────────────────────────┐
│  5️⃣ NoticeResponseDto.fromEntity()                            │
│  (NoticeResponseDto.java:26-39)                                │
└────────────────────────────────────────────────────────────────┘

public static NoticeResponseDto fromEntity(Notice notice) {
    return NoticeResponseDto.builder()
        .id(notice.getId())                    // ← DB가 생성한 ID
        .title(notice.getTitle())
        .content(notice.getContent())
        .author(notice.getAuthor())
        .isPinned(notice.getIsPinned())
        .viewCount(notice.getViewCount())
        .createdAt(notice.getCreatedAt())      // ← @CreationTimestamp
        .updatedAt(notice.getUpdatedAt())      // ← @UpdateTimestamp
        .category(notice.getCategory())
        .imageUrl(notice.getImageUrl())
        .build();
}

역할:
✅ Entity → DTO 변환
✅ 민감한 내부 정보 숨김
✅ API 응답 형식에 맞게 변환
```

---

### **[출력] HTTP Response**

```json
{
  "id": 1,
  "title": "2024 봄 축제 안내",
  "content": "봄 축제가 열립니다!",
  "author": "학생회장",
  "isPinned": true,
  "viewCount": 0,
  "createdAt": "2025-11-23T10:30:00",
  "updatedAt": "2025-11-23T10:30:00",
  "category": "행사",
  "imageUrl": "/uploads/abc123.jpg"
}
```

Status: `201 Created`

---

## 🔍 데이터 조회 흐름 (GET /api/notices/{id})

```
┌────────────────────────────────────────────────────────────────┐
│  Client → Controller → Service → Repository → Database        │
└────────────────────────────────────────────────────────────────┘

GET /api/notices/1
           ↓
NoticeController.getNoticeById(1)
           ↓
NoticeService.getNoticeById(1)    @Transactional  ← 트랜잭션 시작
    ├── NoticeRepository.findById(1)
    │       ↓ SQL: SELECT * FROM notices WHERE id = 1
    │       ↑ Notice Entity 반환
    │
    ├── notice.incrementViewCount()   ← 조회수 증가 (영속성 컨텍스트에서 자동 UPDATE)
    │
    └── NoticeResponseDto.fromEntity(notice)
           ↑ ResponseDto 반환
           ↓
ResponseEntity.ok(response)
           ↓
HTTP 200 OK + JSON 응답
```

---

## 🔑 핵심 패턴 및 원칙

### 1️⃣ **DTO 패턴 (Data Transfer Object)**
- **RequestDto**: 클라이언트 → 서버 (입력 검증 포함)
- **ResponseDto**: 서버 → 클라이언트 (민감 정보 제외)
- **Entity를 직접 노출하지 않음** → 보안 & 유연성

### 2️⃣ **의존성 주입 (Dependency Injection)**
```java
@RequiredArgsConstructor  // Lombok이 생성자 자동 생성
public class NoticeService {
    private final NoticeRepository noticeRepository;       // ← 주입
    private final FileStorageService fileStorageService;   // ← 주입
}
```

### 3️⃣ **트랜잭션 관리**
- `@Transactional`: 메서드 시작~종료까지 하나의 DB 트랜잭션
- `@Transactional(readOnly = true)`: 읽기 전용 최적화
- 조회수 증가 예시:
  ```java
  @Transactional
  public NoticeResponseDto getNoticeById(Long id) {
      Notice notice = noticeRepository.findById(id).orElseThrow(...);
      notice.incrementViewCount();  // ← Dirty Checking으로 자동 UPDATE
      return NoticeResponseDto.fromEntity(notice);
  }
  ```

### 4️⃣ **Spring Data JPA 쿼리 메서드**
```java
// 메서드 이름만으로 쿼리 자동 생성
List<Notice> findByIsPinnedTrueOrderByCreatedAtDesc();

// 자동 생성되는 SQL:
// SELECT * FROM notices WHERE is_pinned = true ORDER BY created_at DESC
```

---

## 📊 전체 데이터 흐름 요약

```
입력 (Input)
    HTTP Request (JSON/Multipart)
         ↓
    Controller (@Valid 검증)
         ↓
    Service (비즈니스 로직)
         ├── FileStorageService (파일 저장)
         └── Repository (DB 저장)
              ↓
         Database
              ↑
    Service (Entity → DTO 변환)
         ↑
    Controller (HTTP 응답 생성)
         ↑
    HTTP Response (JSON)
출력 (Output)
```

---

## 📚 각 계층의 책임과 역할

### Controller Layer (컨트롤러 계층)
**책임**: HTTP 요청/응답 처리
- REST API 엔드포인트 정의
- 요청 데이터 검증 (`@Valid`)
- HTTP 상태 코드 결정
- Service Layer 호출

**파일**: `NoticeController.java`, `GalleryController.java`

### Service Layer (서비스 계층)
**책임**: 비즈니스 로직 처리
- 트랜잭션 관리
- DTO ↔ Entity 변환
- 복잡한 비즈니스 규칙 처리
- Repository 조합 및 호출

**파일**: `NoticeService.java`, `GalleryService.java`, `FileStorageService.java`

### Repository Layer (레포지토리 계층)
**책임**: 데이터 접근 추상화
- CRUD 작업
- 커스텀 쿼리 메서드
- JPA를 통한 SQL 자동 생성

**파일**: `NoticeRepository.java`, `GalleryRepository.java`

### Entity Layer (엔티티 계층)
**책임**: 데이터베이스 테이블 매핑
- 테이블 구조 정의
- 컬럼 매핑
- 관계 설정
- 비즈니스 로직 (incrementViewCount 등)

**파일**: `Notice.java`, `Gallery.java`

### DTO Layer (DTO 계층)
**책임**: 데이터 전송
- API 입력 형식 정의
- API 출력 형식 정의
- 입력 검증 규칙 (`@NotBlank`, `@Size` 등)
- Entity 노출 방지

**파일**: `NoticeRequestDto.java`, `NoticeResponseDto.java`, `GalleryRequestDto.java`, `GalleryResponseDto.java`

---

## 🎯 설계 원칙

1. **관심사의 분리 (Separation of Concerns)**
   - 각 계층은 명확한 책임을 가짐
   - 계층 간 의존성은 단방향 (Controller → Service → Repository)

2. **DRY (Don't Repeat Yourself)**
   - Lombok을 활용한 보일러플레이트 코드 제거
   - Spring Data JPA의 쿼리 메서드 자동 생성

3. **의존성 역전 원칙 (Dependency Inversion)**
   - 인터페이스를 통한 느슨한 결합
   - Spring의 의존성 주입 활용

4. **단일 책임 원칙 (Single Responsibility)**
   - 각 클래스는 하나의 책임만 가짐
   - FileStorageService는 파일 처리만 담당

---

## 💡 주요 기술 특징

### JPA Dirty Checking (변경 감지)
```java
@Transactional
public NoticeResponseDto getNoticeById(Long id) {
    Notice notice = noticeRepository.findById(id).orElseThrow(...);
    notice.incrementViewCount();  // ← setter 호출만으로 자동 UPDATE
    // 명시적인 save() 호출 불필요!
}
```

트랜잭션 종료 시 JPA가 자동으로 변경된 엔티티를 감지하여 UPDATE 쿼리 실행

### Lombok Builder 패턴
```java
Notice notice = Notice.builder()
    .title("제목")
    .content("내용")
    .author("작성자")
    .build();
```

가독성이 높고 불변성을 보장하는 객체 생성 방식

### Spring Data JPA 쿼리 메서드 네이밍 컨벤션
- `findBy`: SELECT 쿼리
- `OrderBy`: 정렬
- `And`, `Or`: 조건 결합
- `Containing`: LIKE '%keyword%'
- `True`, `False`: Boolean 값

---

이 아키텍처는 **유지보수성**, **테스트 용이성**, **확장성**을 고려한 Spring Boot 표준 설계 패턴을 따릅니다.

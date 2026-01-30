package com.feel.backend.controller;

import com.feel.backend.dto.ResourceFileDto;
import com.feel.backend.service.ResourceFileService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/resources")
@RequiredArgsConstructor
public class ResourceFileController {

    private final ResourceFileService resourceFileService;

    /**
     * 파일 업로드
     * POST /api/resources/upload
     */
    @PostMapping("/upload")
    public ResponseEntity<ResourceFileDto.Response> uploadFile(
            @RequestParam String category,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String description,
            @RequestParam MultipartFile file
    ) {
        ResourceFileDto.Response response = resourceFileService.uploadFile(
                category, title, description, file
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * 여러 파일 업로드
     * POST /api/resources/upload-multiple
     */
    @PostMapping("/upload-multiple")
    public ResponseEntity<List<ResourceFileDto.Response>> uploadMultipleFiles(
            @RequestParam String category,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String description,
            @RequestParam List<MultipartFile> files
    ) {
        List<ResourceFileDto.Response> responses = files.stream()
                .map(file -> resourceFileService.uploadFile(category, title, description, file))
                .toList();
        return ResponseEntity.status(HttpStatus.CREATED).body(responses);
    }

    /**
     * 카테고리별 파일 목록 조회 (페이징)
     * GET /api/resources?category=inspection&page=0&size=20
     */
    @GetMapping
    public ResponseEntity<Page<ResourceFileDto.Response>> getFilesByCategory(
            @RequestParam String category,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Page<ResourceFileDto.Response> files = resourceFileService.getFilesByCategory(category, page, size);
        return ResponseEntity.ok(files);
    }

    /**
     * 카테고리별 전체 파일 목록 조회
     * GET /api/resources/all?category=inspection
     */
    @GetMapping("/all")
    public ResponseEntity<List<ResourceFileDto.Response>> getAllFilesByCategory(
            @RequestParam String category
    ) {
        List<ResourceFileDto.Response> files = resourceFileService.getAllFilesByCategory(category);
        return ResponseEntity.ok(files);
    }

    /**
     * 특정 파일 조회
     * GET /api/resources/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<ResourceFileDto.Response> getFileById(@PathVariable Long id) {
        ResourceFileDto.Response file = resourceFileService.getFileById(id);
        return ResponseEntity.ok(file);
    }

    /**
     * 파일 삭제
     * DELETE /api/resources/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFile(@PathVariable Long id) {
        resourceFileService.deleteFile(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * 카테고리별 파일 개수 조회
     * GET /api/resources/count?category=inspection
     */
    @GetMapping("/count")
    public ResponseEntity<Map<String, Long>> getCountByCategory(@RequestParam String category) {
        long count = resourceFileService.getCountByCategory(category);
        return ResponseEntity.ok(Map.of("count", count));
    }

    /**
     * 전체 카테고리 통계
     * GET /api/resources/stats
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Long>> getStats() {
        return ResponseEntity.ok(Map.of(
                "inspection", resourceFileService.getCountByCategory("inspection"),
                "finance", resourceFileService.getCountByCategory("finance"),
                "gallery", resourceFileService.getCountByCategory("gallery"),
                "study-support", resourceFileService.getCountByCategory("study-support")
        ));
    }
}

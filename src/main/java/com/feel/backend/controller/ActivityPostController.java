package com.feel.backend.controller;

import com.feel.backend.dto.ActivityPostRequestDto;
import com.feel.backend.dto.ActivityPostResponseDto;
import com.feel.backend.entity.ActivityCategory;
import com.feel.backend.service.ActivityPostService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/activities")
@RequiredArgsConstructor
public class ActivityPostController {

    private final ActivityPostService activityPostService;

    /** 카테고리별 목록 조회 (페이징, 정렬: latest | deadline | viewCount) */
    @GetMapping
    public ResponseEntity<Page<ActivityPostResponseDto>> getList(
        @RequestParam(required = false) ActivityCategory category,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "12") int size,
        @RequestParam(defaultValue = "latest") String sort
    ) {
        Page<ActivityPostResponseDto> list = activityPostService.getList(category, page, size, sort);
        return ResponseEntity.ok(list);
    }

    /** 상세 조회 (조회수 증가) */
    @GetMapping("/{id}")
    public ResponseEntity<ActivityPostResponseDto> getById(@PathVariable Long id) {
        ActivityPostResponseDto dto = activityPostService.getById(id);
        return ResponseEntity.ok(dto);
    }

    /** 게시글 생성 (대외활동/공모전: Admin, 팀원모집: 로그인 사용자) */
    @PostMapping
    public ResponseEntity<ActivityPostResponseDto> create(
        @Valid @ModelAttribute ActivityPostRequestDto requestDto,
        @RequestParam(required = false) MultipartFile thumbnail
    ) {
        ActivityPostResponseDto created = activityPostService.create(requestDto, thumbnail);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /** 게시글 수정 */
    @PutMapping("/{id}")
    public ResponseEntity<ActivityPostResponseDto> update(
        @PathVariable Long id,
        @Valid @ModelAttribute ActivityPostRequestDto requestDto,
        @RequestParam(required = false) MultipartFile thumbnail
    ) {
        ActivityPostResponseDto updated = activityPostService.update(id, requestDto, thumbnail);
        return ResponseEntity.ok(updated);
    }

    /** 게시글 수정 (일부 필드만) */
    @PatchMapping("/{id}")
    public ResponseEntity<ActivityPostResponseDto> patch(
        @PathVariable Long id,
        @ModelAttribute ActivityPostRequestDto requestDto,
        @RequestParam(required = false) MultipartFile thumbnail
    ) {
        ActivityPostResponseDto updated = activityPostService.update(id, requestDto, thumbnail);
        return ResponseEntity.ok(updated);
    }

    /** 게시글 삭제 */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        activityPostService.delete(id);
        return ResponseEntity.noContent().build();
    }
}

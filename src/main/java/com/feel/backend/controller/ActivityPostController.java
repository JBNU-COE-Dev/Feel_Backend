package com.feel.backend.controller;

import com.feel.backend.auth.AuthRole;
import com.feel.backend.dto.ActivityPostRequestDto;
import com.feel.backend.dto.ActivityPostResponseDto;
import com.feel.backend.dto.ErrorResponse;
import com.feel.backend.entity.ActivityCategory;
import com.feel.backend.entity.ActivityPost;
import com.feel.backend.entity.User;
import com.feel.backend.service.ActivityPostService;
import com.feel.backend.service.AuthService;
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
    private final AuthService authService;

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

    /** 게시글 생성 (대외활동/공모전: Admin, 팀원모집: USER 또는 ADMIN) */
    @PostMapping
    public ResponseEntity<?> create(
        @Valid @ModelAttribute ActivityPostRequestDto requestDto,
        @RequestParam(required = false) MultipartFile thumbnail,
        @RequestHeader(value = "Authorization", required = false) String authHeader
    ) {
        AuthRole role;
        User currentUser;
        try {
            if (requestDto.getCategory() == ActivityCategory.TEAM_RECRUITMENT) {
                role = authService.getRole(authHeader);
                currentUser = authService.resolveCurrentUserOrNullForAdmin(authHeader);
            } else {
                authService.requireAdmin(authHeader);
                role = AuthRole.ADMIN;
                currentUser = null;
            }
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.builder().message(e.getMessage()).build());
        }
        try {
            ActivityPostResponseDto created = activityPostService.create(requestDto, thumbnail, role, currentUser);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ErrorResponse.builder().message(e.getMessage()).build());
        }
    }

    /** 게시글 수정 */
    @PutMapping("/{id}")
    public ResponseEntity<?> update(
        @RequestHeader(value = "Authorization", required = false) String authHeader,
        @PathVariable Long id,
        @Valid @ModelAttribute ActivityPostRequestDto requestDto,
        @RequestParam(required = false) MultipartFile thumbnail
    ) {
        return doUpdate(authHeader, id, requestDto, thumbnail);
    }

    /** 게시글 수정 (일부 필드만) */
    @PatchMapping("/{id}")
    public ResponseEntity<?> patch(
        @RequestHeader(value = "Authorization", required = false) String authHeader,
        @PathVariable Long id,
        @ModelAttribute ActivityPostRequestDto requestDto,
        @RequestParam(required = false) MultipartFile thumbnail
    ) {
        return doUpdate(authHeader, id, requestDto, thumbnail);
    }

    /** 게시글 삭제 */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(
        @RequestHeader(value = "Authorization", required = false) String authHeader,
        @PathVariable Long id
    ) {
        AuthRole role;
        Long currentUserId;
        try {
            Actor actor = resolveActorForPost(authHeader, id);
            role = actor.role();
            currentUserId = actor.userId();
        } catch (RuntimeException e) {
            return mapAuthOrForbidden(e);
        }
        try {
            activityPostService.delete(id, role, currentUserId);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return mapModifyError(e);
        }
    }

    private ResponseEntity<?> doUpdate(
            String authHeader, Long id, ActivityPostRequestDto requestDto, MultipartFile thumbnail) {
        AuthRole role;
        Long currentUserId;
        try {
            Actor actor = resolveActorForPost(authHeader, id);
            role = actor.role();
            currentUserId = actor.userId();
        } catch (RuntimeException e) {
            return mapAuthOrForbidden(e);
        }
        try {
            ActivityPostResponseDto updated =
                    activityPostService.update(id, requestDto, thumbnail, role, currentUserId);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return mapModifyError(e);
        }
    }

    /**
     * 대상 글 카테고리에 맞게 인증 후 (role, userId) 반환.
     * 대외/공모: ADMIN만. 팀원모집: USER/ADMIN + ownership은 서비스에서 검사.
     */
    private Actor resolveActorForPost(String authHeader, Long id) {
        ActivityPost post = activityPostService.findPost(id);
        if (post.getCategory() != ActivityCategory.TEAM_RECRUITMENT) {
            authService.requireAdmin(authHeader);
            return new Actor(AuthRole.ADMIN, null);
        }
        AuthRole role = authService.getRole(authHeader);
        User user = authService.resolveCurrentUserOrNullForAdmin(authHeader);
        Long userId = user != null ? user.getId() : null;
        activityPostService.assertCanModify(post, role, userId);
        return new Actor(role, userId);
    }

    private ResponseEntity<?> mapAuthOrForbidden(RuntimeException e) {
        if (ActivityPostService.FORBIDDEN_OWN_POST.equals(e.getMessage())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.builder().message(e.getMessage()).build());
        }
        if (e.getMessage() != null && e.getMessage().contains("찾을 수 없습니다")) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.builder().message(e.getMessage()).build());
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ErrorResponse.builder().message(e.getMessage()).build());
    }

    private ResponseEntity<?> mapModifyError(RuntimeException e) {
        if (ActivityPostService.FORBIDDEN_OWN_POST.equals(e.getMessage())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.builder().message(e.getMessage()).build());
        }
        if (e.getMessage() != null && e.getMessage().contains("찾을 수 없습니다")) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.builder().message(e.getMessage()).build());
        }
        if (e.getMessage() != null && e.getMessage().contains("관리자 권한")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.builder().message(e.getMessage()).build());
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse.builder().message(e.getMessage()).build());
    }

    private record Actor(AuthRole role, Long userId) {}
}

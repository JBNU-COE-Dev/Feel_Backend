package com.feel.backend.service;

import com.feel.backend.auth.AuthRole;
import com.feel.backend.dto.ActivityPostRequestDto;
import com.feel.backend.dto.ActivityPostResponseDto;
import com.feel.backend.entity.ActivityCategory;
import com.feel.backend.entity.ActivityPost;
import com.feel.backend.entity.User;
import com.feel.backend.repository.ActivityPostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ActivityPostService {

    private static final String UPLOAD_SUBDIR = "activities";
    public static final String FORBIDDEN_OWN_POST = "본인이 작성한 글만 수정/삭제할 수 있습니다.";

    private final ActivityPostRepository activityPostRepository;
    private final FileStorageService fileStorageService;

    /**
     * 카테고리별 목록 조회 (페이징, 정렬)
     * @param sort latest | deadline | viewCount
     */
    public Page<ActivityPostResponseDto> getList(ActivityCategory category, int page, int size, String sort) {
        Pageable pageable = PageRequest.of(page, size);
        Page<ActivityPost> result;
        if ("deadline".equalsIgnoreCase(sort)) {
            result = activityPostRepository.findByCategoryOrderByEndDateAsc(category, pageable);
        } else if ("viewCount".equalsIgnoreCase(sort) || "views".equalsIgnoreCase(sort)) {
            result = activityPostRepository.findByCategoryOrderByViewCountDesc(category, pageable);
        } else {
            result = activityPostRepository.findByCategoryOrderByCreatedAtDesc(category, pageable);
        }
        return result.map(ActivityPostResponseDto::fromEntity);
    }

    public Page<ActivityPostResponseDto> getByAuthorId(Long authorId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return activityPostRepository.findByAuthorIdOrderByCreatedAtDesc(authorId, pageable)
                .map(ActivityPostResponseDto::fromEntity);
    }

    @Transactional
    public ActivityPostResponseDto getById(Long id) {
        ActivityPost post = findPost(id);
        post.incrementViewCount();
        return ActivityPostResponseDto.fromEntity(post);
    }

    public ActivityPost findPost(Long id) {
        return activityPostRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("요청한 게시글을 찾을 수 없습니다."));
    }

    @Transactional
    public ActivityPostResponseDto create(ActivityPostRequestDto dto, MultipartFile thumbnailFile,
                                          AuthRole role, User currentUser) {
        Long authorId = null;
        String author;

        if (dto.getCategory() == ActivityCategory.TEAM_RECRUITMENT) {
            if (role == AuthRole.USER) {
                if (currentUser == null) {
                    throw new RuntimeException("회원 정보를 찾을 수 없습니다.");
                }
                authorId = currentUser.getId();
                author = currentUser.getNickname();
            } else {
                // ADMIN이 팀원모집 작성 시 요청 author 사용, authorId 없음
                if (dto.getAuthor() == null || dto.getAuthor().isBlank()) {
                    throw new RuntimeException("작성자는 필수입니다.");
                }
                author = dto.getAuthor().trim();
            }
        } else {
            if (dto.getAuthor() == null || dto.getAuthor().isBlank()) {
                throw new RuntimeException("작성자는 필수입니다.");
            }
            author = dto.getAuthor().trim();
        }

        String thumbnailUrl = resolveThumbnailUrl(null, dto, thumbnailFile);

        ActivityPost post = ActivityPost.builder()
                .category(dto.getCategory())
                .title(dto.getTitle())
                .content(dto.getContent())
                .thumbnailUrl(thumbnailUrl)
                .viewCount(0)
                .author(author)
                .authorId(authorId)
                .organization(dto.getOrganization())
                .startDate(dto.getStartDate())
                .endDate(dto.getEndDate())
                .applyUrl(dto.getApplyUrl())
                .headcount(dto.getHeadcount())
                .recruitmentRoles(dto.getRecruitmentRoles())
                .contactUrl(dto.getContactUrl())
                .status(dto.getStatus())
                .build();
        ActivityPost saved = activityPostRepository.save(post);
        return ActivityPostResponseDto.fromEntity(saved);
    }

    @Transactional
    public ActivityPostResponseDto update(Long id, ActivityPostRequestDto dto, MultipartFile thumbnailFile,
                                          AuthRole role, Long currentUserId) {
        ActivityPost post = findPost(id);
        assertCanModify(post, role, currentUserId);

        // USER는 카테고리 변경 불가 (권한 우회 방지). ADMIN은 허용.
        if (role == AuthRole.ADMIN && dto.getCategory() != null) {
            post.setCategory(dto.getCategory());
        }

        post.setTitle(dto.getTitle());
        post.setContent(dto.getContent());

        if (role == AuthRole.ADMIN && dto.getAuthor() != null && !dto.getAuthor().isBlank()) {
            post.setAuthor(dto.getAuthor().trim());
        }

        post.setOrganization(dto.getOrganization());
        post.setStartDate(dto.getStartDate());
        post.setEndDate(dto.getEndDate());
        post.setApplyUrl(dto.getApplyUrl());
        post.setHeadcount(dto.getHeadcount());
        post.setRecruitmentRoles(dto.getRecruitmentRoles());
        post.setContactUrl(dto.getContactUrl());
        post.setStatus(dto.getStatus());

        applyThumbnailUpdate(post, dto, thumbnailFile);

        return ActivityPostResponseDto.fromEntity(post);
    }

    @Transactional
    public void delete(Long id, AuthRole role, Long currentUserId) {
        ActivityPost post = findPost(id);
        assertCanModify(post, role, currentUserId);
        if (post.getThumbnailUrl() != null) {
            String path = fileStorageService.extractFileName(post.getThumbnailUrl());
            fileStorageService.deleteFile(path);
        }
        activityPostRepository.deleteById(id);
    }

    /**
     * 수정/삭제 권한: 대외/공모는 ADMIN만, 팀원모집은 ADMIN 또는 본인.
     */
    public void assertCanModify(ActivityPost post, AuthRole role, Long currentUserId) {
        if (post.getCategory() != ActivityCategory.TEAM_RECRUITMENT) {
            if (role != AuthRole.ADMIN) {
                throw new RuntimeException("관리자 권한이 필요합니다.");
            }
            return;
        }
        if (role == AuthRole.ADMIN) {
            return;
        }
        if (currentUserId == null || post.getAuthorId() == null || !post.getAuthorId().equals(currentUserId)) {
            throw new RuntimeException(FORBIDDEN_OWN_POST);
        }
    }

    private String resolveThumbnailUrl(ActivityPost existing, ActivityPostRequestDto dto, MultipartFile thumbnailFile) {
        if (thumbnailFile != null && !thumbnailFile.isEmpty()) {
            String relativePath = fileStorageService.storeFileInSubdir(thumbnailFile, UPLOAD_SUBDIR);
            return "/uploads/" + relativePath;
        }
        if (dto.getThumbnailUrl() != null && !dto.getThumbnailUrl().isBlank()) {
            return dto.getThumbnailUrl();
        }
        return existing != null ? existing.getThumbnailUrl() : null;
    }

    private void applyThumbnailUpdate(ActivityPost post, ActivityPostRequestDto dto, MultipartFile thumbnailFile) {
        if (thumbnailFile != null && !thumbnailFile.isEmpty()) {
            if (post.getThumbnailUrl() != null) {
                String oldPath = fileStorageService.extractFileName(post.getThumbnailUrl());
                fileStorageService.deleteFile(oldPath);
            }
            String relativePath = fileStorageService.storeFileInSubdir(thumbnailFile, UPLOAD_SUBDIR);
            post.setThumbnailUrl("/uploads/" + relativePath);
        } else if (dto.getThumbnailUrl() != null) {
            post.setThumbnailUrl(dto.getThumbnailUrl());
        }
    }
}

package com.feel.backend.service;

import com.feel.backend.dto.MatchingRequestDto;
import com.feel.backend.dto.MatchingResponseDto;
import com.feel.backend.entity.MatchingPost;
import com.feel.backend.repository.MatchingPostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MatchingService {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DISPLAY_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy.MM.dd");
    private static final String DEFAULT_ORGANIZER_NAME = "FeeL";
    private static final String DEFAULT_THUMBNAIL = "https://via.placeholder.com/280x350";

    private final MatchingPostRepository matchingPostRepository;

    public Page<MatchingResponseDto> getMatchings(String type, Integer page, Integer limit, String search, String category) {
        int pageIndex = (page == null || page <= 0) ? 0 : page - 1;
        int size = (limit == null || limit <= 0) ? 10 : limit;
        Pageable pageable = PageRequest.of(pageIndex, size, Sort.by("createdAt").descending());

        Page<MatchingPost> results = matchingPostRepository.search(
            normalize(type),
            normalize(category),
            normalize(search),
            pageable
        );

        return results.map(this::toResponseDto);
    }

    @Transactional
    public MatchingResponseDto getMatchingDetail(Long id) {
        MatchingPost post = matchingPostRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("매칭 글을 찾을 수 없습니다. ID: " + id));
        post.incrementViewCount();
        return toResponseDto(post);
    }

    @Transactional
    public MatchingResponseDto createMatching(MatchingRequestDto requestDto) {
        MatchingPost post = MatchingPost.builder()
            .title(requestDto.getTitle())
            .type(requestDto.getType())
            .category(requestDto.getCategory())
            .description(requestDto.getDescription())
            .maxMembers(requestDto.getMaxMembers())
            .currentMembers(defaultIfNull(requestDto.getCurrentMembers(), 0))
            .deadline(parseDate(requestDto.getDeadline()))
            .thumbnailUrl(defaultIfBlank(requestDto.getThumbnail(), DEFAULT_THUMBNAIL))
            .organizerId(defaultIfNull(requestDto.getOrganizerId(), 1L))
            .organizerName(defaultIfBlank(requestDto.getOrganizerName(), DEFAULT_ORGANIZER_NAME))
            .organizerLogoUrl(requestDto.getOrganizerLogo())
            .companyType(requestDto.getCompanyType())
            .targetAudience(requestDto.getTargetAudience())
            .applicationStart(parseDisplayDate(requestDto.getApplicationStart()))
            .applicationEnd(parseDisplayDate(requestDto.getApplicationEnd()))
            .activityPeriod(requestDto.getActivityPeriod())
            .recruitCount(requestDto.getRecruitCount())
            .activityArea(requestDto.getActivityArea())
            .preferredSkills(requestDto.getPreferredSkills())
            .homepage(requestDto.getHomepage())
            .benefits(requestDto.getBenefits())
            .additionalBenefits(requestDto.getAdditionalBenefits())
            .tagsInterest(joinTags(requestDto.getTagsInterest()))
            .tagsActivity(joinTags(requestDto.getTagsActivity()))
            .viewCount(0)
            .bookmarkCount(0)
            .build();

        MatchingPost saved = matchingPostRepository.save(post);
        return toResponseDto(saved);
    }

    @Transactional
    public MatchingResponseDto updateMatching(Long id, MatchingRequestDto requestDto) {
        MatchingPost post = matchingPostRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("매칭 글을 찾을 수 없습니다. ID: " + id));

        post.setTitle(requestDto.getTitle());
        post.setType(requestDto.getType());
        post.setCategory(requestDto.getCategory());
        post.setDescription(requestDto.getDescription());
        post.setMaxMembers(requestDto.getMaxMembers());
        post.setCurrentMembers(defaultIfNull(requestDto.getCurrentMembers(), post.getCurrentMembers()));
        post.setDeadline(parseDate(requestDto.getDeadline()));
        post.setThumbnailUrl(defaultIfBlank(requestDto.getThumbnail(), post.getThumbnailUrl()));
        post.setOrganizerId(defaultIfNull(requestDto.getOrganizerId(), post.getOrganizerId()));
        post.setOrganizerName(defaultIfBlank(requestDto.getOrganizerName(), post.getOrganizerName()));
        post.setOrganizerLogoUrl(requestDto.getOrganizerLogo());
        post.setCompanyType(requestDto.getCompanyType());
        post.setTargetAudience(requestDto.getTargetAudience());
        post.setApplicationStart(parseDisplayDate(requestDto.getApplicationStart()));
        post.setApplicationEnd(parseDisplayDate(requestDto.getApplicationEnd()));
        post.setActivityPeriod(requestDto.getActivityPeriod());
        post.setRecruitCount(requestDto.getRecruitCount());
        post.setActivityArea(requestDto.getActivityArea());
        post.setPreferredSkills(requestDto.getPreferredSkills());
        post.setHomepage(requestDto.getHomepage());
        post.setBenefits(requestDto.getBenefits());
        post.setAdditionalBenefits(requestDto.getAdditionalBenefits());
        post.setTagsInterest(joinTags(requestDto.getTagsInterest()));
        post.setTagsActivity(joinTags(requestDto.getTagsActivity()));

        return toResponseDto(post);
    }

    @Transactional
    public void deleteMatching(Long id) {
        MatchingPost post = matchingPostRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("매칭 글을 찾을 수 없습니다. ID: " + id));
        matchingPostRepository.delete(post);
    }

    private MatchingResponseDto toResponseDto(MatchingPost post) {
        return MatchingResponseDto.builder()
            .id(post.getId())
            .title(post.getTitle())
            .type(post.getType())
            .category(post.getCategory())
            .description(post.getDescription())
            .members(formatMembers(post.getCurrentMembers(), post.getMaxMembers()))
            .deadline(formatDate(post.getDeadline()))
            .dDay(calculateDday(post.getDeadline()))
            .views(post.getViewCount())
            .comments(0)
            .thumbnail(defaultIfBlank(post.getThumbnailUrl(), DEFAULT_THUMBNAIL))
            .organizerId(post.getOrganizerId())
            .organizerName(defaultIfBlank(post.getOrganizerName(), DEFAULT_ORGANIZER_NAME))
            .organizerLogo(post.getOrganizerLogoUrl())
            .companyType(post.getCompanyType())
            .targetAudience(post.getTargetAudience())
            .applicationStart(formatDisplayDate(post.getApplicationStart()))
            .applicationEnd(formatDisplayDate(post.getApplicationEnd()))
            .activityPeriod(post.getActivityPeriod())
            .recruitCount(post.getRecruitCount())
            .activityArea(post.getActivityArea())
            .preferredSkills(post.getPreferredSkills())
            .homepage(post.getHomepage())
            .benefits(post.getBenefits())
            .additionalBenefits(post.getAdditionalBenefits())
            .tagsInterest(splitTags(post.getTagsInterest()))
            .tagsActivity(splitTags(post.getTagsActivity()))
            .build();
    }

    private Integer calculateDday(LocalDate deadline) {
        if (deadline == null) {
            return null;
        }
        long days = ChronoUnit.DAYS.between(LocalDate.now(), deadline);
        return (int) days;
    }

    private String formatDate(LocalDate date) {
        return date == null ? null : date.format(DATE_FORMAT);
    }

    private String formatDisplayDate(LocalDate date) {
        return date == null ? null : date.format(DISPLAY_DATE_FORMAT);
    }

    private String formatMembers(Integer currentMembers, Integer maxMembers) {
        int current = currentMembers == null ? 0 : currentMembers;
        if (maxMembers == null || maxMembers == 0) {
            return current + "/-";
        }
        return current + "/" + maxMembers;
    }

    private LocalDate parseDate(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return LocalDate.parse(value, DATE_FORMAT);
    }

    private LocalDate parseDisplayDate(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return LocalDate.parse(value.replace(".", "-"), DATE_FORMAT);
    }

    private String joinTags(List<String> tags) {
        if (tags == null || tags.isEmpty()) {
            return null;
        }
        return String.join(",", tags);
    }

    private List<String> splitTags(String tags) {
        if (tags == null || tags.isBlank()) {
            return Collections.emptyList();
        }
        String[] parts = tags.split(",");
        List<String> result = new ArrayList<>();
        for (String part : parts) {
            String trimmed = part.trim();
            if (!trimmed.isEmpty()) {
                result.add(trimmed);
            }
        }
        return result;
    }

    private String normalize(String value) {
        return (value == null || value.isBlank()) ? null : value;
    }

    private String defaultIfBlank(String value, String fallback) {
        return (value == null || value.isBlank()) ? fallback : value;
    }

    private Integer defaultIfNull(Integer value, Integer fallback) {
        return value == null ? fallback : value;
    }

    private Long defaultIfNull(Long value, Long fallback) {
        return value == null ? fallback : value;
    }
}

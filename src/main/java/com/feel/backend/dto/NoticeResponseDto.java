package com.feel.backend.dto;

import com.feel.backend.entity.Notice;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NoticeResponseDto {

    private Long id;
    private String title;
    private String content;
    private String author;
    private Boolean isPinned;
    private Integer viewCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String category;
    private String imageUrl;

    public static NoticeResponseDto fromEntity(Notice notice) {
        return NoticeResponseDto.builder()
            .id(notice.getId())
            .title(notice.getTitle())
            .content(notice.getContent())
            .author(notice.getAuthor())
            .isPinned(notice.getIsPinned())
            .viewCount(notice.getViewCount())
            .createdAt(notice.getCreatedAt())
            .updatedAt(notice.getUpdatedAt())
            .category(notice.getCategory())
            .imageUrl(notice.getImageUrl())
            .build();
    }
}

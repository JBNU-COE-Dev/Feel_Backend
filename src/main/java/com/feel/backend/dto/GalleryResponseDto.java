package com.feel.backend.dto;

import com.feel.backend.entity.Gallery;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GalleryResponseDto {

    private Long id;
    private String title;
    private String description;
    private String imageUrl;
    private String photographer;
    private Integer viewCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String category;

    public static GalleryResponseDto fromEntity(Gallery gallery) {
        return GalleryResponseDto.builder()
                .id(gallery.getId())
                .title(gallery.getTitle())
                .description(gallery.getDescription())
                .imageUrl(gallery.getImageUrl())
                .photographer(gallery.getPhotographer())
                .viewCount(gallery.getViewCount())
                .createdAt(gallery.getCreatedAt())
                .updatedAt(gallery.getUpdatedAt())
                .category(gallery.getCategory())
                .build();
    }
}

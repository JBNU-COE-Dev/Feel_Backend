package com.feel.backend.dto;

import lombok.*;

import java.time.LocalDateTime;

public class ResourceFileDto {

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Request {
        private String category;
        private String title;
        private String description;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Response {
        private Long id;
        private String category;
        private String originalFileName;
        private String fileUrl;
        private String fileType;
        private Long fileSize;
        private String title;
        private String description;
        private LocalDateTime createdAt;
    }
}

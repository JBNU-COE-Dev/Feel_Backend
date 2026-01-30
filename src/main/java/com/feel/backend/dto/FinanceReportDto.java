package com.feel.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;

public class FinanceReportDto {

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Request {
        @NotBlank(message = "제목은 필수입니다.")
        private String title;

        private String description;

        @NotBlank(message = "파일명은 필수입니다.")
        private String fileName;

        @NotBlank(message = "파일 URL은 필수입니다.")
        private String fileUrl;

        @NotNull(message = "파일 크기는 필수입니다.")
        private Long fileSize;

        @NotNull(message = "연도는 필수입니다.")
        private Integer year;

        @NotNull(message = "월은 필수입니다.")
        private Integer month;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Response {
        private Long id;
        private String title;
        private String description;
        private String fileName;
        private String fileUrl;
        private Long fileSize;
        private Integer year;
        private Integer month;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UploadResponse {
        private String fileUrl;
        private String fileName;
        private Long fileSize;
    }
}

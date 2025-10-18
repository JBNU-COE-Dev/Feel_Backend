package com.feel.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GalleryRequestDto {

    @NotBlank(message = "제목은 필수입니다")
    @Size(max = 200, message = "제목은 200자를 초과할 수 없습니다")
    private String title;

    @Size(max = 1000, message = "설명은 1000자를 초과할 수 없습니다")
    private String description;

    @Size(max = 50, message = "촬영자는 50자를 초과할 수 없습니다")
    private String photographer;

    @Size(max = 50, message = "카테고리는 50자를 초과할 수 없습니다")
    private String category;
}

package com.feel.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MatchingRequestDto {
    @NotBlank(message = "제목은 필수입니다.")
    @Size(max = 200, message = "제목은 200자 이하여야 합니다.")
    private String title;

    @NotBlank(message = "타입은 필수입니다.")
    @Size(max = 50, message = "타입은 50자 이하여야 합니다.")
    private String type;

    @Size(max = 50, message = "카테고리는 50자 이하여야 합니다.")
    private String category;

    private String description;
    private Integer maxMembers;
    private Integer currentMembers;
    private String deadline;
    private String thumbnail;
    private Long organizerId;
    private String organizerName;
    private String companyType;
    private String targetAudience;
    private String applicationStart;
    private String applicationEnd;
    private String activityPeriod;
    private String recruitCount;
    private String activityArea;
    private String homepage;
    private List<String> tagsActivity;
}

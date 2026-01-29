package com.feel.backend.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MatchingResponseDto {
    private Long id;
    private String title;
    private String type;
    private String category;
    private String description;
    private String members;
    private String deadline;
    private Integer dDay;
    private Integer views;
    private Integer comments;
    private String thumbnail;
    private Long organizerId;
    private String organizerName;
    private String organizerLogo;
    private String companyType;
    private String targetAudience;
    private String applicationStart;
    private String applicationEnd;
    private String activityPeriod;
    private String recruitCount;
    private String activityArea;
    private String preferredSkills;
    private String homepage;
    private String benefits;
    private String additionalBenefits;
    private List<String> tagsInterest;
    private List<String> tagsActivity;
}

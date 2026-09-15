package com.feel.backend.dto;

import com.feel.backend.entity.User;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminUserResponseDto {

    private Long id;
    private String email;
    private String nickname;
    private LocalDateTime createdAt;
    private long postCount;

    public static AdminUserResponseDto fromEntity(User user, long postCount) {
        return AdminUserResponseDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                .nickname(user.getNickname())
                .createdAt(user.getCreatedAt())
                .postCount(postCount)
                .build();
    }
}

package com.feel.backend.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VerifyResponse {
    private boolean valid;
    /** users 테이블 PK (일반 회원). ADMIN은 null */
    private Long id;
    private String username; // 이메일
    private String nickname; // users 테이블 닉네임 (있으면)
    /** ADMIN | USER */
    private String role;
}

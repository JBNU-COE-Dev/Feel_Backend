package com.feel.backend.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VerifyResponse {
    private boolean valid;
    private String username;
}

package com.feel.backend.service;

import com.feel.backend.dto.LoginResponse;
import com.feel.backend.entity.AdminUser;
import com.feel.backend.repository.AdminUserRepository;
import com.feel.backend.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AdminUserRepository adminUserRepository;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;

    public LoginResponse login(String username, String password) {
        AdminUser user = adminUserRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("사용자명 또는 비밀번호가 올바르지 않습니다."));

        boolean passwordMatches = passwordEncoder.matches(password, user.getPassword());
        if (!passwordMatches) {
            // 디버깅을 위한 로그 (프로덕션에서는 제거)
            System.err.println("비밀번호 검증 실패 - username: " + username);
            System.err.println("저장된 해시: " + user.getPassword());
            System.err.println("입력된 비밀번호와 매칭: " + passwordMatches);
            throw new RuntimeException("사용자명 또는 비밀번호가 올바르지 않습니다.");
        }

        String token = jwtUtil.generateToken(username);

        return LoginResponse.builder()
                .token(token)
                .username(username)
                .build();
    }

    public boolean validateToken(String token) {
        return jwtUtil.validateToken(token);
    }

    public String getUsernameFromToken(String token) {
        return jwtUtil.getUsernameFromToken(token);
    }

    public void logout(String token) {
        // JWT는 stateless이므로 서버 측에서 별도 처리 불필요
        // 필요시 토큰 블랙리스트를 구현할 수 있음
    }
}

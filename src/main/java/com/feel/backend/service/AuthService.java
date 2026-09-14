package com.feel.backend.service;

import com.feel.backend.auth.AuthRole;
import com.feel.backend.dto.LoginResponse;
import com.feel.backend.entity.AdminUser;
import com.feel.backend.entity.User;
import com.feel.backend.repository.AdminUserRepository;
import com.feel.backend.repository.UserRepository;
import com.feel.backend.service.GoogleTokenVerifier.GoogleTokenInfo;
import com.feel.backend.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

    private final AdminUserRepository adminUserRepository;
    private final UserRepository userRepository;
    private final GoogleTokenVerifier googleTokenVerifier;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;

    public LoginResponse login(String username, String password) {
        AdminUser user = adminUserRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("사용자명 또는 비밀번호가 올바르지 않습니다."));

        boolean passwordMatches = passwordEncoder.matches(password, user.getPassword());
        if (!passwordMatches) {
            throw new RuntimeException("사용자명 또는 비밀번호가 올바르지 않습니다.");
        }

        String token = jwtUtil.generateToken(username, AuthRole.ADMIN);

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

    /**
     * 토큰 검증 후 사용자 정보 반환 (username=email, nickname, role)
     */
    public record VerifyUserInfo(String username, String nickname, AuthRole role) {}

    public VerifyUserInfo verifyAndGetUserInfo(String token) {
        if (!validateToken(token)) {
            throw new RuntimeException("유효하지 않은 토큰입니다.");
        }
        AuthRole role = jwtUtil.getRoleFromToken(token);
        if (role == null) {
            throw new RuntimeException("유효하지 않은 토큰입니다. 다시 로그인해주세요.");
        }
        String username = getUsernameFromToken(token);
        String nickname = userRepository.findByEmail(username)
                .map(User::getNickname)
                .orElse(null);
        return new VerifyUserInfo(username, nickname, role);
    }

    public void logout(String token) {
        // JWT는 stateless이므로 서버 측에서 별도 처리 불필요
        // 필요시 토큰 블랙리스트를 구현할 수 있음
    }

    /**
     * Authorization 헤더 검증 후 ADMIN 전용. 기존 쓰기 API용.
     */
    public String validateAuthHeader(String authHeader) {
        return requireAdmin(authHeader);
    }

    public String requireAdmin(String authHeader) {
        String token = extractAndValidateToken(authHeader);
        AuthRole role = jwtUtil.getRoleFromToken(token);
        if (role != AuthRole.ADMIN) {
            throw new RuntimeException("관리자 권한이 필요합니다.");
        }
        return token;
    }

    /**
     * 로그인 회원(USER) 또는 관리자(ADMIN) 허용.
     */
    public String requireUser(String authHeader) {
        String token = extractAndValidateToken(authHeader);
        AuthRole role = jwtUtil.getRoleFromToken(token);
        if (role != AuthRole.USER && role != AuthRole.ADMIN) {
            throw new RuntimeException("로그인이 필요합니다.");
        }
        return token;
    }

    private String extractAndValidateToken(String authHeader) {
        if (authHeader == null || authHeader.isBlank()) {
            throw new RuntimeException("인증 토큰이 필요합니다.");
        }
        if (!authHeader.startsWith("Bearer ")) {
            throw new RuntimeException("유효하지 않은 토큰 형식입니다.");
        }
        String token = authHeader.substring(7);
        if (!validateToken(token)) {
            throw new RuntimeException("유효하지 않은 토큰입니다.");
        }
        if (jwtUtil.getRoleFromToken(token) == null) {
            throw new RuntimeException("유효하지 않은 토큰입니다. 다시 로그인해주세요.");
        }
        return token;
    }

    /**
     * Google ID 토큰으로 로그인 (@jbnu.ac.kr 도메인만 허용)
     * - 기존 사용자: JWT 발급
     * - 신규 사용자: needSignup=true 반환 (닉네임 입력 후 signup 호출)
     */
    public LoginResponse googleLogin(String idToken) {
        GoogleTokenInfo info = googleTokenVerifier.verify(idToken);
        String email = info.getEmail();
        if (email == null || email.isBlank()) {
            throw new RuntimeException("이메일 정보를 가져올 수 없습니다.");
        }
        if (!GoogleTokenVerifier.isAllowedEmail(email)) {
            throw new RuntimeException("전북대학교 웹메일(@jbnu.ac.kr)로만 로그인할 수 있습니다.");
        }

        return userRepository.findByEmail(email)
                .map(user -> LoginResponse.builder()
                        .token(jwtUtil.generateToken(email, AuthRole.USER))
                        .username(email)
                        .nickname(user.getNickname())
                        .needSignup(false)
                        .build())
                .orElse(LoginResponse.builder()
                        .needSignup(true)
                        .email(email)
                        .build());
    }

    /**
     * 회원가입 완료 (닉네임 저장 후 JWT 발급)
     */
    @Transactional
    public LoginResponse completeSignup(String idToken, String nickname) {
        GoogleTokenInfo info = googleTokenVerifier.verify(idToken);
        String email = info.getEmail();
        if (email == null || email.isBlank()) {
            throw new RuntimeException("이메일 정보를 가져올 수 없습니다.");
        }
        if (!GoogleTokenVerifier.isAllowedEmail(email)) {
            throw new RuntimeException("전북대학교 웹메일(@jbnu.ac.kr)로만 가입할 수 있습니다.");
        }
        if (userRepository.existsByEmail(email)) {
            throw new RuntimeException("이미 가입된 이메일입니다.");
        }
        if (userRepository.existsByNickname(nickname)) {
            throw new RuntimeException("이미 사용 중인 닉네임입니다.");
        }

        User user = User.builder()
                .email(email)
                .nickname(nickname.trim())
                .build();
        userRepository.save(user);

        String token = jwtUtil.generateToken(email, AuthRole.USER);
        return LoginResponse.builder()
                .token(token)
                .username(email)
                .nickname(user.getNickname())
                .needSignup(false)
                .build();
    }
}

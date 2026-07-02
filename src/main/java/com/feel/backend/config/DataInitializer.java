package com.feel.backend.config;

import com.feel.backend.entity.AdminUser;
import com.feel.backend.repository.AdminUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final AdminUserRepository adminUserRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        // Admin 계정 관리는 별도 관리자 개입으로 처리
        // 자동 계정 생성 기능 제거 (보안상 이유)
    }
}

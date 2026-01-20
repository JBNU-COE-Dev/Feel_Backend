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
        // 관리자 계정이 없으면 생성, 있으면 비밀번호 업데이트
        adminUserRepository.findByUsername("admin").ifPresentOrElse(
            existingAdmin -> {
                // 기존 계정이 있으면 비밀번호를 올바른 BCrypt 해시로 업데이트
                String correctHash = passwordEncoder.encode("admin123");
                // 비밀번호가 올바르게 해싱되어 있는지 확인
                if (!passwordEncoder.matches("admin123", existingAdmin.getPassword())) {
                    existingAdmin.setPassword(correctHash);
                    adminUserRepository.save(existingAdmin);
                    System.out.println("기존 관리자 계정의 비밀번호가 올바른 BCrypt 해시로 업데이트되었습니다.");
                } else {
                    System.out.println("관리자 계정이 이미 올바른 비밀번호 해시를 가지고 있습니다.");
                }
            },
            () -> {
                // 계정이 없으면 생성
                AdminUser admin = AdminUser.builder()
                        .username("admin")
                        .password(passwordEncoder.encode("admin123"))
                        .build();
                adminUserRepository.save(admin);
                System.out.println("기본 관리자 계정이 생성되었습니다. (username: admin, password: admin123)");
            }
        );
    }
}

package com.jpdictionary.auth.config;

import com.jpdictionary.auth.entity.User;
import com.jpdictionary.auth.enums.Role;
import com.jpdictionary.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@Profile("dev")
@RequiredArgsConstructor
@Slf4j
public class AuthDemoDataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        seedUserIfMissing(
                "komorebi_user",
                "user@komorebi.jp",
                "User@123",
                "Nguyễn Minh Anh",
                Role.USER,
                "N3",
                "Đang chinh phục JLPT N3 cùng từ điển Komorebi."
        );
        seedUserIfMissing(
                "komorebi_admin",
                "admin@komorebi.jp",
                "Admin@123",
                "Quản trị viên Komorebi",
                Role.ADMIN,
                "N1",
                "Quản trị hệ thống và kiểm duyệt dữ liệu từ điển."
        );
    }

    private void seedUserIfMissing(
            String username,
            String email,
            String rawPassword,
            String displayName,
            Role role,
            String jlptTarget,
            String bio
    ) {
        if (userRepository.existsByUsernameIgnoreCase(username)
                || userRepository.existsByEmailIgnoreCase(email)) {
            return;
        }
        User user = User.builder()
                .username(username)
                .email(email)
                .passwordHash(passwordEncoder.encode(rawPassword))
                .displayName(displayName)
                .role(role)
                .jlptTarget(jlptTarget)
                .bio(bio)
                .active(true)
                .build();
        userRepository.save(user);
        log.info("Seeded dev account: username={}, email={}, role={}", username, email, role);
    }
}


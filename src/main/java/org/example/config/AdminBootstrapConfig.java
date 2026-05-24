package org.example.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.entity.Role;
import org.example.entity.User;
import org.example.entity.UserStatus;
import org.example.mapper.utils.UserMapperUtil;
import org.example.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class AdminBootstrapConfig {

    public static final String DEFAULT_ADMIN_EMAIL = "admin@educode.local";
    public static final String DEFAULT_ADMIN_PASSWORD = "Admin123!";

    private final UserRepository userRepository;
    private final UserMapperUtil userMapperUtil;

    @Bean
    CommandLineRunner ensureDefaultAdminUser() {
        return args -> {
            if (userRepository.findByEmail(DEFAULT_ADMIN_EMAIL).isPresent()) {
                return;
            }
            User admin = User.builder()
                    .email(DEFAULT_ADMIN_EMAIL)
                    .password(userMapperUtil.getEncodedPassword(DEFAULT_ADMIN_PASSWORD))
                    .fullName("Администратор системы")
                    .role(Role.ADMIN)
                    .status(UserStatus.ACTIVE)
                    .studentGroup("")
                    .build();
            userRepository.save(admin);
            log.info("Создан учётная запись администратора: {} / {}", DEFAULT_ADMIN_EMAIL, DEFAULT_ADMIN_PASSWORD);
        };
    }
}

package com.foodordering.auth.infrastructure.config;

import com.foodordering.auth.domain.model.User;
import com.foodordering.auth.domain.model.UserRole;
import com.foodordering.auth.domain.model.UserStatus;
import com.foodordering.auth.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

/**
 * Admin Data Loader
 * Tự động tạo tài khoản Admin mặc định khi ứng dụng khởi động lần đầu
 */
@Configuration
@RequiredArgsConstructor
@Slf4j
public class AdminDataLoader {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Bean
    public CommandLineRunner initAdmin() {
        return args -> {
            String adminEmail = "admin@foodorder.com";
            
            Optional<User> adminOptional = userRepository.findByEmail(adminEmail);
            
            if (adminOptional.isEmpty()) {
                log.info("[ADMIN-LOADER] Không tìm thấy tài khoản admin. Đang tạo tài khoản mặc định...");
                
                User admin = User.builder()
                        .email(adminEmail)
                        .password(passwordEncoder.encode("admin123"))
                        .fullName("System Administrator")
                        .phoneNumber("0123456789")
                        .role(UserRole.ADMIN)
                        .status(UserStatus.ACTIVE)
                        .loyaltyPoints(0)
                        .build();
                
                userRepository.save(admin);
                log.info("[ADMIN-LOADER] ✅ Đã tạo tài khoản Admin thành công: {} / admin123", adminEmail);
            } else {
                User admin = adminOptional.get();
                // Đảm bảo user này có role ADMIN (phòng trường hợp trước đó bị đổi role)
                if (admin.getRole() != UserRole.ADMIN) {
                    admin.setRole(UserRole.ADMIN);
                    userRepository.save(admin);
                    log.info("[ADMIN-LOADER] 🛡️ Đã cập nhật lại quyền ADMIN cho tài khoản: {}", adminEmail);
                }
            }
        };
    }
}

package com.foodordering.auth.application.usecase;

import com.foodordering.auth.application.dto.AuthResponseDto;
import com.foodordering.auth.application.dto.LoginRequestDto;
import com.foodordering.auth.domain.model.User;
import com.foodordering.auth.domain.repository.UserRepository;
import com.foodordering.auth.infrastructure.config.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Login Use Case - Application Layer
 * Xử lý logic đăng nhập
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class LoginUseCase {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    /**
     * Thực thi use case: Đăng nhập
     * 
     * @param request DTO chứa email và password
     * @return AuthResponseDto chứa token và thông tin user
     * @throws RuntimeException nếu thông tin đăng nhập không hợp lệ
     */
    public AuthResponseDto execute(LoginRequestDto request) {
        log.info("[LOGIN] Bắt đầu đăng nhập cho email: {}", request.getEmail());

        // Tìm user theo email
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> {
                    log.error("[LOGIN] Không tìm thấy user với email: {}", request.getEmail());
                    return new RuntimeException("Email hoặc mật khẩu không đúng");
                });

        // Kiểm tra password
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            log.error("[LOGIN] Password không đúng cho email: {}", request.getEmail());
            throw new RuntimeException("Email hoặc mật khẩu không đúng");
        }

        // Kiểm tra user có active không
        if (!user.isActive()) {
            log.error("[LOGIN] User không active: {}", request.getEmail());
            throw new RuntimeException("Tài khoản đã bị vô hiệu hóa");
        }

        // 4. Kiểm tra Role an toàn
        String roleName = (user.getRole() != null) ? user.getRole().name() : "CUSTOMER";

        // 5. Tạo JWT token
        String token = jwtService.generateToken(
                user.getEmail(),
                roleName,
                user.getId()
        );

        log.info("[LOGIN] Đăng nhập thành công cho user: {} với role: {}", 
                user.getEmail(), roleName);

        // 6. Trả về response
        return AuthResponseDto.fromTokenAndUser(
                token,
                user.getId(),
                user.getEmail(),
                user.getFullName(),
                roleName
        );
    }
}

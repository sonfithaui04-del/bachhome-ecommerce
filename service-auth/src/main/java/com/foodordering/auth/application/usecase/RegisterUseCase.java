package com.foodordering.auth.application.usecase;

import com.foodordering.auth.application.dto.AuthResponseDto;
import com.foodordering.auth.application.dto.RegisterRequestDto;
import com.foodordering.auth.domain.model.User;
import com.foodordering.auth.domain.model.UserRole;
import com.foodordering.auth.domain.repository.UserRepository;
import com.foodordering.auth.infrastructure.config.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Register Use Case - Application Layer
 * Xử lý logic đăng ký người dùng mới
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RegisterUseCase {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    /**
     * Thực thi use case: Đăng ký user mới
     * 
     * @param request DTO chứa thông tin đăng ký
     * @return AuthResponseDto chứa token và thông tin user
     * @throws RuntimeException nếu email đã tồn tại
     */
    @Transactional
    public AuthResponseDto execute(RegisterRequestDto request) {
        log.info("[REGISTER] Bắt đầu đăng ký user với email: {}", request.getEmail());

        // Kiểm tra email đã tồn tại chưa
        if (userRepository.existsByEmail(request.getEmail())) {
            log.error("[REGISTER] Email đã tồn tại: {}", request.getEmail());
            throw new RuntimeException("Email đã được sử dụng");
        }

        // Tạo User entity
        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName())
                .phoneNumber(request.getPhoneNumber())
                .role(UserRole.CUSTOMER)  // Mặc định là CUSTOMER
                .build();

        // Lưu vào database
        User savedUser = userRepository.save(user);
        log.info("[REGISTER] Đã lưu user thành công với ID: {}", savedUser.getId());

        // Tạo JWT token
        String token = jwtService.generateToken(
                savedUser.getEmail(),
                savedUser.getRole().name(),
                savedUser.getId()
        );

        log.info("[REGISTER] Đăng ký thành công cho user: {}", savedUser.getEmail());

        // Trả về response
        return AuthResponseDto.fromTokenAndUser(
                token,
                savedUser.getId(),
                savedUser.getEmail(),
                savedUser.getFullName(),
                savedUser.getRole().name()
        );
    }
}

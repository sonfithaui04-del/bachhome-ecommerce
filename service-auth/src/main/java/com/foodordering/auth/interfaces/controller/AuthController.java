package com.foodordering.auth.interfaces.controller;

import com.foodordering.auth.application.dto.AuthResponseDto;
import com.foodordering.auth.application.dto.LoginRequestDto;
import com.foodordering.auth.application.dto.RegisterRequestDto;
import com.foodordering.auth.application.usecase.LoginUseCase;
import com.foodordering.auth.application.usecase.RegisterUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Auth Controller - REST API Endpoints
 * Controller không gọi trực tiếp repository, mà gọi Use Cases
 */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Authentication", description = "API cho đăng ký và đăng nhập")
public class AuthController {

    private final RegisterUseCase registerUseCase;
    private final LoginUseCase loginUseCase;

    /**
     * Endpoint đăng ký user mới
     * POST /auth/register
     */
    @PostMapping("/register")
    @Operation(summary = "Đăng ký tài khoản mới", description = "Tạo tài khoản người dùng mới và trả về JWT token")
    public ResponseEntity<AuthResponseDto> register(@Valid @RequestBody RegisterRequestDto request) {
        log.info("[AUTH-CONTROLLER] Nhận request đăng ký: {}", request.getEmail());
        
        try {
            AuthResponseDto response = registerUseCase.execute(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException e) {
            log.error("[AUTH-CONTROLLER] Lỗi đăng ký: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * Endpoint đăng nhập
     * POST /auth/login
     */
    @PostMapping("/login")
    @Operation(summary = "Đăng nhập", description = "Đăng nhập và nhận JWT token")
    public ResponseEntity<AuthResponseDto> login(@Valid @RequestBody LoginRequestDto request) {
        log.info("[AUTH-CONTROLLER] Nhận request đăng nhập: {}", request.getEmail());
        
        try {
            AuthResponseDto response = loginUseCase.execute(request);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            log.error("[AUTH-CONTROLLER] Lỗi đăng nhập: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * Health check endpoint
     * GET /auth/health
     */
    @GetMapping("/health")
    @Operation(summary = "Health Check", description = "Kiểm tra service có hoạt động không")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Auth Service is running");
    }
}

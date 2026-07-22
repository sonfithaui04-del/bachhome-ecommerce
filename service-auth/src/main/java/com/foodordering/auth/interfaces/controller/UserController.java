package com.foodordering.auth.interfaces.controller;

import com.foodordering.auth.application.dto.UserDto;
import com.foodordering.auth.domain.model.User;
import com.foodordering.auth.domain.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.foodordering.auth.application.dto.UpdatePointsDto;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "User Management", description = "API quản lý người dùng")
public class UserController {

    private final UserRepository userRepository;

    @GetMapping
    @Operation(summary = "Lấy danh sách tất cả user", description = "Trả về danh sách user (dành cho Admin)")
    public ResponseEntity<List<UserDto>> getAllUsers() {
        log.info("[USER-CONTROLLER] Nhận request lấy danh sách user");
        List<User> users = userRepository.findAll();
        
        List<UserDto> userDtos = users.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
                
        return ResponseEntity.ok(userDtos);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Lấy thông tin User theo ID")
    public ResponseEntity<UserDto> getUserById(@PathVariable Long id) {
        return userRepository.findById(id)
                .map(this::mapToDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{id}/points/add")
    @Operation(summary = "Cộng điểm thưởng cho User")
    public ResponseEntity<UserDto> addPoints(@PathVariable Long id, @RequestBody UpdatePointsDto request) {
        return userRepository.findById(id).map(user -> {
            user.addPoints(request.getPoints());
            userRepository.save(user);
            return ResponseEntity.ok(mapToDto(user));
        }).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{id}/points/deduct")
    @Operation(summary = "Trừ điểm thưởng của User")
    public ResponseEntity<UserDto> deductPoints(@PathVariable Long id, @RequestBody UpdatePointsDto request) {
        return userRepository.findById(id).map(user -> {
            user.deductPoints(request.getPoints());
            userRepository.save(user);
            return ResponseEntity.ok(mapToDto(user));
        }).orElse(ResponseEntity.notFound().build());
    }

    private UserDto mapToDto(User user) {
        return UserDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .phoneNumber(user.getPhoneNumber())
                .role(user.getRole().name())
                .loyaltyPoints(user.getLoyaltyPoints())
                .build();
    }
}

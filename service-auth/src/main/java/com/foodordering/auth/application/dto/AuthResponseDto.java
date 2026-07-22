package com.foodordering.auth.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO cho Auth Response (trả về sau khi login/register thành công)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponseDto {

    private String token;
    private String tokenType;
    private Long userId;
    private String email;
    private String fullName;
    private String role;

    /**
     * Factory method để tạo response với Bearer token
     */
    public static AuthResponseDto fromTokenAndUser(String token, Long userId, String email, String fullName, String role) {
        return AuthResponseDto.builder()
                .token(token)
                .tokenType("Bearer")
                .userId(userId)
                .email(email)
                .fullName(fullName)
                .role(role)
                .build();
    }
}

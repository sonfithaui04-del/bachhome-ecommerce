package com.foodordering.payment.infrastructure.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Cấu hình SePay cho thanh toán QR
 * Thay thế các giá trị YOUR_* bằng thông tin thực từ tài khoản SePay
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "sepay")
public class SePayConfig {
    private String apiToken;
    private String bankAccountNumber;
    private String bankCode;
    private String accountName;
    private String apiUrl;
    private String frontendUrl;
}

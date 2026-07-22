package com.foodordering.payment.application.service;

import com.foodordering.payment.infrastructure.config.SePayConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * Service xử lý thanh toán SePay
 * Tạo QR VietQR để khách hàng quét và chuyển khoản
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SePayService {

    private final SePayConfig sePayConfig;
    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * Tạo URL QR code VietQR cho thanh toán
     * 
     * @param orderId Mã đơn hàng
     * @param amount Số tiền thanh toán
     * @return URL của hình ảnh QR code
     */
    public String generateQRCodeUrl(Long orderId, BigDecimal amount) {
        // Format: https://qr.sepay.vn/img?acc={account}&bank={bankCode}&amount={amount}&des={description}
        String description = "DH" + orderId;
        
        String qrUrl = String.format(
            "https://qr.sepay.vn/img?acc=%s&bank=%s&amount=%s&des=%s&template=compact",
            sePayConfig.getBankAccountNumber(),
            sePayConfig.getBankCode(),
            amount.longValue(),
            URLEncoder.encode(description, StandardCharsets.UTF_8)
        );
        
        log.info("[SEPAY] Generated QR URL for Order {}: {}", orderId, qrUrl);
        return qrUrl;
    }

    /**
     * Tạo thông tin chuyển khoản chi tiết
     */
    public SePayPaymentInfo createPaymentInfo(Long orderId, BigDecimal amount) {
        String description = "DH" + orderId;
        String qrUrl = generateQRCodeUrl(orderId, amount);
        
        return SePayPaymentInfo.builder()
                .orderId(orderId)
                .amount(amount)
                .bankCode(sePayConfig.getBankCode())
                .accountNumber(sePayConfig.getBankAccountNumber())
                .accountName(sePayConfig.getAccountName())
                .description(description)
                .qrCodeUrl(qrUrl)
                .build();
    }

    /**
     * Xác minh webhook callback từ SePay
     * SePay sẽ gọi webhook khi có giao dịch mới
     */
    public boolean verifyWebhook(String transactionId, BigDecimal amount, String content) {
        // Kiểm tra nội dung chuyển khoản có chứa mã đơn hàng không
        // Content format: "DH{orderId}"
        if (content == null || !content.startsWith("DH")) {
            return false;
        }
        
        try {
            String orderIdStr = content.substring(2).trim();
            Long orderId = Long.parseLong(orderIdStr);
            log.info("[SEPAY] Verified webhook for Order {}, amount: {}", orderId, amount);
            return true;
        } catch (NumberFormatException e) {
            log.warn("[SEPAY] Invalid order ID in webhook content: {}", content);
            return false;
        }
    }

    /**
     * Lấy thông tin giao dịch từ SePay API
     */
    public String getTransactionDetails(String transactionId) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + sePayConfig.getApiToken());
            headers.setContentType(MediaType.APPLICATION_JSON);

            String url = sePayConfig.getApiUrl() + "/transactions/" + transactionId;
            
            ResponseEntity<String> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                new HttpEntity<>(headers),
                String.class
            );
            
            return response.getBody();
        } catch (Exception e) {
            log.error("[SEPAY] Failed to get transaction details: {}", e.getMessage());
            return null;
        }
    }

    // DTO cho thông tin thanh toán
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class SePayPaymentInfo {
        private Long orderId;
        private BigDecimal amount;
        private String bankCode;
        private String accountNumber;
        private String accountName;
        private String description;
        private String qrCodeUrl;
    }
}

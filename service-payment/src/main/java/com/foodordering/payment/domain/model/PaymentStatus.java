package com.foodordering.payment.domain.model;

/**
 * Trạng thái thanh toán
 */
public enum PaymentStatus {
    PENDING,              // Chờ thanh toán
    PENDING_VERIFICATION, // User đã xác nhận, chờ hệ thống xác minh
    SUCCESS,              // Thanh toán thành công
    FAILED,               // Thanh toán thất bại
    CANCELLED,            // Đã hủy
    REFUNDED              // Đã hoàn tiền
}

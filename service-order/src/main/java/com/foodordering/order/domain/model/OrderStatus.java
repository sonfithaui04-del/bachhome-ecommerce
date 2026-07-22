package com.foodordering.order.domain.model;

/**
 * Order Status Enum
 */
public enum OrderStatus {
    PENDING,        // Đơn hàng mới tạo
    CONFIRMED,      // Đã xác nhận
    PREPARING,      // Đang chuẩn bị
    READY,          // Sẵn sàng giao
    DELIVERING,     // Đang giao
    COMPLETED,      // Hoàn thành
    CANCELLED       // Đã hủy
}

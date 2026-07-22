package com.foodordering.auth.domain.model;

/**
 * User Role Enum
 * Định nghĩa các vai trò trong hệ thống
 */
public enum UserRole {
    CUSTOMER,  // Khách hàng đặt món
    ADMIN,     // Quản trị viên
    STAFF,     // Nhân viên (dự phòng cho tương lai)
    SHIPPER    // Người giao hàng
}

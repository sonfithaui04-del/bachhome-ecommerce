package com.foodordering.payment.domain.model;

/**
 * Phương thức thanh toán
 */
public enum PaymentMethod {
    COD,    // Thanh toán khi nhận hàng (Cash on Delivery)
    SEPAY,  // Chuyển khoản qua SePay QR
    MOMO,   // Ví MoMo (future)
    VNPAY,  // VNPay (future)
    ZALOPAY // ZaloPay (future)
}

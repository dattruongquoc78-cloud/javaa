package com.example.exaple06.enums;

public enum PaymentStatus {
    PENDING,      // ⏳ Đang chờ xử lý
    COMPLETED,    // ✅ Đã thanh toán
    FAILED,       // ❌ Thanh toán thất bại
    CANCELLED,    // 🚫 Người dùng thoát / không thanh toán
    EXPIRED       // 🕒 Hết hạn (tùy chọn dùng nếu cần timeout)
}

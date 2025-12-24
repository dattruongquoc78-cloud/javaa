package com.example.exaple06.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BillRequest {
    private Long orderId;
    private Long promotionId; // Có thể null nếu không áp dụng
    private String paymentMethod; // ✅ Thêm phương thức thanh toán
}
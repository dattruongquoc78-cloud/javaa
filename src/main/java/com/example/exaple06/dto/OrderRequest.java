package com.example.exaple06.dto;

import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Getter
@Setter
public class OrderRequest {
    private Long tableId;
    private Long userId;
    private String notes;
    private List<OrderItemRequest> items;
    private Long promotionId;
    private String promotionCode;

    // 🔥 THÊM CLASS CON NÀY
    @Getter
    @Setter
    public static class OrderItemRequest {
        private Long productId;
        private Integer quantity;

        // ⭐ NEW FIELDS FOR DISCOUNT SUPPORT
        private Double originalPrice; 
        private Double finalPrice;
    }
}

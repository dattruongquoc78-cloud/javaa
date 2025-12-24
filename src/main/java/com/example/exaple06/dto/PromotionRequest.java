package com.example.exaple06.dto;

import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;
import com.example.exaple06.entity.Promotion;

@Getter
@Setter
public class PromotionRequest {
    private String name;
    private String description;
    private String code;
    private Promotion.DiscountType discountType;
    private Double discountAmount;
    private Double discountPercentage;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Boolean isActive;
    private Integer usageLimit;
}

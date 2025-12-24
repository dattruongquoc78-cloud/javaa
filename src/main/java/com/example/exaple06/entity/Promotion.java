package com.example.exaple06.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "promotions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder // ✅ THÊM BUILDER
public class Promotion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String description; // ✅ THÊM FIELD NÀY
    private String code; // ✅ THÊM FIELD NÀY - mã khuyến mãi
    
    @Enumerated(EnumType.STRING) // ✅ THÊM ENUM CHO LOẠI GIẢM GIÁ
    private DiscountType discountType; 
    
    private Double discountAmount;
    private Double discountPercentage;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Boolean isActive = true;
    private Integer usageLimit; // ✅ THÊM FIELD NÀY - số lần sử dụng tối đa
    private Integer usedCount = 0; // ✅ THÊM FIELD NÀY - số lần đã sử dụng

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
    
    // ✅ THÊM ENUM
    public enum DiscountType {
        PERCENTAGE, FIXED_AMOUNT
    }
}
package com.example.exaple06.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "bills")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Bill {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String billNumber;

    @Column(nullable = false)
    private Double totalAmount;

    @CreationTimestamp
    private LocalDateTime createdAt;

    // ✅ Quan hệ với Order
    @ManyToOne
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    // ✅ Thêm quan hệ với Promotion
    @ManyToOne
    @JoinColumn(name = "promotion_id")
    private Promotion promotion;

    // ✅ Phương thức thanh toán (Tiền mặt / MOMO)
    @Column(nullable = true)
    private String paymentMethod;

    // ✅ Trạng thái thanh toán
    @Column(nullable = false)
    private String paymentStatus = "UNPAID"; // UNPAID | PAID
}

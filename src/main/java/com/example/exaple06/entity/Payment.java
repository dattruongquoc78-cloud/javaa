package com.example.exaple06.entity;

import com.example.exaple06.enums.PaymentMethod;
import com.example.exaple06.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonBackReference;

@Entity
@Table(name = "payments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private PaymentMethod method;

    @Enumerated(EnumType.STRING)
    private PaymentStatus status;

    private Double totalAmount;

    private String notes;
    private String transactionId;

    @CreationTimestamp
    private LocalDateTime issuedAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    // 🔗 Quan hệ 1-1 ngược với Order
    // @OneToOne(mappedBy = "payment")
    // @JsonBackReference // ✅ CHỖ NÀY
    // private Order order;
@OneToOne
@JoinColumn(name = "order_id") // ✅ Cột khóa ngoại nằm ở bảng payments
@JsonBackReference
private Order order;


}

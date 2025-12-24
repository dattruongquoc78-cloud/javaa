package com.example.exaple06.dto;

import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter
@Setter
public class BillResponse {
    private Long id;
    private String billNumber;
    private Double totalAmount;
    private LocalDateTime createdAt;
    private OrderResponse order;
    private String paymentMethod;
    private String paymentStatus;
}
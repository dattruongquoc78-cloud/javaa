package com.example.exaple06.dto;

import com.example.exaple06.enums.OrderStatus;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class OrderResponse {
    private Long id;
    private OrderStatus status;
    private Double totalAmount;
    private String notes;
    private LocalDateTime createdAt;
    private TableDTO table;
    private UserDTO user;
    private List<OrderDetailDTO> orderDetails;
}
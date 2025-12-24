package com.example.exaple06.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrderDetailDTO {
    private Long id;
    private Integer quantity;
    private Double price;
    private ProductDTO product;
}
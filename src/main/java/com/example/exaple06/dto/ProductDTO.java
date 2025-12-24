package com.example.exaple06.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProductDTO {
    private Long id;
    private String title;
    private String description;
    private Double price;
    private String photo;
    private Integer stockQuantity;
    private CategoryDTO category;
}
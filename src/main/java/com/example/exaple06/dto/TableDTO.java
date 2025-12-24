package com.example.exaple06.dto;

import com.example.exaple06.enums.TableStatus;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TableDTO {
    private Long id;
    private String name;
    private String description;
    private Integer capacity;
    private TableStatus status;
}
package com.vieguys.productservice.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateProductRequestDTO {
    private String name;
    private String description;
    private Double price;
    private Integer stock;
}

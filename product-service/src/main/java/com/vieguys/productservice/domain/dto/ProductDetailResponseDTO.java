package com.vieguys.productservice.domain.dto;

import com.vieguys.productservice.domain.model.Review;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductDetailResponseDTO {
    private ProductResponseDTO product;
    private List<Review> reviews;
}

package com.vieguys.productservice.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
@Document(collection = "products")
public class Product {

    @Id
    private String id;

    private String name;
    private String description;
    private Double price;
    private Integer stock;

    private List<String> imageUrls;

    private Double averageRating;
    private Integer totalReviews;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

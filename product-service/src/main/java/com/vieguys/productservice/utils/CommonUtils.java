package com.vieguys.productservice.utils;

import com.vieguys.productservice.domain.dto.ProductResponseDTO;
import com.vieguys.productservice.domain.model.Product;
import org.springframework.http.MediaType;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URLConnection;
import java.util.List;

public final class CommonUtils {

    private CommonUtils() {
    }

    public static ProductResponseDTO toProductResponse(Product product) {
        return ProductResponseDTO.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .stock(product.getStock())
                .imageUrls(product.getImageUrls() == null
                        ? List.of()
                        : product.getImageUrls().stream().map(CommonUtils::buildImageUrl).toList())
                .averageRating(product.getAverageRating())
                .totalReviews(product.getTotalReviews())
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .build();
    }

    public static String buildImageUrl(String remotePath) {
        return UriComponentsBuilder.fromPath("/api/product/images")
                .queryParam("path", remotePath)
                .build()
                .encode()
                .toUriString();
    }

    public static String resolveContentType(String path) {
        String contentType = URLConnection.guessContentTypeFromName(path);
        return contentType != null ? contentType : MediaType.APPLICATION_OCTET_STREAM_VALUE;
    }
}

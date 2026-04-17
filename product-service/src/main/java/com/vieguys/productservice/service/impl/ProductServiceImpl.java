package com.vieguys.productservice.service.impl;

import com.vieguys.productservice.domain.model.Product;
import com.vieguys.productservice.repository.ProductRepository;
import com.vieguys.productservice.service.FtpStorageService;
import com.vieguys.productservice.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final FtpStorageService ftpStorageService;

    @Override
    public Product createProduct(
            String name,
            String description,
            Double price,
            Integer stock,
            List<MultipartFile> images
    ) {
        validateRequest(name, price, stock, images);

        String normalizedName = name.trim();
        if (productRepository.existsByNameIgnoreCase(normalizedName)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Product name already exists");
        }

        List<String> uploadedImagePaths = ftpStorageService.uploadFiles(images);
        LocalDateTime now = LocalDateTime.now();
        Product product = Product.builder()
                .name(normalizedName)
                .description(description)
                .price(price)
                .stock(stock)
                .imageUrls(uploadedImagePaths)
                .averageRating(0.0)
                .totalReviews(0)
                .createdAt(now)
                .updatedAt(now)
                .build();

        try {
            return productRepository.save(product);
        } catch (RuntimeException exception) {
            ftpStorageService.deleteFiles(uploadedImagePaths);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to create product", exception);
        }
    }

    private void validateRequest(String name, Double price, Integer stock, List<MultipartFile> images) {
        if (!StringUtils.hasText(name)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Product name must not be blank");
        }
        if (price == null || price < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Product price must be greater than or equal to 0");
        }
        if (stock == null || stock < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Product stock must be greater than or equal to 0");
        }
        if (images == null || images.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Product images must not be empty");
        }
    }
}

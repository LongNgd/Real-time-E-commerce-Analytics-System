package com.vieguys.productservice.service;

import com.vieguys.productservice.domain.dto.CreateReviewRequestDTO;
import com.vieguys.productservice.domain.dto.ProductDetailResponseDTO;
import com.vieguys.productservice.domain.dto.UpdateProductRequestDTO;
import com.vieguys.productservice.domain.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ProductService {
    Page<Product> getProducts(int page, int size, String sortBy, String direction);

    ProductDetailResponseDTO getProductDetail(String productId);

    void deleteProduct(String productId);

    Product updateProduct(String productId, UpdateProductRequestDTO request);

    Product addProductImages(String productId, List<MultipartFile> images);

    Product removeProductImages(String productId, List<String> imagePaths);

    Product createProduct(
            String name,
            String description,
            Double price,
            Integer stock,
            List<MultipartFile> images
    );

    void createReview(String productId, CreateReviewRequestDTO request, String userEmail, String userName);

    void deleteReview(String productId, String reviewId, String userEmail);
}

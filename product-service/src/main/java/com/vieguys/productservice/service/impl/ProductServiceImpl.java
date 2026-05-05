package com.vieguys.productservice.service.impl;

import com.vieguys.productservice.domain.dto.CreateReviewRequestDTO;
import com.vieguys.productservice.domain.dto.ProductDetailResponseDTO;
import com.vieguys.productservice.domain.dto.UpdateProductRequestDTO;
import com.vieguys.productservice.domain.model.Product;
import com.vieguys.productservice.domain.model.Review;
import com.vieguys.productservice.repository.ProductRepository;
import com.vieguys.productservice.repository.ReviewRepository;
import com.vieguys.productservice.service.FtpStorageService;
import com.vieguys.productservice.service.ProductService;
import com.vieguys.productservice.utils.CommonUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private static final String PRODUCT_DETAIL_CACHE = "productDetail";

    private final ProductRepository productRepository;
    private final ReviewRepository reviewRepository;
    private final FtpStorageService ftpStorageService;

    @Override
    public Page<Product> getProducts(int page, int size, String sortBy, String direction) {
        if (page < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Page must be greater than or equal to 0");
        }
        if (size <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Size must be greater than 0");
        }
        if (!StringUtils.hasText(sortBy)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Sort field must not be blank");
        }

        Sort.Direction sortDirection;
        try {
            sortDirection = Sort.Direction.fromString(direction);
        } catch (IllegalArgumentException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Direction must be asc or desc", exception);
        }

        return productRepository.findAll(PageRequest.of(page, size, Sort.by(sortDirection, sortBy)));
    }

    @Override
    @Cacheable(value = PRODUCT_DETAIL_CACHE, key = "#productId")
    public ProductDetailResponseDTO getProductDetail(String productId) {
        Product product = ensureProductExists(productId);
        List<Review> reviews = reviewRepository.findByProductId(productId).stream()
                .sorted((left, right) -> right.getCreatedAt().compareTo(left.getCreatedAt()))
                .toList();

        return ProductDetailResponseDTO.builder()
                .product(CommonUtils.toProductResponse(product))
                .reviews(reviews)
                .build();
    }

    @Override
    @CacheEvict(value = PRODUCT_DETAIL_CACHE, allEntries = true)
    public void deleteProduct(String productId) {
        if (!StringUtils.hasText(productId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Product id must not be blank");
        }

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found"));

        List<String> imagePaths = product.getImageUrls() == null ? List.of() : List.copyOf(product.getImageUrls());

        try {
            reviewRepository.deleteByProductId(productId);
            productRepository.delete(product);
        } catch (RuntimeException exception) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to delete product", exception);
        }

        ftpStorageService.deleteFiles(imagePaths);
    }

    @Override
    @CacheEvict(value = PRODUCT_DETAIL_CACHE, allEntries = true)
    public Product updateProduct(String productId, UpdateProductRequestDTO request) {
        if (!StringUtils.hasText(productId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Product id must not be blank");
        }
        if (request == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Update request must not be empty");
        }
        validateProductRequest(request.getName(), request.getPrice(), request.getStock());

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found"));

        String normalizedName = request.getName().trim();
        if (!product.getName().equalsIgnoreCase(normalizedName)
                && productRepository.existsByNameIgnoreCase(normalizedName)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Product name already exists");
        }

        product.setName(normalizedName);
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setStock(request.getStock());
        product.setUpdatedAt(LocalDateTime.now());

        return productRepository.save(product);
    }

    @Override
    @CacheEvict(value = PRODUCT_DETAIL_CACHE, allEntries = true)
    public Product addProductImages(String productId, List<MultipartFile> images) {
        if (!StringUtils.hasText(productId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Product id must not be blank");
        }
        if (images == null || images.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Product images must not be empty");
        }

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found"));

        List<String> uploadedImagePaths = ftpStorageService.uploadFiles(images);
        List<String> updatedImagePaths = new ArrayList<>();
        if (product.getImageUrls() != null) {
            updatedImagePaths.addAll(product.getImageUrls());
        }
        updatedImagePaths.addAll(uploadedImagePaths);

        product.setImageUrls(updatedImagePaths);
        product.setUpdatedAt(LocalDateTime.now());

        try {
            return productRepository.save(product);
        } catch (RuntimeException exception) {
            ftpStorageService.deleteFiles(uploadedImagePaths);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to add product images", exception);
        }
    }

    @Override
    @CacheEvict(value = PRODUCT_DETAIL_CACHE, allEntries = true)
    public Product removeProductImages(String productId, List<String> imagePaths) {
        if (!StringUtils.hasText(productId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Product id must not be blank");
        }
        if (imagePaths == null || imagePaths.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Image paths must not be empty");
        }

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found"));

        List<String> currentImagePaths = product.getImageUrls() == null
                ? List.of()
                : new ArrayList<>(product.getImageUrls());

        for (String imagePath : imagePaths) {
            if (!StringUtils.hasText(imagePath) || !currentImagePaths.contains(imagePath)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Image path does not belong to product");
            }
        }

        if (currentImagePaths.size() == imagePaths.size()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Product must keep at least one image");
        }

        currentImagePaths.removeAll(imagePaths);
        ftpStorageService.deleteFiles(imagePaths);

        product.setImageUrls(currentImagePaths);
        product.setUpdatedAt(LocalDateTime.now());
        return productRepository.save(product);
    }

    @Override
    @CacheEvict(value = PRODUCT_DETAIL_CACHE, allEntries = true)
    public Product createProduct(
            String name,
            String description,
            Double price,
            Integer stock,
            List<MultipartFile> images
    ) {
        validateCreateProductRequest(name, price, stock, images);

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

    @Override
    @CacheEvict(value = PRODUCT_DETAIL_CACHE, allEntries = true)
    public void createReview(String productId, CreateReviewRequestDTO request, String userEmail, String userName) {
        validateReviewRequest(productId, request, userEmail, userName);

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found"));

        if (reviewRepository.existsByProductIdAndUserEmail(productId, userEmail.trim())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "You have already reviewed this product");
        }

        Review review = Review.builder()
                .productId(productId)
                .userEmail(userEmail.trim())
                .userName(userName.trim())
                .content(request.getContent().trim())
                .rating(request.getRating())
                .createdAt(LocalDateTime.now())
                .build();

        Review savedReview = reviewRepository.save(review);
        try {
            updateProductRating(product);
        } catch (RuntimeException exception) {
            reviewRepository.deleteById(savedReview.getId());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to create review", exception);
        }
    }

    @Override
    @CacheEvict(value = PRODUCT_DETAIL_CACHE, allEntries = true)
    public void deleteReview(String productId, String reviewId, String userEmail) {
        if (!StringUtils.hasText(reviewId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Review id must not be blank");
        }
        if (!StringUtils.hasText(userEmail)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User email is missing");
        }

        Product product = ensureProductExists(productId);
        String normalizedUserEmail = userEmail.trim();
        String normalizedReviewId = reviewId.trim();

        if (!reviewRepository.existsByIdAndProductIdAndUserEmail(normalizedReviewId, productId, normalizedUserEmail)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Review not found");
        }

        reviewRepository.deleteById(normalizedReviewId);
        updateProductRating(product);
    }

    private void validateCreateProductRequest(String name, Double price, Integer stock, List<MultipartFile> images) {
        validateProductRequest(name, price, stock);
        if (!StringUtils.hasText(name)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Product name must not be blank");
        }
        if (images == null || images.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Product images must not be empty");
        }
    }

    private void validateProductRequest(String name, Double price, Integer stock) {
        if (!StringUtils.hasText(name)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Product name must not be blank");
        }
        if (price == null || price < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Product price must be greater than or equal to 0");
        }
        if (stock == null || stock < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Product stock must be greater than or equal to 0");
        }
    }

    private void validateReviewRequest(
            String productId,
            CreateReviewRequestDTO request,
            String userEmail,
            String userName
    ) {
        if (!StringUtils.hasText(productId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Product id must not be blank");
        }
        if (request == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Review request must not be empty");
        }
        if (!StringUtils.hasText(request.getContent())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Review content must not be blank");
        }
        if (request.getRating() == null || request.getRating() < 1 || request.getRating() > 5) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Review rating must be between 1 and 5");
        }
        if (!StringUtils.hasText(userEmail)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User email is missing");
        }
        if (!StringUtils.hasText(userName)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User name is missing");
        }
    }

    private void validatePagination(int page, int size, String sortBy, String direction) {
        if (page < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Page must be greater than or equal to 0");
        }
        if (size <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Size must be greater than 0");
        }
        if (!StringUtils.hasText(sortBy)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Sort field must not be blank");
        }
        try {
            Sort.Direction.fromString(direction);
        } catch (IllegalArgumentException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Direction must be asc or desc", exception);
        }
    }

    private Product ensureProductExists(String productId) {
        if (!StringUtils.hasText(productId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Product id must not be blank");
        }

        return productRepository.findById(productId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found"));
    }

    private void updateProductRating(Product product) {
        List<Review> reviews = reviewRepository.findByProductId(product.getId());
        product.setTotalReviews(reviews.size());
        product.setAverageRating(reviews.stream().mapToInt(Review::getRating).average().orElse(0.0));
        product.setUpdatedAt(LocalDateTime.now());
        productRepository.save(product);
    }
}

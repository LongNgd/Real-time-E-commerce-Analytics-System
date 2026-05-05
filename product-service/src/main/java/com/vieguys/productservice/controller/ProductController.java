package com.vieguys.productservice.controller;

import com.vieguys.productservice.domain.dto.CreateReviewRequestDTO;
import com.vieguys.productservice.domain.dto.ProductDetailResponseDTO;
import com.vieguys.productservice.domain.dto.ProductResponseDTO;
import com.vieguys.productservice.domain.dto.RemoveProductImagesRequestDTO;
import com.vieguys.productservice.domain.dto.UpdateProductRequestDTO;
import com.vieguys.productservice.service.FtpStorageService;
import com.vieguys.productservice.service.ProductService;
import com.vieguys.productservice.utils.CommonUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Tag(name = "Product", description = "Product catalog, details, images, and reviews APIs")
@RestController
@RequestMapping
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;
    private final FtpStorageService ftpStorageService;

    @Operation(
            summary = "List products",
            description = "Returns a paginated product list with sorting options."
    )
    @GetMapping
    public ResponseEntity<Page<ProductResponseDTO>> getProducts(
            @Parameter(description = "Zero-based page index", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Number of products per page", example = "10")
            @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Field used for sorting", example = "createdAt")
            @RequestParam(defaultValue = "createdAt") String sort,
            @Parameter(description = "Sort direction: asc or desc", example = "desc")
            @RequestParam(defaultValue = "desc") String direction
    ) {
        Page<ProductResponseDTO> products = productService.getProducts(page, size, sort, direction)
                .map(CommonUtils::toProductResponse);
        return ResponseEntity.ok(products);
    }

    @Operation(
            summary = "Get product detail",
            description = "Returns product information together with its reviews."
    )
    @GetMapping(path = "/{id}")
    public ResponseEntity<ProductDetailResponseDTO> getProductDetail(
            @Parameter(description = "Product identifier") @PathVariable String id
    ) {
        return ResponseEntity.ok(productService.getProductDetail(id));
    }

    @Operation(
            summary = "Delete product",
            description = "Deletes a product, its reviews, and associated image files."
    )
    @DeleteMapping(path = "/{id}")
    public ResponseEntity<Void> deleteProduct(
            @Parameter(description = "Product identifier") @PathVariable String id
    ) {
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Update product",
            description = "Updates the core product information and returns the refreshed product detail."
    )
    @PutMapping(path = "/{id}")
    public ResponseEntity<ProductDetailResponseDTO> updateProduct(
            @Parameter(description = "Product identifier") @PathVariable String id,
            @RequestBody UpdateProductRequestDTO request
    ) {
        productService.updateProduct(id, request);
        return ResponseEntity.ok(productService.getProductDetail(id));
    }

    @Operation(
            summary = "Add product images",
            description = "Uploads one or more product images and returns the refreshed product detail."
    )
    @PostMapping(path = "/{id}/images", consumes = "multipart/form-data")
    public ResponseEntity<ProductDetailResponseDTO> addProductImages(
            @Parameter(description = "Product identifier") @PathVariable String id,
            @Parameter(description = "Image files to upload")
            @RequestParam List<MultipartFile> images
    ) {
        productService.addProductImages(id, images);
        return ResponseEntity.ok(productService.getProductDetail(id));
    }

    @Operation(
            summary = "Remove product images",
            description = "Removes selected product images and returns the refreshed product detail."
    )
    @DeleteMapping(path = "/{id}/images")
    public ResponseEntity<ProductDetailResponseDTO> removeProductImages(
            @Parameter(description = "Product identifier") @PathVariable String id,
            @RequestBody RemoveProductImagesRequestDTO request
    ) {
        productService.removeProductImages(id, request.getImagePaths());
        return ResponseEntity.ok(productService.getProductDetail(id));
    }

    @Operation(
            summary = "Download product image",
            description = "Streams a product image by its stored remote path."
    )
    @GetMapping(path = "/images")
    public ResponseEntity<byte[]> getImage(
            @Parameter(description = "Stored remote image path") @RequestParam String path
    ) {
        byte[] imageBytes = ftpStorageService.downloadFile(path);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, CommonUtils.resolveContentType(path))
                .body(imageBytes);
    }

    @Operation(
            summary = "Create product",
            description = "Creates a new product with uploaded images and returns the created product detail."
    )
    @PostMapping(consumes = "multipart/form-data")
    public ResponseEntity<ProductDetailResponseDTO> createProduct(
            @Parameter(description = "Product name")
            @RequestParam String name,
            @Parameter(description = "Product description")
            @RequestParam(required = false) String description,
            @Parameter(description = "Product price", example = "199.99")
            @RequestParam Double price,
            @Parameter(description = "Available stock", example = "50")
            @RequestParam Integer stock,
            @Parameter(description = "Product image files")
            @RequestParam List<MultipartFile> images
    ) {
        String productId = productService.createProduct(name, description, price, stock, images).getId();
        return ResponseEntity.status(HttpStatus.CREATED).body(productService.getProductDetail(productId));
    }

    @Operation(
            summary = "Create product review",
            description = "Creates a review for a product and returns the refreshed product detail."
    )
    @PostMapping(path = "/{id}/review")
    public ResponseEntity<ProductDetailResponseDTO> createReview(
            @Parameter(description = "Product identifier") @PathVariable String id,
            @RequestBody CreateReviewRequestDTO request,
            @Parameter(description = "User email from gateway header")
            @RequestHeader("X-User-Email") String userEmail,
            @Parameter(description = "User name from gateway header")
            @RequestHeader("X-User-Name") String userName
    ) {
        productService.createReview(id, request, userEmail, userName);
        return ResponseEntity.status(HttpStatus.CREATED).body(productService.getProductDetail(id));
    }

    @Operation(
            summary = "Delete product review",
            description = "Deletes the current user's review for a product and returns the refreshed product detail."
    )
    @DeleteMapping(path = "/{id}/review/{reviewId}")
    public ResponseEntity<ProductDetailResponseDTO> deleteReview(
            @Parameter(description = "Product identifier") @PathVariable String id,
            @Parameter(description = "Review identifier") @PathVariable String reviewId,
            @Parameter(description = "User email from gateway header")
            @RequestHeader("X-User-Email") String userEmail
    ) {
        productService.deleteReview(id, reviewId, userEmail);
        return ResponseEntity.ok(productService.getProductDetail(id));
    }
}

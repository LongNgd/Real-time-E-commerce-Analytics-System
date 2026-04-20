package com.vieguys.productservice.controller;

import com.vieguys.productservice.domain.dto.CreateReviewRequestDTO;
import com.vieguys.productservice.domain.dto.ProductDetailResponseDTO;
import com.vieguys.productservice.domain.dto.ProductResponseDTO;
import com.vieguys.productservice.domain.dto.RemoveProductImagesRequestDTO;
import com.vieguys.productservice.domain.dto.UpdateProductRequestDTO;
import com.vieguys.productservice.domain.model.Product;
import com.vieguys.productservice.domain.model.Review;
import com.vieguys.productservice.service.FtpStorageService;
import com.vieguys.productservice.service.ProductService;
import com.vieguys.productservice.utils.CommonUtils;
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

@RestController
@RequestMapping
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;
    private final FtpStorageService ftpStorageService;

    @GetMapping
    public ResponseEntity<Page<ProductResponseDTO>> getProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sort,
            @RequestParam(defaultValue = "desc") String direction
    ) {
        Page<ProductResponseDTO> products = productService.getProducts(page, size, sort, direction)
                .map(CommonUtils::toProductResponse);
        return ResponseEntity.ok(products);
    }

    @GetMapping(path = "/{id}/detail")
    public ResponseEntity<ProductDetailResponseDTO> getProductDetail(
            @PathVariable String id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(productService.getProductDetail(id, page, size));
    }

    @GetMapping(path = "/{id}")
    public ResponseEntity<ProductResponseDTO> getProductById(@PathVariable String id) {
        Product product = productService.getProductById(id);
        return ResponseEntity.ok(CommonUtils.toProductResponse(product));
    }

    @DeleteMapping(path = "/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable String id) {
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping(path = "/{id}")
    public ResponseEntity<ProductResponseDTO> updateProduct(
            @PathVariable String id,
            @RequestBody UpdateProductRequestDTO request
    ) {
        Product product = productService.updateProduct(id, request);
        return ResponseEntity.ok(CommonUtils.toProductResponse(product));
    }

    @PostMapping(path = "/{id}/images", consumes = "multipart/form-data")
    public ResponseEntity<ProductResponseDTO> addProductImages(
            @PathVariable String id,
            @RequestParam List<MultipartFile> images
    ) {
        Product product = productService.addProductImages(id, images);
        return ResponseEntity.ok(CommonUtils.toProductResponse(product));
    }

    @DeleteMapping(path = "/{id}/images")
    public ResponseEntity<ProductResponseDTO> removeProductImages(
            @PathVariable String id,
            @RequestBody RemoveProductImagesRequestDTO request
    ) {
        Product product = productService.removeProductImages(id, request.getImagePaths());
        return ResponseEntity.ok(CommonUtils.toProductResponse(product));
    }

    @GetMapping(path = "/images")
    public ResponseEntity<byte[]> getImage(@RequestParam String path) {
        byte[] imageBytes = ftpStorageService.downloadFile(path);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, CommonUtils.resolveContentType(path))
                .body(imageBytes);
    }

    @PostMapping(path = "/create", consumes = "multipart/form-data")
    public ResponseEntity<ProductResponseDTO> createProduct(
            @RequestParam String name,
            @RequestParam(required = false) String description,
            @RequestParam Double price,
            @RequestParam Integer stock,
            @RequestParam List<MultipartFile> images
    ) {
        Product product = productService.createProduct(name, description, price, stock, images);
        return ResponseEntity.status(HttpStatus.CREATED).body(CommonUtils.toProductResponse(product));
    }

    @GetMapping(path = "/{productId}/review")
    public ResponseEntity<Page<Review>> getProductReviews(
            @PathVariable String productId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sort,
            @RequestParam(defaultValue = "desc") String direction
    ) {
        return ResponseEntity.ok(productService.getProductReviews(productId, page, size, sort, direction));
    }

    @PostMapping(path = "/{id}/review")
    public ResponseEntity<Review> createReview(
            @PathVariable String id,
            @RequestBody CreateReviewRequestDTO request,
            @RequestHeader("X-User-Email") String userEmail,
            @RequestHeader("X-User-Name") String userName
    ) {
        Review review = productService.createReview(id, request, userEmail, userName);
        return ResponseEntity.status(HttpStatus.CREATED).body(review);
    }
}

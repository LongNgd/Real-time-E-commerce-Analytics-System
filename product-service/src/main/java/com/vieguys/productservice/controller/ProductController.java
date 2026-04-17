package com.vieguys.productservice.controller;

import com.vieguys.productservice.domain.model.Product;
import com.vieguys.productservice.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping
@RequiredArgsConstructor
public class ProductController {

    public final ProductService productService;

    @PostMapping(path = "/create", consumes = "multipart/form-data")
    public ResponseEntity<Product> createProduct(
            @RequestParam String name,
            @RequestParam(required = false) String description,
            @RequestParam Double price,
            @RequestParam Integer stock,
            @RequestParam List<MultipartFile> images
    ) {
        Product product = productService.createProduct(name, description, price, stock, images);
        return ResponseEntity.status(HttpStatus.CREATED).body(product);
    }
}

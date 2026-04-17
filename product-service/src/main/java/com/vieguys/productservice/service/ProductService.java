package com.vieguys.productservice.service;

import com.vieguys.productservice.domain.model.Product;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ProductService {
    Product createProduct(
            String name,
            String description,
            Double price,
            Integer stock,
            List<MultipartFile> images
    );
}

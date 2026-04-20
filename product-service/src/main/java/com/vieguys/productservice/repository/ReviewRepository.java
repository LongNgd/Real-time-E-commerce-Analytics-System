package com.vieguys.productservice.repository;

import com.vieguys.productservice.domain.model.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewRepository extends MongoRepository<Review, String> {
    boolean existsByProductIdAndUserEmail(String productId, String userEmail);

    Page<Review> findByProductId(String productId, Pageable pageable);

    List<Review> findByProductId(String productId);

    void deleteByProductId(String productId);
}

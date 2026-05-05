package com.vieguys.productservice.repository;

import com.vieguys.productservice.domain.model.Review;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewRepository extends MongoRepository<Review, String> {
    boolean existsByProductIdAndUserEmail(String productId, String userEmail);

    boolean existsByIdAndProductIdAndUserEmail(String id, String productId, String userEmail);

    List<Review> findByProductId(String productId);

    void deleteByProductId(String productId);
}

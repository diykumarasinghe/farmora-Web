package com.farmora.repository;

import com.farmora.model.Order;
import com.farmora.model.Review;
import com.farmora.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findByTargetUserOrderByCreatedAtDesc(User targetUser);
    List<Review> findByReviewerOrderByCreatedAtDesc(User reviewer);
    Optional<Review> findByOrderAndReviewer(Order order, User reviewer);
    List<Review> findByProductOrderByCreatedAtDesc(com.farmora.model.Product product);
    Optional<Review> findByProductAndReviewer(com.farmora.model.Product product, User reviewer);
}

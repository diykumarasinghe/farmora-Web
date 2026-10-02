package com.farmora.service;

import com.farmora.dto.review.ReviewRequest;
import com.farmora.dto.review.ReviewResponse;
import com.farmora.exception.AccessDeniedCustomException;
import com.farmora.exception.BusinessException;
import com.farmora.exception.ResourceNotFoundException;
import com.farmora.model.*;
import com.farmora.repository.ProductRepository;
import com.farmora.repository.ReviewRepository;
import com.farmora.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final OrderService orderService;
    private final AuthService authService;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final NotificationService notificationService;

    @Transactional
    public ReviewResponse createReview(ReviewRequest request, String reviewerEmail) {
        User reviewer = authService.getUserByEmail(reviewerEmail);
        Order order = null;
        Product product = null;
        User targetUser = null;

        if (request.getOrderId() != null) {
            order = orderService.getOrderEntity(request.getOrderId());
            if (!order.getBuyer().getId().equals(reviewer.getId())) {
                throw new AccessDeniedCustomException("Only the buyer who purchased the order can submit a review.");
            }
            if (order.getStatus() != OrderStatus.DELIVERED) {
                throw new BusinessException("Reviews can only be submitted after the order has been delivered.");
            }
            if (reviewRepository.findByOrderAndReviewer(order, reviewer).isPresent()) {
                throw new BusinessException("You have already reviewed this order.");
            }
            targetUser = order.getFarmer();
            product = order.getProduct();
        }

        if (request.getProductId() != null) {
            product = productRepository.findById(request.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + request.getProductId()));
            if (targetUser == null) {
                targetUser = product.getFarmer();
            }
        }

        if (order == null && product == null) {
            throw new BusinessException("Either Order ID or Product ID must be specified for a review.");
        }

        Review review = Review.builder()
                .order(order)
                .product(product)
                .reviewer(reviewer)
                .targetUser(targetUser)
                .rating(request.getRating())
                .comment(request.getComment())
                .build();

        Review saved = reviewRepository.save(review);

        // Notify target farmer
        if (targetUser != null) {
            notificationService.createNotification(
                    targetUser,
                    "New Review Received!",
                    "Buyer " + reviewer.getFullName() + " left a " + request.getRating() + "-star review.",
                    "REVIEW_RECEIVED",
                    saved.getId()
            );
        }

        return mapToResponse(saved);
    }

    public List<ReviewResponse> getReviewsForUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        return reviewRepository.findByTargetUserOrderByCreatedAtDesc(user).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<ReviewResponse> getReviewsForProduct(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));
        return reviewRepository.findByProductOrderByCreatedAtDesc(product).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<ReviewResponse> getMySubmittedReviews(String reviewerEmail) {
        User reviewer = authService.getUserByEmail(reviewerEmail);
        return reviewRepository.findByReviewerOrderByCreatedAtDesc(reviewer).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public ReviewResponse updateReview(Long reviewId, ReviewRequest request, String reviewerEmail) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found with id: " + reviewId));
        User user = authService.getUserByEmail(reviewerEmail);

        if (!review.getReviewer().getId().equals(user.getId()) && user.getRole() != Role.ADMIN) {
            throw new AccessDeniedCustomException("You can only edit your own reviews.");
        }

        review.setRating(request.getRating());
        review.setComment(request.getComment());
        return mapToResponse(reviewRepository.save(review));
    }

    @Transactional
    public void deleteReview(Long reviewId, String reviewerEmail) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found with id: " + reviewId));
        User user = authService.getUserByEmail(reviewerEmail);

        if (!review.getReviewer().getId().equals(user.getId()) && user.getRole() != Role.ADMIN) {
            throw new AccessDeniedCustomException("You can only delete your own reviews.");
        }

        reviewRepository.delete(review);
    }

    public com.farmora.dto.review.FarmerRatingSummaryDto getRatingSummaryForFarmer(Long farmerId) {
        User farmer = userRepository.findById(farmerId)
                .orElseThrow(() -> new ResourceNotFoundException("Farmer not found with id: " + farmerId));

        List<Review> reviews = reviewRepository.findByTargetUserOrderByCreatedAtDesc(farmer);
        long totalReviews = reviews.size();
        double avgRating = 0.0;
        java.util.Map<Integer, Long> counts = new java.util.HashMap<>();
        for (int i = 1; i <= 5; i++) {
            counts.put(i, 0L);
        }

        if (totalReviews > 0) {
            double sum = 0.0;
            for (Review r : reviews) {
                int rating = r.getRating() != null ? r.getRating() : 5;
                sum += rating;
                counts.put(rating, counts.getOrDefault(rating, 0L) + 1);
            }
            avgRating = Math.round((sum / totalReviews) * 10.0) / 10.0;
        }

        return com.farmora.dto.review.FarmerRatingSummaryDto.builder()
                .farmerId(farmerId)
                .averageRating(avgRating)
                .totalReviews(totalReviews)
                .ratingCounts(counts)
                .build();
    }

    public ReviewResponse mapToResponse(Review review) {
        return ReviewResponse.builder()
                .id(review.getId())
                .orderId(review.getOrder() != null ? review.getOrder().getId() : null)
                .productId(review.getProduct() != null ? review.getProduct().getId() : null)
                .productName(review.getProduct() != null ? review.getProduct().getName() : (review.getOrder() != null && review.getOrder().getProduct() != null ? review.getOrder().getProduct().getName() : null))
                .reviewer(authService.mapToUserResponse(review.getReviewer()))
                .targetUser(review.getTargetUser() != null ? authService.mapToUserResponse(review.getTargetUser()) : null)
                .rating(review.getRating())
                .comment(review.getComment())
                .createdAt(review.getCreatedAt())
                .updatedAt(review.getUpdatedAt())
                .build();
    }
}

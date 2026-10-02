package com.farmora.controller;

import com.farmora.dto.review.ReviewRequest;
import com.farmora.dto.review.ReviewResponse;
import com.farmora.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping
    @PreAuthorize("hasRole('BUYER')")
    public ResponseEntity<ReviewResponse> createReview(
            @Valid @RequestBody ReviewRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        ReviewResponse response = reviewService.createReview(request, userDetails.getUsername());
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<ReviewResponse>> getReviewsForUser(@PathVariable Long userId) {
        return ResponseEntity.ok(reviewService.getReviewsForUser(userId));
    }

    @GetMapping("/my")
    public ResponseEntity<List<ReviewResponse>> getMySubmittedReviews(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(reviewService.getMySubmittedReviews(userDetails.getUsername()));
    }

    @GetMapping("/summary/{farmerId}")
    public ResponseEntity<com.farmora.dto.review.FarmerRatingSummaryDto> getRatingSummaryForFarmer(@PathVariable Long farmerId) {
        return ResponseEntity.ok(reviewService.getRatingSummaryForFarmer(farmerId));
    }
}

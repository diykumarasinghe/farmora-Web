package com.farmora.controller;

import com.farmora.dto.analytics.BuyerAnalyticsResponse;
import com.farmora.dto.analytics.FarmerAnalyticsResponse;
import com.farmora.dto.product.ProductResponse;
import com.farmora.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @GetMapping("/api/analytics/farmer")
    public ResponseEntity<FarmerAnalyticsResponse> getFarmerAnalytics(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(analyticsService.getFarmerAnalytics(userDetails.getUsername()));
    }

    @GetMapping("/api/analytics/buyer")
    public ResponseEntity<BuyerAnalyticsResponse> getBuyerAnalytics(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(analyticsService.getBuyerAnalytics(userDetails.getUsername()));
    }

    @GetMapping("/api/buyer/saved-products")
    public ResponseEntity<List<ProductResponse>> getSavedProducts(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(analyticsService.getSavedProducts(userDetails.getUsername()));
    }

    @PostMapping("/api/buyer/saved-products/{productId}")
    public ResponseEntity<Void> saveProduct(
            @PathVariable Long productId,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        analyticsService.saveProduct(productId, userDetails.getUsername());
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/api/buyer/saved-products/{productId}")
    public ResponseEntity<Void> removeSavedProduct(
            @PathVariable Long productId,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        analyticsService.removeSavedProduct(productId, userDetails.getUsername());
        return ResponseEntity.noContent().build();
    }
}

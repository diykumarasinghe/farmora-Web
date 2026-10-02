package com.farmora.controller;

import com.farmora.dto.bid.BidRequest;
import com.farmora.dto.bid.BidResponse;
import com.farmora.dto.order.OrderResponse;
import com.farmora.service.BidService;
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
@RequestMapping("/api/bids")
@RequiredArgsConstructor
public class BidController {

    private final BidService bidService;

    @PostMapping
    @PreAuthorize("hasRole('BUYER')")
    public ResponseEntity<BidResponse> createBid(
            @Valid @RequestBody BidRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        BidResponse response = bidService.createBid(request, userDetails.getUsername());
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/my")
    @PreAuthorize("hasRole('BUYER')")
    public ResponseEntity<List<BidResponse>> getMyBids(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(bidService.getMyBids(userDetails.getUsername()));
    }

    @GetMapping("/farmer/received")
    @PreAuthorize("hasRole('FARMER')")
    public ResponseEntity<List<BidResponse>> getFarmerReceivedBids(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(bidService.getFarmerReceivedBids(userDetails.getUsername()));
    }

    @GetMapping("/product/{productId}")
    public ResponseEntity<List<BidResponse>> getProductBids(
            @PathVariable Long productId,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        return ResponseEntity.ok(bidService.getProductBids(productId, userDetails.getUsername()));
    }

    @PutMapping("/{id}/accept")
    @PreAuthorize("hasRole('FARMER')")
    public ResponseEntity<OrderResponse> acceptBid(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        return ResponseEntity.ok(bidService.acceptBid(id, userDetails.getUsername()));
    }

    @PutMapping("/{id}/reject")
    @PreAuthorize("hasRole('FARMER')")
    public ResponseEntity<BidResponse> rejectBid(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        return ResponseEntity.ok(bidService.rejectBid(id, userDetails.getUsername()));
    }

    @PutMapping("/{id}/cancel")
    @PreAuthorize("hasRole('BUYER')")
    public ResponseEntity<BidResponse> cancelBid(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        return ResponseEntity.ok(bidService.cancelBid(id, userDetails.getUsername()));
    }
}

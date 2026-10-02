package com.farmora.controller;

import com.farmora.dto.bid.BidHistoryResponse;
import com.farmora.dto.bid.BidRequest;
import com.farmora.dto.bid.BidResponse;
import com.farmora.dto.bid.CounterOfferRequest;
import com.farmora.dto.order.OrderResponse;
import com.farmora.service.BidService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bids")
@RequiredArgsConstructor
public class BidController {

    private final BidService bidService;

    // POST /api/bids (place bid)
    @PostMapping
    public ResponseEntity<BidResponse> createBid(
            @Valid @RequestBody BidRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(bidService.createBid(request, userDetails.getUsername()));
    }

    // GET /api/bids/:bidId (view bid details)
    @GetMapping("/{id}")
    public ResponseEntity<BidResponse> getBidById(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        return ResponseEntity.ok(bidService.getBidById(id, userDetails.getUsername()));
    }

    // PUT /api/bids/:bidId (general status update: counter/accept/reject)
    @PutMapping("/{id}")
    public ResponseEntity<?> updateBid(
            @PathVariable Long id,
            @RequestParam(required = false, defaultValue = "accept") String action,
            @RequestBody(required = false) CounterOfferRequest counterRequest,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        if ("counter".equalsIgnoreCase(action) && counterRequest != null) {
            return ResponseEntity.ok(bidService.sendCounterOffer(id, counterRequest, userDetails.getUsername()));
        } else if ("reject".equalsIgnoreCase(action)) {
            return ResponseEntity.ok(bidService.rejectBid(id, userDetails.getUsername()));
        } else {
            return ResponseEntity.ok(bidService.acceptBid(id, userDetails.getUsername()));
        }
    }

    // DELETE /api/bids/:bidId (withdraw bid)
    @DeleteMapping("/{id}")
    public ResponseEntity<BidResponse> deleteOrWithdrawBid(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        return ResponseEntity.ok(bidService.withdrawBid(id, userDetails.getUsername()));
    }

    // GET /api/bids/farmer/:id (all bids received by farmer)
    @GetMapping("/farmer/{id}")
    public ResponseEntity<List<BidResponse>> getFarmerBidsById(@PathVariable Long id) {
        return ResponseEntity.ok(bidService.getFarmerBidsById(id));
    }

    // GET /api/bids/buyer/:id (all bids placed by buyer)
    @GetMapping("/buyer/{id}")
    public ResponseEntity<List<BidResponse>> getBuyerBidsById(@PathVariable Long id) {
        return ResponseEntity.ok(bidService.getBuyerBidsById(id));
    }

    // POST /api/bids/:bidId/counter (counter-offer)
    @PostMapping("/{id}/counter")
    public ResponseEntity<BidResponse> postCounterOffer(
            @PathVariable Long id,
            @Valid @RequestBody CounterOfferRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        return ResponseEntity.ok(bidService.sendCounterOffer(id, request, userDetails.getUsername()));
    }

    // GET /api/bids/:bidId/history (negotiation history)
    @GetMapping("/{id}/history")
    public ResponseEntity<List<BidHistoryResponse>> getBidHistory(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        return ResponseEntity.ok(bidService.getBidHistory(id, userDetails.getUsername()));
    }

    // PUT /api/bids/:bidId/extend (extend expiry)
    @PutMapping("/{id}/extend")
    public ResponseEntity<BidResponse> extendBidExpiry(
            @PathVariable Long id,
            @RequestParam(required = false, defaultValue = "24") int hours,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        return ResponseEntity.ok(bidService.extendBidExpiry(id, hours, userDetails.getUsername()));
    }

    // DELETE /api/bids/:bidId/archive (archive old bids)
    @DeleteMapping("/{id}/archive")
    public ResponseEntity<BidResponse> archiveBid(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        return ResponseEntity.ok(bidService.archiveBid(id, userDetails.getUsername()));
    }

    // Existing convenience endpoints
    @GetMapping("/my")
    public ResponseEntity<List<BidResponse>> getMyBids(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(bidService.getMyBids(userDetails.getUsername()));
    }

    @GetMapping("/farmer/received")
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

    @PutMapping("/{id}/counter")
    public ResponseEntity<BidResponse> sendCounterOffer(
            @PathVariable Long id,
            @Valid @RequestBody CounterOfferRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        return ResponseEntity.ok(bidService.sendCounterOffer(id, request, userDetails.getUsername()));
    }

    @PutMapping("/{id}/accept-counter")
    public ResponseEntity<OrderResponse> acceptCounterOffer(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        return ResponseEntity.ok(bidService.acceptCounterOffer(id, userDetails.getUsername()));
    }

    @PutMapping("/{id}/accept")
    public ResponseEntity<OrderResponse> acceptBid(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        return ResponseEntity.ok(bidService.acceptBid(id, userDetails.getUsername()));
    }

    @PutMapping("/{id}/reject")
    public ResponseEntity<BidResponse> rejectBid(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        return ResponseEntity.ok(bidService.rejectBid(id, userDetails.getUsername()));
    }

    @PutMapping("/{id}/withdraw")
    public ResponseEntity<BidResponse> withdrawBid(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        return ResponseEntity.ok(bidService.withdrawBid(id, userDetails.getUsername()));
    }

    @PutMapping("/{id}/cancel")
    public ResponseEntity<BidResponse> cancelBid(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        return ResponseEntity.ok(bidService.cancelBid(id, userDetails.getUsername()));
    }
}

package com.farmora.controller;

import com.farmora.dto.bulk.BulkOrderResponse;
import com.farmora.dto.bulk.CreateBulkOrderRequest;
import com.farmora.dto.bulk.ModifyBulkQuantityRequest;
import com.farmora.model.BulkOrderStatus;
import com.farmora.service.BulkOrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bulk-orders")
@RequiredArgsConstructor
public class BulkOrderController {

    private final BulkOrderService bulkOrderService;

    @PostMapping
    public ResponseEntity<BulkOrderResponse> createBulkOrder(
            @Valid @RequestBody CreateBulkOrderRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(bulkOrderService.createBulkOrder(request, userDetails.getUsername()));
    }

    @GetMapping("/my")
    public ResponseEntity<List<BulkOrderResponse>> getMyBulkOrders(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(bulkOrderService.getMyBulkOrders(userDetails.getUsername()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<BulkOrderResponse> getBulkOrderById(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        return ResponseEntity.ok(bulkOrderService.getBulkOrderById(id, userDetails.getUsername()));
    }

    @PutMapping("/{id}/quantity")
    public ResponseEntity<BulkOrderResponse> modifyBulkQuantity(
            @PathVariable Long id,
            @Valid @RequestBody ModifyBulkQuantityRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        return ResponseEntity.ok(bulkOrderService.modifyBulkQuantity(id, request, userDetails.getUsername()));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<BulkOrderResponse> updateStatus(
            @PathVariable Long id,
            @RequestParam BulkOrderStatus status,
            @RequestParam(required = false) String notes,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        return ResponseEntity.ok(bulkOrderService.updateStatus(id, status, notes, userDetails.getUsername()));
    }

    @PutMapping("/{id}/cancel")
    public ResponseEntity<BulkOrderResponse> cancelBulkOrder(
            @PathVariable Long id,
            @RequestParam(required = false) String reason,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        return ResponseEntity.ok(bulkOrderService.cancelBulkOrder(id, reason, userDetails.getUsername()));
    }
}

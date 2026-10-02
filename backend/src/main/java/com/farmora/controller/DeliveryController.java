package com.farmora.controller;

import com.farmora.dto.delivery.DeliveryResponse;
import com.farmora.model.DeliveryStatus;
import com.farmora.service.DeliveryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/deliveries")
@RequiredArgsConstructor
public class DeliveryController {

    private final DeliveryService deliveryService;

    @GetMapping("/available")
    @PreAuthorize("hasRole('DELIVERY_PARTNER') or hasRole('ADMIN')")
    public ResponseEntity<List<DeliveryResponse>> getAvailableDeliveries() {
        return ResponseEntity.ok(deliveryService.getAvailableDeliveries());
    }

    @GetMapping("/my")
    @PreAuthorize("hasRole('DELIVERY_PARTNER')")
    public ResponseEntity<List<DeliveryResponse>> getMyDeliveries(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(deliveryService.getMyDeliveries(userDetails.getUsername()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<DeliveryResponse> getDeliveryById(@PathVariable Long id) {
        return ResponseEntity.ok(deliveryService.getDeliveryById(id));
    }

    @PutMapping("/{id}/accept")
    @PreAuthorize("hasRole('DELIVERY_PARTNER')")
    public ResponseEntity<DeliveryResponse> acceptDelivery(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        return ResponseEntity.ok(deliveryService.acceptDelivery(id, userDetails.getUsername()));
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('DELIVERY_PARTNER') or hasRole('ADMIN')")
    public ResponseEntity<DeliveryResponse> updateDeliveryStatus(
            @PathVariable Long id,
            @RequestParam DeliveryStatus status,
            @RequestParam(required = false) String notes,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        return ResponseEntity.ok(deliveryService.updateDeliveryStatus(id, status, notes, userDetails.getUsername()));
    }
}

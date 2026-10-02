package com.farmora.controller;

import com.farmora.dto.delivery.CreateDeliveryRequest;
import com.farmora.dto.delivery.DeliveryResponse;
import com.farmora.dto.delivery.RedirectDeliveryRequest;
import com.farmora.dto.delivery.UpdateDeliveryLocationRequest;
import com.farmora.model.DeliveryStatus;
import com.farmora.service.DeliveryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/deliveries")
@RequiredArgsConstructor
public class DeliveryController {

    private final DeliveryService deliveryService;

    @PostMapping("/request")
    public ResponseEntity<DeliveryResponse> createDeliveryRequest(
            @Valid @RequestBody CreateDeliveryRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(deliveryService.createDeliveryRequest(request, userDetails.getUsername()));
    }

    @GetMapping("/available")
    public ResponseEntity<List<DeliveryResponse>> getAvailableDeliveries() {
        return ResponseEntity.ok(deliveryService.getAvailableDeliveries());
    }

    @GetMapping("/my")
    public ResponseEntity<List<DeliveryResponse>> getMyDeliveries(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(deliveryService.getMyDeliveries(userDetails.getUsername()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<DeliveryResponse> getDeliveryById(@PathVariable Long id) {
        return ResponseEntity.ok(deliveryService.getDeliveryById(id));
    }

    @GetMapping("/order/{orderId}")
    public ResponseEntity<DeliveryResponse> getDeliveryByOrderId(
            @PathVariable Long orderId,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        return ResponseEntity.ok(deliveryService.getDeliveryByOrderId(orderId, userDetails.getUsername()));
    }

    @PutMapping("/{id}/accept")
    public ResponseEntity<DeliveryResponse> acceptDelivery(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        return ResponseEntity.ok(deliveryService.acceptDelivery(id, userDetails.getUsername()));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<DeliveryResponse> updateDeliveryStatus(
            @PathVariable Long id,
            @RequestParam DeliveryStatus status,
            @RequestParam(required = false) String notes,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        return ResponseEntity.ok(deliveryService.updateDeliveryStatus(id, status, notes, userDetails.getUsername()));
    }

    @PutMapping("/{id}/redirect")
    public ResponseEntity<DeliveryResponse> redirectDelivery(
            @PathVariable Long id,
            @Valid @RequestBody RedirectDeliveryRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        return ResponseEntity.ok(deliveryService.redirectDelivery(id, request, userDetails.getUsername()));
    }

    @PutMapping("/{id}/cancel")
    public ResponseEntity<DeliveryResponse> cancelDelivery(
            @PathVariable Long id,
            @RequestParam(required = false) String reason,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        return ResponseEntity.ok(deliveryService.cancelDelivery(id, reason, userDetails.getUsername()));
    }

    @PutMapping("/{id}/location")
    public ResponseEntity<DeliveryResponse> updateLocation(
            @PathVariable Long id,
            @RequestBody UpdateDeliveryLocationRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        return ResponseEntity.ok(deliveryService.updateLocation(id, request, userDetails.getUsername()));
    }
}

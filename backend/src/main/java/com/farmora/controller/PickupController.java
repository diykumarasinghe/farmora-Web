package com.farmora.controller;

import com.farmora.dto.pickup.PickupResponse;
import com.farmora.dto.pickup.ReschedulePickupRequest;
import com.farmora.dto.pickup.SchedulePickupRequest;
import com.farmora.model.PickupStatus;
import com.farmora.service.PickupService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/pickups")
@RequiredArgsConstructor
public class PickupController {

    private final PickupService pickupService;

    @PostMapping("/schedule")
    public ResponseEntity<PickupResponse> schedulePickup(
            @Valid @RequestBody SchedulePickupRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(pickupService.schedulePickup(request, userDetails.getUsername()));
    }

    @GetMapping("/my")
    public ResponseEntity<List<PickupResponse>> getMyPickups(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(pickupService.getMyPickups(userDetails.getUsername()));
    }

    @GetMapping("/order/{orderId}")
    public ResponseEntity<PickupResponse> getPickupByOrderId(
            @PathVariable Long orderId,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        return ResponseEntity.ok(pickupService.getPickupByOrderId(orderId, userDetails.getUsername()));
    }

    @PutMapping("/{id}/reschedule")
    public ResponseEntity<PickupResponse> reschedulePickup(
            @PathVariable Long id,
            @Valid @RequestBody ReschedulePickupRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        return ResponseEntity.ok(pickupService.reschedulePickup(id, request, userDetails.getUsername()));
    }

    @PutMapping("/{id}/cancel")
    public ResponseEntity<PickupResponse> cancelPickup(
            @PathVariable Long id,
            @RequestParam(required = false) String reason,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        return ResponseEntity.ok(pickupService.cancelPickup(id, reason, userDetails.getUsername()));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<PickupResponse> updateStatus(
            @PathVariable Long id,
            @RequestParam PickupStatus status,
            @RequestParam(required = false) String passcode,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        return ResponseEntity.ok(pickupService.updatePickupStatus(id, status, passcode, userDetails.getUsername()));
    }
}

package com.farmora.controller;

import com.farmora.dto.harvest.DailyHarvestRequest;
import com.farmora.dto.harvest.DailyHarvestResponse;
import com.farmora.dto.harvest.HarvestAnalyticsResponse;
import com.farmora.model.HarvestStatus;
import com.farmora.service.DailyHarvestService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/harvest")
@RequiredArgsConstructor
public class DailyHarvestController {

    private final DailyHarvestService harvestService;

    // POST /api/harvest/daily (post harvest)
    @PostMapping("/daily")
    public ResponseEntity<DailyHarvestResponse> postDailyHarvest(
            @Valid @RequestBody DailyHarvestRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(harvestService.createDailyHarvest(request, userDetails.getUsername()));
    }

    // GET /api/harvest/:harvestId (view details)
    @GetMapping("/{harvestId}")
    public ResponseEntity<DailyHarvestResponse> getHarvestDetails(@PathVariable Long harvestId) {
        return ResponseEntity.ok(harvestService.getHarvestById(harvestId));
    }

    // PUT /api/harvest/:harvestId/quantity (update qty)
    @PutMapping("/{harvestId}/quantity")
    public ResponseEntity<DailyHarvestResponse> updateHarvestQuantity(
            @PathVariable Long harvestId,
            @RequestParam BigDecimal quantity,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        return ResponseEntity.ok(harvestService.updateQuantity(harvestId, quantity, userDetails.getUsername()));
    }

    // DELETE /api/harvest/:harvestId (archive harvest)
    @DeleteMapping("/{harvestId}")
    public ResponseEntity<DailyHarvestResponse> archiveHarvest(
            @PathVariable Long harvestId,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        return ResponseEntity.ok(harvestService.archiveHarvest(harvestId, userDetails.getUsername()));
    }

    // GET /api/harvest/farmer/:id/today (today's posts)
    @GetMapping("/farmer/{id}/today")
    public ResponseEntity<List<DailyHarvestResponse>> getFarmerTodayPosts(@PathVariable Long id) {
        return ResponseEntity.ok(harvestService.getFarmerTodayHarvest(id));
    }

    // GET /api/harvest/farmer/:id/all
    @GetMapping("/farmer/{id}/all")
    public ResponseEntity<List<DailyHarvestResponse>> getFarmerAllPosts(@PathVariable Long id) {
        return ResponseEntity.ok(harvestService.getFarmerAllHarvests(id));
    }

    // PUT /api/harvest/:harvestId/status (sold-out or available)
    @PutMapping("/{harvestId}/status")
    public ResponseEntity<DailyHarvestResponse> updateHarvestStatus(
            @PathVariable Long harvestId,
            @RequestParam HarvestStatus status,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        return ResponseEntity.ok(harvestService.updateStatus(harvestId, status, userDetails.getUsername()));
    }

    // GET /api/harvest/available/now (fresh products)
    @GetMapping("/available/now")
    public ResponseEntity<List<DailyHarvestResponse>> getAvailableFreshHarvests() {
        return ResponseEntity.ok(harvestService.getAvailableNow());
    }

    // GET /api/harvest/:farmerId/analytics (stats)
    @GetMapping("/{farmerId}/analytics")
    public ResponseEntity<HarvestAnalyticsResponse> getHarvestAnalytics(@PathVariable Long farmerId) {
        return ResponseEntity.ok(harvestService.getFarmerHarvestAnalytics(farmerId));
    }

    // PUT /api/harvest/:harvestId/extend (extend date)
    @PutMapping("/{harvestId}/extend")
    public ResponseEntity<DailyHarvestResponse> extendHarvestDate(
            @PathVariable Long harvestId,
            @RequestParam(required = false, defaultValue = "2") int days,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        return ResponseEntity.ok(harvestService.extendHarvestDate(harvestId, days, userDetails.getUsername()));
    }

    // DELETE /api/harvest/cleanup (delete/mark expired)
    @DeleteMapping("/cleanup")
    public ResponseEntity<Map<String, Object>> cleanupExpiredHarvests() {
        return ResponseEntity.ok(harvestService.cleanupExpired());
    }
}

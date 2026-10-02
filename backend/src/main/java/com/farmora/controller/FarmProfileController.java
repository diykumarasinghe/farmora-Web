package com.farmora.controller;

import com.farmora.dto.farm.FarmProfileRequest;
import com.farmora.dto.farm.FarmProfileResponse;
import com.farmora.model.FarmingMethod;
import com.farmora.model.VerificationStatus;
import com.farmora.service.FarmProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/farm-profiles")
@RequiredArgsConstructor
public class FarmProfileController {

    private final FarmProfileService farmProfileService;

    @GetMapping("/my")
    @PreAuthorize("hasRole('FARMER')")
    public ResponseEntity<FarmProfileResponse> getMyFarmProfile(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(farmProfileService.getMyFarmProfile(userDetails.getUsername()));
    }

    @PostMapping("/my")
    @PreAuthorize("hasRole('FARMER')")
    public ResponseEntity<FarmProfileResponse> createOrUpdateMyFarmProfile(
            @Valid @RequestBody FarmProfileRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        return ResponseEntity.ok(farmProfileService.createOrUpdateFarmProfile(request, userDetails.getUsername()));
    }

    @PostMapping("/my/request-verification")
    @PreAuthorize("hasRole('FARMER')")
    public ResponseEntity<FarmProfileResponse> requestVerification(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(farmProfileService.requestVerification(userDetails.getUsername()));
    }

    @PatchMapping("/my/archive")
    @PreAuthorize("hasRole('FARMER')")
    public ResponseEntity<FarmProfileResponse> toggleArchiveMyFarmProfile(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(farmProfileService.toggleArchiveProfile(userDetails.getUsername()));
    }

    @GetMapping("/farmer/{farmerId}")
    public ResponseEntity<FarmProfileResponse> getFarmProfileByFarmerId(@PathVariable Long farmerId) {
        return ResponseEntity.ok(farmProfileService.getFarmProfileByFarmerId(farmerId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<FarmProfileResponse> getFarmProfileById(@PathVariable Long id) {
        return ResponseEntity.ok(farmProfileService.getFarmProfileById(id));
    }

    @GetMapping
    public ResponseEntity<List<FarmProfileResponse>> getFarms(
            @RequestParam(required = false) String province,
            @RequestParam(required = false) String district,
            @RequestParam(required = false) FarmingMethod farmingMethod,
            @RequestParam(required = false) VerificationStatus verificationStatus
    ) {
        return ResponseEntity.ok(farmProfileService.getFarms(province, district, farmingMethod, verificationStatus));
    }

    @PutMapping("/{id}/verify")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<FarmProfileResponse> adminUpdateVerificationStatus(
            @PathVariable Long id,
            @RequestParam VerificationStatus status,
            @RequestParam(required = false) String notes
    ) {
        return ResponseEntity.ok(farmProfileService.adminUpdateVerificationStatus(id, status, notes));
    }
}

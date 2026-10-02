package com.farmora.controller;

import com.farmora.dto.buyer.BuyerAddressRequest;
import com.farmora.dto.buyer.BuyerAddressResponse;
import com.farmora.service.BuyerAddressService;
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
@RequestMapping("/api/buyer/addresses")
@RequiredArgsConstructor
@PreAuthorize("hasRole('BUYER')")
public class BuyerAddressController {

    private final BuyerAddressService buyerAddressService;

    @GetMapping
    public ResponseEntity<List<BuyerAddressResponse>> getMyAddresses(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(buyerAddressService.getMyAddresses(userDetails.getUsername()));
    }

    @PostMapping
    public ResponseEntity<BuyerAddressResponse> addAddress(
            @Valid @RequestBody BuyerAddressRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        BuyerAddressResponse response = buyerAddressService.addAddress(request, userDetails.getUsername());
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<BuyerAddressResponse> updateAddress(
            @PathVariable Long id,
            @Valid @RequestBody BuyerAddressRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        return ResponseEntity.ok(buyerAddressService.updateAddress(id, request, userDetails.getUsername()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAddress(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        buyerAddressService.deleteAddress(id, userDetails.getUsername());
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/default")
    public ResponseEntity<BuyerAddressResponse> setDefaultAddress(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        return ResponseEntity.ok(buyerAddressService.setDefaultAddress(id, userDetails.getUsername()));
    }
}

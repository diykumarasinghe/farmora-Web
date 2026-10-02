package com.farmora.controller;

import com.farmora.dto.farm.FarmProfileResponse;
import com.farmora.service.FavouriteFarmService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/buyer/favourites")
@RequiredArgsConstructor
@PreAuthorize("hasRole('BUYER')")
public class FavouriteFarmController {

    private final FavouriteFarmService favouriteFarmService;

    @GetMapping
    public ResponseEntity<List<FarmProfileResponse>> getFavouriteFarms(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(favouriteFarmService.getFavouriteFarms(userDetails.getUsername()));
    }

    @PostMapping("/{farmerId}")
    public ResponseEntity<Map<String, String>> addFavourite(
            @PathVariable Long farmerId,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        favouriteFarmService.addFavourite(farmerId, userDetails.getUsername());
        return ResponseEntity.ok(Collections.singletonMap("message", "Farm added to favourites"));
    }

    @DeleteMapping("/{farmerId}")
    public ResponseEntity<Map<String, String>> removeFavourite(
            @PathVariable Long farmerId,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        favouriteFarmService.removeFavourite(farmerId, userDetails.getUsername());
        return ResponseEntity.ok(Collections.singletonMap("message", "Farm removed from favourites"));
    }

    @GetMapping("/check/{farmerId}")
    public ResponseEntity<Map<String, Boolean>> checkIsFavourite(
            @PathVariable Long farmerId,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        boolean isFav = favouriteFarmService.isFavourite(farmerId, userDetails.getUsername());
        return ResponseEntity.ok(Collections.singletonMap("isFavourite", isFav));
    }
}

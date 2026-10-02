package com.farmora.controller;

import com.farmora.dto.agri.AgriculturalPostRequest;
import com.farmora.dto.agri.AgriculturalPostResponse;
import com.farmora.model.AgriCategory;
import com.farmora.service.AgriculturalInfoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/agri-info")
@RequiredArgsConstructor
public class AgriculturalInfoController {

    private final AgriculturalInfoService agriService;

    @GetMapping
    public ResponseEntity<List<AgriculturalPostResponse>> getPosts(
            @RequestParam(required = false) AgriCategory category,
            @RequestParam(required = false) String province
    ) {
        return ResponseEntity.ok(agriService.getAllPosts(category, province));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AgriculturalPostResponse> getPostById(@PathVariable Long id) {
        return ResponseEntity.ok(agriService.getPostById(id));
    }

    @PostMapping
    public ResponseEntity<AgriculturalPostResponse> createPost(
            @Valid @RequestBody AgriculturalPostRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(agriService.createPost(request, userDetails.getUsername()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePost(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        agriService.deletePost(id, userDetails.getUsername());
        return ResponseEntity.noContent().build();
    }
}

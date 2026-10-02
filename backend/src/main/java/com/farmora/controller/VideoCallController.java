package com.farmora.controller;

import com.farmora.dto.video.VideoCallRequest;
import com.farmora.dto.video.VideoCallResponse;
import com.farmora.service.VideoCallService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/video-calls")
@RequiredArgsConstructor
public class VideoCallController {

    private final VideoCallService videoCallService;

    @PostMapping("/initiate")
    public ResponseEntity<VideoCallResponse> initiateCall(
            @Valid @RequestBody VideoCallRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(videoCallService.initiateCall(request, userDetails.getUsername()));
    }

    @PostMapping("/schedule")
    public ResponseEntity<VideoCallResponse> scheduleMeeting(
            @Valid @RequestBody VideoCallRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(videoCallService.scheduleMeeting(request, userDetails.getUsername()));
    }

    @GetMapping("/my")
    public ResponseEntity<List<VideoCallResponse>> getMyCalls(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(videoCallService.getMyCalls(userDetails.getUsername()));
    }

    @PutMapping("/{id}/end")
    public ResponseEntity<VideoCallResponse> endCall(
            @PathVariable Long id,
            @RequestParam(required = false, defaultValue = "120") Integer durationSeconds,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        return ResponseEntity.ok(videoCallService.endCall(id, durationSeconds, userDetails.getUsername()));
    }

    @PutMapping("/{id}/cancel")
    public ResponseEntity<VideoCallResponse> cancelCall(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        return ResponseEntity.ok(videoCallService.cancelCall(id, userDetails.getUsername()));
    }
}

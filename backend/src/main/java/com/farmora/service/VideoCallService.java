package com.farmora.service;

import com.farmora.dto.video.VideoCallRequest;
import com.farmora.dto.video.VideoCallResponse;
import com.farmora.exception.AccessDeniedCustomException;
import com.farmora.exception.ResourceNotFoundException;
import com.farmora.model.User;
import com.farmora.model.VideoCall;
import com.farmora.model.VideoCallStatus;
import com.farmora.repository.UserRepository;
import com.farmora.repository.VideoCallRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VideoCallService {

    private final VideoCallRepository videoCallRepository;
    private final UserRepository userRepository;
    private final AuthService authService;
    private final NotificationService notificationService;

    @Transactional
    public VideoCallResponse initiateCall(VideoCallRequest request, String callerEmail) {
        User caller = authService.getUserByEmail(callerEmail);
        User receiver = userRepository.findById(request.getReceiverId())
                .orElseThrow(() -> new ResourceNotFoundException("Receiver not found with id: " + request.getReceiverId()));

        String roomCode = "farmora-live-" + UUID.randomUUID().toString().substring(0, 8);
        String title = request.getTitle() != null ? request.getTitle() : "Live Consultation with " + caller.getFullName();

        VideoCall call = VideoCall.builder()
                .caller(caller)
                .receiver(receiver)
                .roomCode(roomCode)
                .title(title)
                .status(VideoCallStatus.ONGOING)
                .meetingLink("/video-room/" + roomCode)
                .notes(request.getNotes())
                .build();

        VideoCall saved = videoCallRepository.save(call);

        notificationService.createNotification(
                receiver,
                "Incoming Live Video Call",
                caller.getFullName() + " is inviting you to a live video call. Room code: " + roomCode,
                "VIDEO_CALL_INCOMING",
                saved.getId()
        );

        return mapToResponse(saved);
    }

    @Transactional
    public VideoCallResponse scheduleMeeting(VideoCallRequest request, String callerEmail) {
        User caller = authService.getUserByEmail(callerEmail);
        User receiver = userRepository.findById(request.getReceiverId())
                .orElseThrow(() -> new ResourceNotFoundException("Receiver not found with id: " + request.getReceiverId()));

        String roomCode = "farmora-meet-" + UUID.randomUUID().toString().substring(0, 8);
        String title = request.getTitle() != null ? request.getTitle() : "Scheduled Farm Consultation";

        VideoCall call = VideoCall.builder()
                .caller(caller)
                .receiver(receiver)
                .roomCode(roomCode)
                .title(title)
                .scheduledAt(request.getScheduledAt() != null ? request.getScheduledAt() : LocalDateTime.now().plusDays(1))
                .status(VideoCallStatus.SCHEDULED)
                .meetingLink("/video-room/" + roomCode)
                .notes(request.getNotes())
                .build();

        VideoCall saved = videoCallRepository.save(call);

        notificationService.createNotification(
                receiver,
                "Video Meeting Scheduled",
                caller.getFullName() + " scheduled a live video meeting with you for " + saved.getScheduledAt(),
                "VIDEO_CALL_SCHEDULED",
                saved.getId()
        );

        return mapToResponse(saved);
    }

    public List<VideoCallResponse> getMyCalls(String userEmail) {
        User user = authService.getUserByEmail(userEmail);
        return videoCallRepository.findMyCalls(user).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public VideoCallResponse endCall(Long callId, Integer durationSeconds, String userEmail) {
        VideoCall call = videoCallRepository.findById(callId)
                .orElseThrow(() -> new ResourceNotFoundException("Video call not found with id: " + callId));
        User user = authService.getUserByEmail(userEmail);

        if (!call.getCaller().getId().equals(user.getId()) && !call.getReceiver().getId().equals(user.getId()) && user.getRole() != com.farmora.model.Role.ADMIN) {
            throw new AccessDeniedCustomException("You are not authorized to end this video call.");
        }

        call.setStatus(VideoCallStatus.COMPLETED);
        call.setEndedAt(LocalDateTime.now());
        call.setDurationSeconds(durationSeconds != null ? durationSeconds : 120);

        return mapToResponse(videoCallRepository.save(call));
    }

    @Transactional
    public VideoCallResponse cancelCall(Long callId, String userEmail) {
        VideoCall call = videoCallRepository.findById(callId)
                .orElseThrow(() -> new ResourceNotFoundException("Video call not found with id: " + callId));
        User user = authService.getUserByEmail(userEmail);

        if (!call.getCaller().getId().equals(user.getId()) && !call.getReceiver().getId().equals(user.getId()) && user.getRole() != com.farmora.model.Role.ADMIN) {
            throw new AccessDeniedCustomException("You are not authorized to cancel this call.");
        }

        call.setStatus(VideoCallStatus.CANCELLED);
        return mapToResponse(videoCallRepository.save(call));
    }

    public VideoCallResponse mapToResponse(VideoCall v) {
        return VideoCallResponse.builder()
                .id(v.getId())
                .caller(authService.mapToUserResponse(v.getCaller()))
                .receiver(authService.mapToUserResponse(v.getReceiver()))
                .roomCode(v.getRoomCode())
                .title(v.getTitle())
                .scheduledAt(v.getScheduledAt())
                .status(v.getStatus())
                .durationSeconds(v.getDurationSeconds())
                .meetingLink(v.getMeetingLink())
                .notes(v.getNotes())
                .createdAt(v.getCreatedAt())
                .endedAt(v.getEndedAt())
                .build();
    }
}

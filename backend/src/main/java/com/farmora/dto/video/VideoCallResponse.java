package com.farmora.dto.video;

import com.farmora.dto.auth.UserResponse;
import com.farmora.model.VideoCallStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VideoCallResponse {
    private Long id;
    private UserResponse caller;
    private UserResponse receiver;
    private String roomCode;
    private String title;
    private LocalDateTime scheduledAt;
    private VideoCallStatus status;
    private Integer durationSeconds;
    private String meetingLink;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime endedAt;
}

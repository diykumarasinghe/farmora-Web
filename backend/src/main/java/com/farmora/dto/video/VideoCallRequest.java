package com.farmora.dto.video;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VideoCallRequest {

    @NotNull(message = "Receiver ID is required")
    private Long receiverId;

    private String title;
    private LocalDateTime scheduledAt;
    private String notes;
}

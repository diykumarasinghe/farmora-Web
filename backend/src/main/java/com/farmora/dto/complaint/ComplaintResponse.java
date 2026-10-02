package com.farmora.dto.complaint;

import com.farmora.dto.auth.UserResponse;
import com.farmora.model.ComplaintStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ComplaintResponse {
    private Long id;
    private UserResponse complainant;
    private UserResponse targetUser;
    private Long orderId;
    private String subject;
    private String description;
    private ComplaintStatus status;
    private String adminNotes;
    private LocalDateTime createdAt;
    private LocalDateTime resolvedAt;
}

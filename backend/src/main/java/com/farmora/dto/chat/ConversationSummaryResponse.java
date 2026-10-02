package com.farmora.dto.chat;

import com.farmora.dto.auth.UserResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConversationSummaryResponse {
    private UserResponse contact;
    private String lastMessage;
    private LocalDateTime lastMessageTime;
    private long unreadCount;
}

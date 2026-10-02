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
public class ChatMessageResponse {
    private Long id;
    private UserResponse sender;
    private UserResponse recipient;
    private String content;
    private Boolean readStatus;
    private Boolean isMine;
    private LocalDateTime sentAt;
}

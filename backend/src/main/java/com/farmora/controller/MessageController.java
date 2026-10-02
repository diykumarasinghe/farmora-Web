package com.farmora.controller;

import com.farmora.dto.chat.ChatMessageRequest;
import com.farmora.dto.chat.ChatMessageResponse;
import com.farmora.dto.chat.ConversationSummaryResponse;
import com.farmora.service.ChatMessageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
public class MessageController {

    private final ChatMessageService chatMessageService;

    @PostMapping
    public ResponseEntity<ChatMessageResponse> sendMessage(
            @Valid @RequestBody ChatMessageRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(chatMessageService.sendMessage(request, userDetails.getUsername()));
    }

    @GetMapping("/conversations")
    public ResponseEntity<List<ConversationSummaryResponse>> getConversations(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        return ResponseEntity.ok(chatMessageService.getConversations(userDetails.getUsername()));
    }

    @GetMapping("/conversation/{otherUserId}")
    public ResponseEntity<List<ChatMessageResponse>> getConversation(
            @PathVariable Long otherUserId,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        return ResponseEntity.ok(chatMessageService.getConversation(otherUserId, userDetails.getUsername()));
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<Void> markAsRead(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        chatMessageService.markAsRead(id, userDetails.getUsername());
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/conversation/{otherUserId}/read")
    public ResponseEntity<Void> markConversationAsRead(
            @PathVariable Long otherUserId,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        chatMessageService.markConversationAsRead(otherUserId, userDetails.getUsername());
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMessage(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        chatMessageService.deleteMessage(id, userDetails.getUsername());
        return ResponseEntity.noContent().build();
    }
}

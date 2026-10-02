package com.farmora.service;

import com.farmora.dto.chat.ChatMessageRequest;
import com.farmora.dto.chat.ChatMessageResponse;
import com.farmora.dto.chat.ConversationSummaryResponse;
import com.farmora.exception.AccessDeniedCustomException;
import com.farmora.exception.ResourceNotFoundException;
import com.farmora.model.ChatMessage;
import com.farmora.model.User;
import com.farmora.repository.ChatMessageRepository;
import com.farmora.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatMessageService {

    private final ChatMessageRepository chatMessageRepository;
    private final UserRepository userRepository;
    private final AuthService authService;
    private final NotificationService notificationService;

    @Transactional
    public ChatMessageResponse sendMessage(ChatMessageRequest request, String senderEmail) {
        User sender = authService.getUserByEmail(senderEmail);
        User recipient = userRepository.findById(request.getRecipientId())
                .orElseThrow(() -> new ResourceNotFoundException("Recipient not found with id: " + request.getRecipientId()));

        ChatMessage message = ChatMessage.builder()
                .sender(sender)
                .recipient(recipient)
                .content(request.getContent().trim())
                .readStatus(false)
                .deletedBySender(false)
                .deletedByRecipient(false)
                .build();

        ChatMessage saved = chatMessageRepository.save(message);

        // Notify recipient
        notificationService.createNotification(
                recipient,
                "New Message from " + sender.getFullName(),
                request.getContent().length() > 60 ? request.getContent().substring(0, 57) + "..." : request.getContent(),
                "CHAT_MESSAGE",
                saved.getId()
        );

        return mapToResponse(saved, sender);
    }

    public List<ChatMessageResponse> getConversation(Long otherUserId, String currentUserEmail) {
        User currentUser = authService.getUserByEmail(currentUserEmail);
        User otherUser = userRepository.findById(otherUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + otherUserId));

        List<ChatMessage> list = chatMessageRepository.findConversationHistory(currentUser, otherUser);
        return list.stream()
                .map(m -> mapToResponse(m, currentUser))
                .collect(Collectors.toList());
    }

    public List<ConversationSummaryResponse> getConversations(String currentUserEmail) {
        User currentUser = authService.getUserByEmail(currentUserEmail);
        List<ChatMessage> allMessages = chatMessageRepository.findAllActiveForUser(currentUser);

        // Group by the other user ID
        Map<Long, List<ChatMessage>> grouped = new LinkedHashMap<>();
        for (ChatMessage m : allMessages) {
            Long otherId = m.getSender().getId().equals(currentUser.getId())
                    ? m.getRecipient().getId()
                    : m.getSender().getId();
            grouped.computeIfAbsent(otherId, k -> new ArrayList<>()).add(m);
        }

        List<ConversationSummaryResponse> summaries = new ArrayList<>();
        for (Map.Entry<Long, List<ChatMessage>> entry : grouped.entrySet()) {
            List<ChatMessage> messages = entry.getValue();
            if (messages.isEmpty()) continue;
            ChatMessage latest = messages.get(0); // already ordered DESC by sentAt
            User contact = latest.getSender().getId().equals(currentUser.getId())
                    ? latest.getRecipient()
                    : latest.getSender();

            long unreadCount = messages.stream()
                    .filter(m -> m.getRecipient().getId().equals(currentUser.getId()) && Boolean.FALSE.equals(m.getReadStatus()))
                    .count();

            summaries.add(ConversationSummaryResponse.builder()
                    .contact(authService.mapToUserResponse(contact))
                    .lastMessage(latest.getContent())
                    .lastMessageTime(latest.getSentAt())
                    .unreadCount(unreadCount)
                    .build());
        }

        return summaries;
    }

    @Transactional
    public void markAsRead(Long messageId, String userEmail) {
        User user = authService.getUserByEmail(userEmail);
        ChatMessage message = chatMessageRepository.findById(messageId)
                .orElseThrow(() -> new ResourceNotFoundException("Message not found with id: " + messageId));

        if (!message.getRecipient().getId().equals(user.getId())) {
            throw new AccessDeniedCustomException("You can only mark your received messages as read.");
        }

        message.setReadStatus(true);
        chatMessageRepository.save(message);
    }

    @Transactional
    public void markConversationAsRead(Long otherUserId, String userEmail) {
        User currentUser = authService.getUserByEmail(userEmail);
        User otherUser = userRepository.findById(otherUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + otherUserId));

        List<ChatMessage> list = chatMessageRepository.findConversationHistory(currentUser, otherUser);
        for (ChatMessage m : list) {
            if (m.getRecipient().getId().equals(currentUser.getId()) && Boolean.FALSE.equals(m.getReadStatus())) {
                m.setReadStatus(true);
            }
        }
        chatMessageRepository.saveAll(list);
    }

    @Transactional
    public void deleteMessage(Long messageId, String userEmail) {
        User user = authService.getUserByEmail(userEmail);
        ChatMessage message = chatMessageRepository.findById(messageId)
                .orElseThrow(() -> new ResourceNotFoundException("Message not found with id: " + messageId));

        boolean isSender = message.getSender().getId().equals(user.getId());
        boolean isRecipient = message.getRecipient().getId().equals(user.getId());

        if (!isSender && !isRecipient) {
            throw new AccessDeniedCustomException("You can only delete messages from your conversations.");
        }

        if (isSender) {
            message.setDeletedBySender(true);
        }
        if (isRecipient) {
            message.setDeletedByRecipient(true);
        }

        if (Boolean.TRUE.equals(message.getDeletedBySender()) && Boolean.TRUE.equals(message.getDeletedByRecipient())) {
            chatMessageRepository.delete(message);
        } else {
            chatMessageRepository.save(message);
        }
    }

    public ChatMessageResponse mapToResponse(ChatMessage m, User currentUser) {
        boolean isMine = currentUser != null && m.getSender().getId().equals(currentUser.getId());
        return ChatMessageResponse.builder()
                .id(m.getId())
                .sender(authService.mapToUserResponse(m.getSender()))
                .recipient(authService.mapToUserResponse(m.getRecipient()))
                .content(m.getContent())
                .readStatus(m.getReadStatus())
                .isMine(isMine)
                .sentAt(m.getSentAt())
                .build();
    }
}

package com.farmora.repository;

import com.farmora.model.ChatMessage;
import com.farmora.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    @Query("SELECT m FROM ChatMessage m WHERE " +
           "((m.sender = :user1 AND m.recipient = :user2 AND m.deletedBySender = false) OR " +
           " (m.sender = :user2 AND m.recipient = :user1 AND m.deletedByRecipient = false)) " +
           "ORDER BY m.sentAt ASC")
    List<ChatMessage> findConversationHistory(@Param("user1") User user1, @Param("user2") User user2);

    @Query("SELECT m FROM ChatMessage m WHERE " +
           "(m.sender = :user AND m.deletedBySender = false) OR " +
           "(m.recipient = :user AND m.deletedByRecipient = false) " +
           "ORDER BY m.sentAt DESC")
    List<ChatMessage> findAllActiveForUser(@Param("user") User user);

    long countByRecipientAndReadStatusFalseAndDeletedByRecipientFalse(User recipient);
}

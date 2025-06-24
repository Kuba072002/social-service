package org.example.domain.message;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MessageRepository extends JpaRepository<Message, MessageId> {
    @Query("""
            SELECT *
            FROM messages
            WHERE chat_id = ?0 AND message_id >= ?1 AND message_id <= ?2 LIMIT ?3
            ORDER BY m.messageId DESC
            """)
    List<Message> findAllByChatIdAndMessageIdBetween(Long chatId, UUID from, UUID to, int limit);

    Optional<Message> findByChatIdAndMessageId(Long chatId, UUID messageId);

    void deleteByChatId(Long chatId);
}

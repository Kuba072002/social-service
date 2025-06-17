package org.example.domain.message;

import org.springframework.data.cassandra.repository.Query;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MessageRepository extends ListCrudRepository<Message, UUID> {
    @Query("SELECT * FROM messages WHERE chat_id = ?0 AND message_id >= ?1 AND message_id <= ?2 LIMIT ?3")
    List<Message> findAllByChatIdAndMessageIdBetween(Long chatId, UUID from, UUID to, int limit);

    Optional<Message> findByChatIdAndMessageId(Long chatId, UUID messageId);

    void deleteByChatId(Long chatId);
}

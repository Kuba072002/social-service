package org.example.domain.message;

import org.springframework.data.cassandra.repository.Query;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Repository
public interface MessageRepository extends ListCrudRepository<Message, UUID> {
    @Query("SELECT * FROM messages WHERE chat_id = ?0 AND created_at >= ?1 AND created_at <= ?2 LIMIT ?3")
    List<Message> findAllByChatIdAndCreatedAtBetween(Long chatId, Instant from, Instant to, int limit);
}

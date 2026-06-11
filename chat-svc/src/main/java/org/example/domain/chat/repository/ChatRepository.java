package org.example.domain.chat.repository;

import org.example.domain.chat.entity.Chat;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;

@Repository
public interface ChatRepository extends JpaRepository<Chat, Long> {

    boolean existsByPrivatePairKey(@Param("privatePairKey") String privatePairKey);

    @EntityGraph(attributePaths = "participants")
    Optional<Chat> findWithParticipantsById(Long chatId);

    @Modifying
    @Query(value = """
            UPDATE chat_schema.chats
            SET last_message_at = :lastMessageAt
            WHERE id = (
                SELECT id FROM chat_schema.chats
                WHERE id = :id AND (last_message_at < :lastMessageAt OR last_message_at IS NULL)
                FOR UPDATE
            )
            """, nativeQuery = true)
    int updateLastMessageAt(@Param("id") Long id, @Param("lastMessageAt") Instant lastMessageAt);
}

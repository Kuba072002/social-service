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

    @Query(value = """
                SELECT EXISTS (
                    SELECT 1
                    FROM chat_schema.chat_participants cp1
                    JOIN chat_schema.chat_participants cp2 ON cp2.chat_id = cp1.chat_id AND cp2.user_id = :user2Id
                    JOIN chat_schema.chats c ON c.id = cp1.chat_id AND c.is_private = true
                    WHERE cp1.user_id = :user1Id
                )
            """, nativeQuery = true)
    boolean existsPrivateChat(@Param("user1Id") Long user1Id, @Param("user2Id") Long user2Id);

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

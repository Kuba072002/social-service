package org.example.domain.chat.repository;

import org.example.domain.chat.entity.ChatDetail;
import org.example.domain.chat.entity.ChatParticipant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface ChatParticipantRepository extends JpaRepository<ChatParticipant, Long> {

    List<ChatParticipant> findByChatId(Long chatId);

    @Query(value = """
            SELECT cp.chat_id,c.name,c.image_url,c.is_private,c.last_message_at,cp.last_read_at,null AS other_user
            FROM chat_schema.chat_participants cp
            JOIN chat_schema.chats c ON cp.chat_id = c.id
            WHERE cp.user_id = ?1 AND c.is_private = 'false'
            ORDER BY c.last_message_at
            OFFSET ?2
            LIMIT ?3
            """, nativeQuery = true)
    List<ChatDetail> findUserGroupChats(Long userId, int offset, int limit);

    @Query(value = """
            SELECT cp.chat_id,c.is_private,c.last_message_at,cp.last_read_at,
            (SELECT cp2.user_id FROM chat_schema.chat_participants cp2 WHERE cp2.chat_id = c.id AND cp2.user_id != ?1 limit 1) AS other_user
            FROM chat_schema.chat_participants cp
            JOIN chat_schema.chats c ON cp.chat_id = c.id
            WHERE cp.user_id = ?1 AND c.is_private = 'true'
            ORDER BY c.last_message_at
            OFFSET ?2
            LIMIT ?3
            """, nativeQuery = true)
    List<ChatDetail> findUserPrivateChats(Long userId, int offset, int limit);

    Optional<ChatParticipant> findByChatIdAndUserId(Long chatId, Long userId);

    @Modifying
    @Query("UPDATE ChatParticipant cp SET cp.lastReadAt = :lastReadAt WHERE cp.chat.id = :chatId AND cp.userId = :userId AND cp.lastReadAt < :lastReadAt")
    void updateLastReadAt(@Param("chatId") Long chatId, @Param("userId") Long userId, @Param("lastReadAt") Instant lastReadAt);
}

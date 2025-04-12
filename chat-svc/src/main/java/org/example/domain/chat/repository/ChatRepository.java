package org.example.domain.chat.repository;

import org.example.domain.chat.entity.Chat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ChatRepository extends JpaRepository<Chat, Long> {

    @Query(value = """
                SELECT EXISTS (
                    SELECT 1
                    FROM chat_schema.chats c
                    JOIN chat_schema.chat_participants cp1 ON cp1.chat_id = c.id AND cp1.user_id = :user1Id
                    JOIN chat_schema.chat_participants cp2 ON cp2.chat_id = c.id AND cp2.user_id = :user2Id
                    WHERE c.is_private = true
                )
            """, nativeQuery = true)
    boolean existsPrivateChat(@Param("user1Id") Long user1Id, @Param("user2Id") Long user2Id);

    @Query("SELECT c FROM Chat c LEFT JOIN FETCH c.participants WHERE c.id = :chatId")
    Optional<Chat> findChatWithParticipantsById(@Param("chatId") Long chatId);
}

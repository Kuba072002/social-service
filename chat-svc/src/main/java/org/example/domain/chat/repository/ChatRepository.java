package org.example.domain.chat.repository;

import org.example.domain.chat.entity.Chat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ChatRepository extends JpaRepository<Chat, Long> {
    @Query("""
                SELECT c,cp
                FROM Chat c
                JOIN ChatParticipant cp ON c.id = cp1.chat.id
                WHERE c.id = :chatId
            """)
    Optional<Chat> findChat(@Param("chatId") Long chatId);

    @Query("""
                SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END
                FROM Chat c
                WHERE c.type = 'private'
                AND EXISTS (
                    SELECT 1 FROM ChatParticipant cp1
                    WHERE cp1.chat.id = c.id AND cp1.user.id = :user1Id
                )
                AND EXISTS (
                    SELECT 1 FROM ChatParticipant cp2
                    WHERE cp2.chat.id = c.id AND cp2.user.id = :user2Id
                )
            """)
    boolean existsPrivateChat(@Param("user1Id") Long user1Id, @Param("user2Id") Long user2Id);
}

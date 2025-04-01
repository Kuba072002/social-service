package org.example.domain.chat.repository;

import org.example.domain.chat.entity.Chat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ChatRepository extends JpaRepository<Chat, Long> {
    @Query("""
                SELECT CASE WHEN COUNT(c) >= 2 THEN true ELSE false END
                FROM Chat c
                JOIN ChatParticipant cp ON cp.chat.id = c.id
                WHERE c.isPrivate = true
                AND cp.userId IN (:user1Id, :user2Id)
            """)
    boolean existsPrivateChat(@Param("user1Id") Long user1Id, @Param("user2Id") Long user2Id);
}

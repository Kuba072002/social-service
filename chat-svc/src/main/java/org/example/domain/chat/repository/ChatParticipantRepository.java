package org.example.domain.chat.repository;

import org.example.domain.chat.entity.ChatParticipant;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
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

    List<ChatParticipant> findByChat_Id(Long chatId);

    @EntityGraph(attributePaths = "chat")
    List<ChatParticipant> findByUserId(Long userId, Pageable pageable);

    Optional<ChatParticipant> findByChatIdAndUserId(Long chatId, Long userId);

    @Modifying
    @Query("UPDATE ChatParticipant cp SET cp.lastReadAt = :lastReadAt WHERE cp.chat.id = :chatId AND cp.userId = :userId AND cp.lastReadAt < :lastReadAt")
    void updateLastReadAt(@Param("chatId") Long chatId, @Param("userId") Long userId, @Param("lastReadAt") Instant lastReadAt);
}

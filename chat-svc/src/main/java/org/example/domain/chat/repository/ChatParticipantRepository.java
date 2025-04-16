package org.example.domain.chat.repository;

import org.example.domain.chat.entity.ChatParticipant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatParticipantRepository extends JpaRepository<ChatParticipant, Long> {

    @Query("SELECT cp FROM ChatParticipant cp WHERE cp.chat.id =:chatId")
    List<ChatParticipant> findAllByChatId(Long chatId);

    @Query("SELECT cp FROM ChatParticipant cp LEFT JOIN FETCH cp.chat WHERE cp.userId = :userId")
    List<ChatParticipant> findChatParticipantsWithChatsByUserId(@Param("userId") Long userId);
}

package org.example.domain.chat.repository;

import org.example.domain.chat.entity.Chat;
import org.example.domain.chat.entity.ChatParticipant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ChatParticipantRepository extends JpaRepository<ChatParticipant, Long> {

    List<ChatParticipant> findAllByChatId(Long chatId);

    @Query("SELECT cp.chat FROM ChatParticipant cp WHERE cp.userId = :userId")
    List<Chat> findAllChatsByUserId(Long userId);
}

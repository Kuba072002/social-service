package org.example.domain.chat.repository;

import org.example.domain.chat.entity.ChatParticipant;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatParticipantRepository extends JpaRepository<ChatParticipant, Long> {

    List<ChatParticipant> findByChat_Id(Long chatId);

    @EntityGraph(attributePaths = "chat")
    List<ChatParticipant> findByUserId(Long userId, Pageable pageable);
}

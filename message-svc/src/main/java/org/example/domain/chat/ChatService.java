package org.example.domain.chat;

import org.example.application.dto.ChatParticipantsDTO;

public interface ChatService {
    ChatParticipantsDTO findChatParticipants(Long chatId);
}

package org.example.domain.chat;

import org.example.application.dto.ChatDTO;

public interface ChatService {
    ChatDTO getChat(Long chatId);
}

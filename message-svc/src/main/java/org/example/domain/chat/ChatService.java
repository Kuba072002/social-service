package org.example.domain.chat;

import org.example.application.dto.ChatParticipantsDTO;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;

@HttpExchange(accept = "application/json", contentType = "application/json")
public interface ChatService {
    @GetExchange("/internal/chats")
    ChatParticipantsDTO findChatParticipants(@RequestParam Long chatId);
}

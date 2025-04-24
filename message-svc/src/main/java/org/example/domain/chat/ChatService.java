package org.example.domain.chat;

import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;

import java.util.Set;

@HttpExchange(accept = "application/json", contentType = "application/json")
public interface ChatService {
    @GetExchange("/internal/chats/participants/ids")
    Set<Long> findChatParticipantIds(@RequestParam Long chatId);
}

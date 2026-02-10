package org.example.domain.chat;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;

import java.util.Set;

@HttpExchange(accept = "application/json", contentType = "application/json", url = "${chat.service.url}")
public interface ChatService {
    @GetExchange("/internal/chats/{chatId}/participants/ids")
    Set<Long> findChatParticipantIds(@PathVariable Long chatId);
}

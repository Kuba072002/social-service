package org.example.domain.message;

import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;

import java.util.List;
import java.util.Set;

@HttpExchange(accept = "application/json", contentType = "application/json", url = "${message.service.url}")
public interface MessageService {

    @GetExchange("/internal/messages")
    List<LatestChatMessagesDTO> getLatestChatMessages(@RequestParam Set<Long> chatIds);
}

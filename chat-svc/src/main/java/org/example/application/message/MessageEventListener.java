package org.example.application.message;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.domain.chat.ChatFacade;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class MessageEventListener {
    private final ChatFacade chatFacade;

    @RabbitListener(queues = "${message.events.queue.rabbit}")
    public void process(MessageEvent messageEvent) {
        log.info("Processing message event for chat: {}", messageEvent.chatId());
        chatFacade.updateLastMessageAt(messageEvent.chatId(),messageEvent.messageCreatedAt());
    }
}

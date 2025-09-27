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
    private static final String POST_EVENT_TYPE = "POST";
    private static final String GET_EVENT_TYPE = "GET";

    @RabbitListener(queues = "${message.events.queue.rabbit}")
    public void process(MessageEvent messageEvent) {
        log.info("Processing message event for chat: {} , with type: {}", messageEvent.chatId(), messageEvent.type());
        switch (messageEvent.type()) {
            case POST_EVENT_TYPE ->
                    chatFacade.updateLastMessageAt(messageEvent.chatId(), messageEvent.lastMessageCreatedAt());
            case GET_EVENT_TYPE ->
                    chatFacade.updateLastReadAt(messageEvent.chatId(), messageEvent.userId(), messageEvent.lastReadAt());
            default -> log.info("Unhandled message event for chat, type: {}", messageEvent.type());
        }
    }
}

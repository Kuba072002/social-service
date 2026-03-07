package org.example.application.chat;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.domain.chat.ChatFacade;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChatEventListener {
    private static final String CREATE = "CREATE";
    private static final String MODIFY = "MODIFY";
    private static final String DELETE = "DELETE";

    private final ChatFacade chatFacade;

    @RabbitListener(queues = "${chat.events.queue.rabbit}")
    public void process(ChatEvent event) {
        log.info("Processing chat event with type {}. for chatId: {}", event.type(), event.chatId());
        long timestamp = event.updatedAt() * 1000;
        switch (event.type()) {
            case CREATE, MODIFY -> chatFacade.createOrModify(event, timestamp);
            case DELETE -> chatFacade.delete(event.chatId(), timestamp);
            default -> log.warn("Received unknown chat event type: {}", event.type());
        }
    }
}

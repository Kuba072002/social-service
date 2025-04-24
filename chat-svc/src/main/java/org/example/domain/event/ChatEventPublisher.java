package org.example.domain.event;

import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ChatEventPublisher {
    private final RabbitTemplate rabbitTemplate;

    public void sendEvent(ChatEvent chatEvent) {
        rabbitTemplate.convertAndSend("chat_events_queue", chatEvent);
    }
}

package org.example.domain.event;

import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ChatEventPublisher {
    private final RabbitTemplate rabbitTemplate;
    @Value("${chat.events.queue.rabbit}")
    private String queueName;

    public void sendEvent(ChatEvent chatEvent) {
        rabbitTemplate.convertAndSend(queueName, chatEvent);
    }
}

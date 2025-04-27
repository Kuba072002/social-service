package org.example.domain.message;

import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class MessagePublisher {
    private final SimpMessagingTemplate messagingTemplate;
    private final RabbitTemplate rabbitTemplate;
    @Value("${message.events.queue.rabbit}")
    private String queueName;

    @Async("asyncExecutor")
    public void publish(Set<Long> chatParticipantIds, Message message) {
        chatParticipantIds.forEach(userId -> {
            if (message.getSenderId().equals(userId)) return;
            messagingTemplate.convertAndSend("/topic/" + userId, message);
        });
        var event = new MessageEvent(message.getChatId(), message.getCreatedAt());
        rabbitTemplate.convertAndSend(queueName, event);
    }
}

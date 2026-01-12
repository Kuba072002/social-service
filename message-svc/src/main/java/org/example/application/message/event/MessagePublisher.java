package org.example.application.message.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.common.Utils;
import org.example.domain.message.Message;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class MessagePublisher {
    private final SimpMessagingTemplate messagingTemplate;
    private final RabbitTemplate rabbitTemplate;
    @Value("${message.events.queue.rabbit}")
    private String queueName;

    @Async("asyncExecutor")
    public void broadcastMessageAndPublishEvent(Set<Long> chatParticipantIds, Message message) {
        var messageJson = Utils.writeToJson(message);
        chatParticipantIds.forEach(userId -> {
            if (message.getSenderId().equals(userId)) return;
            messagingTemplate.convertAndSendToUser(String.valueOf(userId), "/queue/messages", messageJson);
        });
        var event = MessageEvent.post(message.getChatId(), message.getCreatedAt());
        publish(event);
    }

    public void publish(MessageEvent event) {
        log.info("Publishing message event: {}", event);
        rabbitTemplate.convertAndSend(queueName, event);
    }
}

package org.example.application.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.domain.event.ChatEventOutbox;
import org.example.domain.event.ChatEventService;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChatEventScheduler {
    private final ChatEventService chatEventService;
    private final RabbitTemplate rabbitTemplate;

    @Value("${chat.events.queue.rabbit}")
    private String queueName;
    @Value("${outbox.publisher.batch-size:50}")
    private int batchSize;

    @Scheduled(fixedDelayString = "${outbox.publisher.delay-ms:3000}")
    public void publishEvents() {
        List<ChatEventOutbox> events = chatEventService.findEventsBatch(batchSize);

        var sentEvents = events.stream()
                .map(this::send)
                .filter(Objects::nonNull)
                .toList();

        chatEventService.deleteEvents(sentEvents);
    }

    private ChatEventOutbox send(ChatEventOutbox outbox) {
        try {
            log.info("Sending chat event: {}, id: {}", outbox.getPayload(), outbox.getId());
            rabbitTemplate.convertAndSend(queueName, outbox.getPayload());
            return outbox;
        } catch (AmqpException e) {
            log.error("Failed to publish event {}", outbox.getId(), e);
            return null;
        }
    }
}

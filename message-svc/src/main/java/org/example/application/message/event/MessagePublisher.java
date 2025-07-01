package org.example.application.message.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class MessagePublisher {
    private final RabbitTemplate rabbitTemplate;
    @Value("${message.events.queue.rabbit}")
    private String queueName;

    @Async("asyncExecutor")
    public void publish(MessageEvent event) {
        log.info("Publishing message event: {}", event);
        rabbitTemplate.convertAndSend(queueName, event);
    }
}

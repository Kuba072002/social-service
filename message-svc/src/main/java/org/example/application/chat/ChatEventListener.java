package org.example.application.chat;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.domain.chat.ChatFacade;
import org.example.domain.message.MessageFacade;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChatEventListener {
    private final ChatFacade chatFacade;
    private final MessageFacade messageFacade;

    @Transactional
    @RabbitListener(queues = "${chat.events.queue.rabbit}")
    public void process(ChatEvent chatEvent) {
        log.info("Processing chat event with type {}. for chat: {}", chatEvent.type(), chatEvent.chatId());
        switch (chatEvent.type()) {
            case "CREATE", "MODIFY" -> {
                chatFacade.save(chatEvent.chatId(), chatEvent.userIds());
            }
            case "DELETE" -> {
                chatFacade.delete(chatEvent.chatId());
                messageFacade.delete(chatEvent.chatId());
            }
            default -> {
                log.info("Invalid chat event: {}", chatEvent);
            }
        }
    }
}

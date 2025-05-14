package org.example.application.chat;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.domain.chat.ChatFacade;
import org.example.domain.message.MessageFacade;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChatEventListener {
    private final ChatFacade chatFacade;
    private final MessageFacade messageFacade;

    @RabbitListener(queues = "${chat.events.queue.rabbit}")
    public void process(ChatEvent chatEvent) {
        log.info("Processing chat event with type {}. for chat: {}", chatEvent.type(), chatEvent.chatId());
        chatFacade.evictChatParticipants(chatEvent.chatId());
        if (chatEvent.type().equals("DELETE")) {
            messageFacade.delete(chatEvent.chatId());
        }
    }
}

package org.example.application;

import lombok.RequiredArgsConstructor;
import org.example.domain.message.Message;
import org.example.domain.message.MessageFacade;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class MessageController {
    private final MessageFacade messageFacade;
    private final SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/message")
    public void createMessage(Message message) {
        var userIdsToNotify = messageFacade.createMessage(message);

        userIdsToNotify.forEach(userId ->
                messagingTemplate.convertAndSendToUser(
                        userId.toString(), "/queue/messages", message
                )
        );
    }
}

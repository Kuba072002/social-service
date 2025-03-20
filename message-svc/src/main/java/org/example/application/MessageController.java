package org.example.application;

import org.example.domain.message.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
public class MessageController {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/message")
    public void createMessage(Message message) {
        messagingTemplate.convertAndSendToUser(
                "user", "/queue/messages", message
        );
    }
}

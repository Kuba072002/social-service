package org.example.application.activity;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.ApplicationException;
import org.example.application.dto.ChatActivityRequest;
import org.example.application.event.MessageEvent;
import org.example.application.event.OutboundMessagingService;
import org.example.domain.chat.ChatFacade;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Controller;

import java.security.Principal;

import static org.example.common.MessageApplicationError.NOT_INVOLVED_REQUESTER;

@Controller
@RequiredArgsConstructor
@Slf4j
public class ActivityController {
    private final ChatFacade chatFacade;
    private final OutboundMessagingService outboundMessagingService;

    @MessageMapping("/chat/lastRead")
    public boolean handle(@Payload ChatActivityRequest req, Principal principal) {
        if (principal == null) {
            throw new IllegalArgumentException("Principal is required");
        }
        Long userId = Long.parseLong(principal.getName());
        if (chatFacade.findChatParticipants(req.chatId()).contains(userId)) {
            throw new ApplicationException(NOT_INVOLVED_REQUESTER);
        }
        var event = MessageEvent.get(req.chatId(), userId, req.lastReadAt());
        outboundMessagingService.publishEvent(event);
        return true;
    }

    @MessageExceptionHandler
    @SendToUser("/queue/errors")
    public String handleException(Throwable ex) {
        log.error("WebSocket error: {}", ex.getMessage());
        return ex.getMessage();
    }
}

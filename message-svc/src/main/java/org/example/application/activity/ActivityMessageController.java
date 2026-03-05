package org.example.application.activity;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.application.activity.service.ActiveUserService;
import org.example.application.dto.ChatActivityRequest;
import org.example.common.Utils;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Slf4j
@Controller
@RequiredArgsConstructor
public class ActivityMessageController {
    private final ActiveUserService activeUserService;

    @MessageMapping("/chat/lastRead")
    public void updateLastRead(@Payload String req, Principal principal) {
        var chatActivityRequest = Utils.readJson(req, ChatActivityRequest.class);
        if (principal == null) {
            throw new IllegalArgumentException("Principal is required");
        }
        Long userId = Long.parseLong(principal.getName());
        activeUserService.updateLastRead(userId, chatActivityRequest.chatId(), chatActivityRequest.lastReadAt());
    }

    @MessageExceptionHandler
    @SendToUser("/queue/errors")
    public String handleException(Throwable ex) {
        log.error("WebSocket error: {}", ex.getMessage());
        return ex.getMessage();
    }
}

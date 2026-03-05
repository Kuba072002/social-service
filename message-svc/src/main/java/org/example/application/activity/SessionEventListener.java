package org.example.application.activity;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.example.application.activity.service.ActiveUserService;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.security.Principal;

@Slf4j
@Component
@RequiredArgsConstructor
public class SessionEventListener {
    private final ActiveUserService activeUserService;

    @EventListener
    public void handle(SessionConnectEvent event) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(event.getMessage(), StompHeaderAccessor.class);
        var userId = getUserId(event.getUser());
        if (StringUtils.isNotBlank(userId) && accessor != null) {
            activeUserService.handleConnectedUser(userId, accessor.getSessionId());
        }
    }

    @EventListener
    public void handle(SessionDisconnectEvent event) {
        try {
            String sessionId = event.getSessionId();
            activeUserService.handleDisconnectedUser(sessionId);
        } catch (Exception ex) {
            log.error("Error handling SessionDisconnectEvent: {}", ex.getMessage());
        }
    }

    private static String getUserId(Principal principal) {
        return principal != null ? principal.getName() : null;
    }
}

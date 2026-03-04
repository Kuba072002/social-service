package org.example.application.activity;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.example.application.event.OutboundMessagingService;
import org.example.application.event.UserStatusEvent;
import org.example.domain.activity.ActiveUserService;
import org.example.domain.user.UserService;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.security.Principal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class SessionEventListener {
    private final ActiveUserService activeUserService;
    private final OutboundMessagingService outboundMessagingService;
    private final UserService userService;

    @EventListener
    public void handle(SessionConnectEvent event) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(event.getMessage(), StompHeaderAccessor.class);
        var userId = getUserId(event.getUser());
        if (StringUtils.isNotBlank(userId) && accessor != null) {
            activeUserService.userConnected(userId, accessor.getSessionId());
            outboundMessagingService.broadcastUserStatus(userId, UserStatusEvent.Status.ONLINE);
        }
    }

    @EventListener
    public void handle(SessionDisconnectEvent event) {
        try {
            String sessionId = event.getSessionId();
            String userId = activeUserService.userDisconnected(sessionId);
            if (userId == null) {
                log.warn("No userId found for sessionId = {}", sessionId);
                return;
            }
            if (!activeUserService.isUserOnline(userId)) {
                outboundMessagingService.broadcastUserStatus(userId, UserStatusEvent.Status.OFFLINE);
                var lastSeenAt = Instant.now().minusSeconds(10).truncatedTo(ChronoUnit.SECONDS);
                userService.updateLastSeenAt(Long.valueOf(userId), lastSeenAt);
            }
        } catch (Exception ex) {
            log.error("Error handling SessionDisconnectEvent: {}", ex.getMessage());
        }
    }

    private static String getUserId(Principal principal) {
        return principal != null ? principal.getName() : null;
    }
}

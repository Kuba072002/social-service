package org.example.application.activity;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.example.application.event.OutboundMessagingService;
import org.example.application.event.UserStatusEvent;
import org.example.domain.activity.ActiveUserService;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.*;

import java.security.Principal;

@Slf4j
@Component
@RequiredArgsConstructor
public class SessionEventListener {
    private final ActiveUserService activeUserService;
    private final OutboundMessagingService outboundMessagingService;

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
        String sessionId = event.getSessionId();
        String userId = activeUserService.getUserIdBySession(sessionId);
        if (userId != null) {
            outboundMessagingService.broadcastUserStatus(userId, UserStatusEvent.Status.OFFLINE);
        }
        activeUserService.userDisconnected(sessionId);
        //TO_DO update last seen at in user_service
    }

    @EventListener(classes = {SessionSubscribeEvent.class, SessionUnsubscribeEvent.class})
    public void handle(AbstractSubProtocolEvent event) {
        switch (event) {
            case SessionSubscribeEvent sub -> outboundMessagingService
                    .broadcastUserStatus(getUserId(sub.getUser()), UserStatusEvent.Status.ONLINE);
            case SessionUnsubscribeEvent unsub -> outboundMessagingService
                    .broadcastUserStatus(getUserId(unsub.getUser()), UserStatusEvent.Status.OFFLINE);
            default -> {
                log.warn("Unknown event: {}", event);
            }
        }
    }

    private static String getUserId(Principal principal) {
        return principal != null ? principal.getName() : null;
    }
}

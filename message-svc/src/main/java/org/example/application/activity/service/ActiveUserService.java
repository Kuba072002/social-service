package org.example.application.activity.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.application.chat.ChatAccessValidator;
import org.example.application.dto.ChatActivityDTO;
import org.example.application.dto.WsEvent;
import org.example.application.event.MessageEvent;
import org.example.application.event.OutboundMessagingService;
import org.example.application.event.UserStatusEvent;
import org.example.domain.activity.ActiveUserRegistry;
import org.example.domain.user.UserService;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ActiveUserService {
    private final ChatAccessValidator chatAccessValidator;
    private final UserService userService;
    private final OutboundMessagingService outboundMessagingService;
    private final ActiveUserRegistry activeUserRegistry;

    public void updateLastRead(Long userId, Long chatId, Instant lastReadAt) {
        Set<Long> participants = chatAccessValidator.getParticipantsIfAllowed(chatId, userId);
        outboundMessagingService.broadcastMessageAndPublishEvent(
                activeUserRegistry.getActiveUsers(participants),
                userId,
                WsEvent.of(new ChatActivityDTO(chatId, userId, lastReadAt)),
                MessageEvent.get(chatId, userId, lastReadAt)
        );
    }

    public void handleConnectedUser(String userId, String sessionId) {
        activeUserRegistry.userConnected(userId, sessionId);
        outboundMessagingService.broadcastUserStatus(userId, UserStatusEvent.Status.ONLINE);
    }

    public void handleDisconnectedUser(String sessionId) {
        String userId = activeUserRegistry.userDisconnected(sessionId);
        if (userId == null) {
            log.warn("No userId found for sessionId = {}", sessionId);
            return;
        }
        if (!activeUserRegistry.isUserOnline(userId)) {
            outboundMessagingService.broadcastUserStatus(userId, UserStatusEvent.Status.OFFLINE);
            var lastSeenAt = Instant.now().minusSeconds(10).truncatedTo(ChronoUnit.SECONDS);
            userService.updateLastSeenAt(Long.valueOf(userId), lastSeenAt);
        }
    }

    public Map<Long, Boolean> getActiveUsersMap(Set<Long> userIds) {
        Set<Long> activeUsers = activeUserRegistry.getActiveUsers(userIds);
        return userIds.stream()
                .collect(Collectors.toMap(
                        Function.identity(),
                        activeUsers::contains
                ));
    }

}

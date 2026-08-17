package org.example.application.interceptor;

import lombok.extern.slf4j.Slf4j;
import org.example.ApplicationException;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.security.Principal;

import static org.example.common.MessageApplicationError.INVALID_SUBSCRIPTION_DESTINATION;
import static org.example.common.MessageApplicationError.INVALID_TOKEN;

@Slf4j
@Component
public class StompAuthInterceptor implements ChannelInterceptor {
    private static final String USER_MESSAGES_QUEUE = "/user/queue/messages";
    private static final String USER_ERRORS_QUEUE = "/user/queue/errors";
    private static final String USER_STATUS_TOPIC_PREFIX = "/topic/status.";

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (accessor == null) {
            log.debug("preSend() >> No STOMP accessor, raw message: {}", message);
            return message;
        }
        log.debug("preSend() >> With STOMP accessor, user: {}, command: {}",
                accessor.getUser(), accessor.getCommand());
        if (StompCommand.SUBSCRIBE.equals(accessor.getCommand())) {
            validateSubscription(accessor);
        }
        return message;
    }

    private void validateSubscription(StompHeaderAccessor accessor) {
        Principal user = accessor.getUser();
        if (user == null || !StringUtils.hasText(user.getName())) {
            throw new ApplicationException(INVALID_TOKEN);
        }

        String destination = accessor.getDestination();
        if (isAllowedUserQueue(destination) || isAllowedStatusTopic(destination, user.getName())) {
            return;
        }

        log.warn("Rejected STOMP subscription: userId = {}, destination = {}", user.getName(), destination);
        throw new ApplicationException(INVALID_SUBSCRIPTION_DESTINATION);
    }

    private boolean isAllowedUserQueue(String destination) {
        return USER_MESSAGES_QUEUE.equals(destination) || USER_ERRORS_QUEUE.equals(destination);
    }

    private boolean isAllowedStatusTopic(String destination, String userId) {
        return (USER_STATUS_TOPIC_PREFIX + userId).equals(destination);
    }
}

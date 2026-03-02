package org.example.application.interceptor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;

@Component
public class StompAuthInterceptor implements ChannelInterceptor {

    private static final Logger LOGGER = LoggerFactory.getLogger(StompAuthInterceptor.class);

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (accessor == null) {
            LOGGER.debug("preSend() >> No STOMP accessor, raw message: {}", message);
            return message;
        }
        LOGGER.debug("preSend() >> With STOMP accessor, user: {}, command: {}",
                accessor.getUser(), accessor.getCommand());
        return message;
    }
}

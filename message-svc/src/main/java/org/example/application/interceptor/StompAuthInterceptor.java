package org.example.application.interceptor;

import com.sun.security.auth.UserPrincipal;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.example.ApplicationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

import static org.example.common.MessageApplicationError.INVALID_TOKEN;
import static org.example.common.MessageApplicationError.MISSING_AUTH_HEADER;

@Component
public class StompAuthInterceptor implements ChannelInterceptor {

    private final SecretKey key;

    private static final Logger LOGGER = LoggerFactory.getLogger(StompAuthInterceptor.class);

    public StompAuthInterceptor(@Value("${jwt.secret}") String secret) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (accessor != null) {
            LOGGER.info("preSend() >> STOMP accessor, raw message: {}", message);
            if (StompCommand.CONNECT.equals(accessor.getCommand())) {
                String authHeader = accessor.getFirstNativeHeader(HttpHeaders.AUTHORIZATION);
                String userId;

                if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                    throw new ApplicationException(MISSING_AUTH_HEADER);
                }
                try {
                    String token = authHeader.substring(7);
                    userId = validateToken(token);
                } catch (JwtException ex) {
                    throw new ApplicationException(INVALID_TOKEN);
                }
                accessor.setUser(new UserPrincipal(userId));
                LOGGER.info("preSend() >> Connected user: {}", accessor.getUser());
            }
        } else {
            LOGGER.info("preSend() >> No STOMP accessor, raw message: {}", message);
        }

        return message;
    }

    private String validateToken(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }
}

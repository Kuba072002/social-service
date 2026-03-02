package org.example.application.interceptor;

import com.sun.security.auth.UserPrincipal;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.example.ApplicationException;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.Principal;
import java.util.Map;

import static org.example.common.MessageApplicationError.INVALID_TOKEN;
import static org.example.common.MessageApplicationError.MISSING_AUTH_HEADER;

@Slf4j
@Component
public class UserHandshakeHandler extends DefaultHandshakeHandler {
    private final SecretKey key;

    public UserHandshakeHandler(@Value("${jwt.secret}") String secret) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    protected @Nullable Principal determineUser(
            ServerHttpRequest request, WebSocketHandler wsHandler, Map<String, Object> attributes
    ) {
        String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        String token = "";
        if (StringUtils.hasText(authHeader) && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7).strip();
        }
        if (!StringUtils.hasText(token)) {
            log.warn("WS handshake rejected — no token provided from {}",
                    request.getRemoteAddress());
            throw new ApplicationException(MISSING_AUTH_HEADER);
        }
        try {
            String userId = validateTokenAndGetSubject(token);
            attributes.put("userId", userId);
            attributes.put("token", token);
            return new UserPrincipal(userId);
        } catch (JwtException ex) {
            throw new ApplicationException(INVALID_TOKEN);
        }
    }

    private String validateTokenAndGetSubject(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }
}

package org.example;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

import static org.example.GatewayApplicationError.INVALID_AUTHORIZATION;

@Component
public class AuthenticationService {
    private final SecretKey key;

    public AuthenticationService(
            @Value("${jwt.secret}") String jwtSecret
    ) {
        this.key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }

    public Mono<Long> getUserId(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return Mono.error(new ApplicationException(INVALID_AUTHORIZATION));
        }

        String token = authHeader.substring(7);
        var userIdSubject = validateTokenAndGetSubject(token);
        if (StringUtils.isBlank(userIdSubject)) {
            return Mono.error(new ApplicationException(INVALID_AUTHORIZATION));
        }
        return Mono.just(Long.parseLong(userIdSubject));
    }

    public String validateTokenAndGetSubject(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }
}

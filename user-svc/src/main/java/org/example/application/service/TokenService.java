package org.example.application.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.example.ApplicationException;
import org.example.comon.UserApplicationError;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.codec.Hex;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.UUID;

@Component
public class TokenService {
    public static final String ACCESS_TYPE = "ACCESS";
    public static final String REFRESH_TYPE = "REFRESH";

    @Value("${jwt.access.secret}")
    private String accessSecret;
    @Value("${jwt.refresh.secret}")
    private String refreshSecret;
    @Value("${jwt.access.expiration}")
    private int accessExpirationMillis;

    @Value("${jwt.refresh.expiration}")
    private int refreshExpirationMillis;

    private SecretKey accessSecretKey;
    private SecretKey refreshSecretKey;

    @PostConstruct
    public void init() {
        this.accessSecretKey = Keys.hmacShaKeyFor(accessSecret.getBytes(StandardCharsets.UTF_8));
        this.refreshSecretKey = Keys.hmacShaKeyFor(refreshSecret.getBytes(StandardCharsets.UTF_8));
    }

    public String generate(Long userId, String userName, String type) {
        var key = type.equals(REFRESH_TYPE) ? refreshSecretKey : accessSecretKey;
        var expirationMillis = type.equals(REFRESH_TYPE) ? refreshExpirationMillis : accessExpirationMillis;
        Date issuedAt = new Date();
        var expirationDate = new Date(issuedAt.getTime() + expirationMillis);
        return Jwts.builder()
                .id(UUID.randomUUID().toString())
                .subject(userId.toString())
                .claim("userName", userName)
                .issuedAt(issuedAt)
                .expiration(expirationDate)
                .signWith(key)
                .compact();
    }

    public String hash(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            return new String(Hex.encode(hash));
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not found", e);
        }
    }

    public Claims validateAndGetClaims(String token, String type) {
        var key = type.equals(REFRESH_TYPE) ? refreshSecretKey : accessSecretKey;
        try {
            return Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (JwtException e) {
            throw new ApplicationException(UserApplicationError.INVALID_AUTH_DATA);
        }

    }

}

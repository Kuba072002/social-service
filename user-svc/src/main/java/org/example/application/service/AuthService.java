package org.example.application.service;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.example.ApplicationException;
import org.example.application.dto.SignInResponse;
import org.example.domain.service.UserService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

import static org.example.comon.UserApplicationError.INVALID_AUTH_DATA;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserService userService;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    @Value("${jwt.secret}")
    private String jwtSecret;
    @Value("${jwt.expiration}")
    private int jwtExpirationMillis;
    private SecretKey key;

    @PostConstruct
    public void init() {
        this.key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }

    public SignInResponse authUser(String email, String password) {
        var user = userService.findByEmail(email)
                .filter(u -> passwordEncoder.matches(password, u.getPassword()))
                .orElseThrow(() -> new ApplicationException(INVALID_AUTH_DATA));
        var token = generateToken(user.getId(), user.getUserName());
        return new SignInResponse(userMapper.toUserDTO(user), token);
    }

    public String generateToken(Long userId, String userName) {
        return Jwts.builder()
                .subject(userId.toString())
                .claim("userName", userName)
                .issuedAt(new Date())
                .expiration(new Date((new Date()).getTime() + jwtExpirationMillis))
                .signWith(key)
                .compact();
    }
}

package org.example.domain.activity;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class ActiveUserService {

    private static final String USER_INFO_PREFIX = "ws:user:";
    private static final String SESSION_PREFIX = "ws:session:";

    private static final Duration USER_TTL = Duration.ofMinutes(2);

    private final StringRedisTemplate redis;

    public void userConnected(String userId, String sessionId) {
        redis.opsForValue().set(SESSION_PREFIX + sessionId, userId, USER_TTL);

        String userInfoKey = USER_INFO_PREFIX + userId;
        Map<String, String> info = new HashMap<>();
        info.put("sessionId", sessionId);
        info.put("connectedAt", Instant.now().toString());
        info.put("lastSeen", Instant.now().toString());
        redis.opsForHash().putAll(userInfoKey, info);
        redis.expire(userInfoKey, USER_TTL);

        log.debug("User connected: userId = {}, sessionId = {}", userId, sessionId);
    }

    public void userDisconnected(String sessionId) {
        String userId = redis.opsForValue().get(SESSION_PREFIX + sessionId);
        if (userId == null) {
            log.warn("No userId found for sessionId = {}", sessionId);
            return;
        }
        redis.delete(SESSION_PREFIX + sessionId);
        redis.delete(USER_INFO_PREFIX + userId);

        log.debug("User disconnected: userId = {}, sessionId = {}", userId, sessionId);
    }

    public void refreshLastSeen(String userId, String sessionId) {
        String userInfoKey = USER_INFO_PREFIX + userId;
        redis.opsForHash().put(userInfoKey, "lastSeen", Instant.now().toString());
        redis.expire(userInfoKey, USER_TTL);
        redis.expire(SESSION_PREFIX + sessionId, USER_TTL);
    }

    public String getUserIdBySession(String sessionId) {
        return redis.opsForValue().get(SESSION_PREFIX + sessionId);
    }

}

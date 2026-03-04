package org.example.domain.activity;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ActiveUserService {

    private static final String USER_INFO_PREFIX = "ws:user:";
    private static final String SESSION_PREFIX = "ws:session:";

    private final StringRedisTemplate redis;

    public void userConnected(String userId, String sessionId) {
        redis.opsForSet().add(USER_INFO_PREFIX + userId, sessionId);
        redis.opsForValue().set(SESSION_PREFIX + sessionId, userId);
        log.info("User connected: userId = {}, sessionId = {}", userId, sessionId);
    }

    public String userDisconnected(String sessionId) {
        String userId = getUserIdBySession(sessionId);
        if (userId != null) {
            redis.opsForSet().remove(USER_INFO_PREFIX + userId, sessionId);
            redis.delete(SESSION_PREFIX + sessionId);
            log.info("User disconnected: userId = {}, sessionId = {}", userId, sessionId);
        }
        return userId;
    }

    public String getUserIdBySession(String sessionId) {
        return redis.opsForValue().get(SESSION_PREFIX + sessionId);
    }

    public boolean isUserOnline(String userId) {
        Long count = redis.opsForSet().size(USER_INFO_PREFIX + userId);
        return count != null && count > 0;
    }

    public Map<Long, Boolean> getActiveUsers(Set<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return Collections.emptyMap();
        }
        return userIds.stream()
                .collect(Collectors.toMap(
                        Function.identity(),
                        userId -> BooleanUtils.isTrue(redis.hasKey(USER_INFO_PREFIX + userId)))
                );
    }

}

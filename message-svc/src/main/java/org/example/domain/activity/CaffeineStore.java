package org.example.domain.activity;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "spring.cache.type", havingValue = "caffeine", matchIfMissing = true)
class CaffeineStore implements SessionStore {
    private final CacheManager cacheManager;

    @Override
    public void saveSession(SessionInfo sessionInfo) {
        Cache cache = cacheManager.getCache("session_info");
        if (cache != null) {
            cache.put(sessionInfo.sessionId(), sessionInfo.userId());
        }
    }

    @Override
    public void deleteSession(String sessionId) {
        Cache cache = cacheManager.getCache("session_info");
        if (cache != null) {
            cache.evict(sessionId);
        }
    }

    @Override
    public Optional<SessionInfo> findSession(String sessionId) {
        Cache cache = cacheManager.getCache("session_info");
        if (cache != null) {
            return Optional.ofNullable(cache.get(sessionId, String.class))
                    .map(userId -> new SessionInfo(sessionId, userId));
        }
        return Optional.empty();
    }

    @Override
    public UserSessionInfo saveUserSession(UserSessionInfo userSessionInfo) {
        Cache cache = cacheManager.getCache("user_info");
        if (cache != null) {
            cache.put(userSessionInfo.userId(), userSessionInfo.sessionIds());
        }
        return userSessionInfo;
    }

    @Override
    public void deleteUserSession(String userId) {
        Cache cache = cacheManager.getCache("user_info");
        if (cache != null) {
            cache.evict(userId);
        }
    }

    @Override
    public Optional<UserSessionInfo> findUserSession(String userId) {
        Cache cache = cacheManager.getCache("user_info");
        if (cache != null) {
            List<String> sessionIds = cache.get(userId, List.class);
            return Optional.ofNullable(sessionIds)
                    .map(s -> new UserSessionInfo(userId, s));
        }
        return Optional.empty();
    }

    @Override
    public boolean existUserSession(String userId) {
        Cache cache = cacheManager.getCache("user_info");
        if (cache != null) {
            return cache.get(userId) != null;
        }
        return false;
    }
}

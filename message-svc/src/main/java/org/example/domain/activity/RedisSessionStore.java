package org.example.domain.activity;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "spring.cache.type", havingValue = "redis")
class RedisSessionStore implements SessionStore {
    private final SessionInfoRepository sessionInfoRepository;
    private final UserSessionInfoRepository userSessionInfoRepository;

    @Override
    public void saveSession(SessionInfo sessionInfo) {
        sessionInfoRepository.save(sessionInfo);
    }

    @Override
    public void deleteSession(String sessionId) {
        sessionInfoRepository.deleteById(sessionId);
    }

    @Override
    public Optional<SessionInfo> findSession(String sessionId) {
        return sessionInfoRepository.findById(sessionId);
    }

    @Override
    public UserSessionInfo saveUserSession(UserSessionInfo userSessionInfo) {
        return userSessionInfoRepository.save(userSessionInfo);
    }

    @Override
    public void deleteUserSession(String userId) {
        userSessionInfoRepository.deleteById(userId);
    }

    @Override
    public Optional<UserSessionInfo> findUserSession(String userId) {
        return userSessionInfoRepository.findById(userId);
    }

    @Override
    public boolean existUserSession(String userId) {
        return userSessionInfoRepository.existsById(userId);
    }
}

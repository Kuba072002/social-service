package org.example.domain.activity;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ActiveUserRegistry {
    private final SessionInfoRepository sessionInfoRepository;
    private final UserSessionInfoRepository userSessionInfoRepository;

    public void userConnected(String userId, String sessionId) {
        sessionInfoRepository.save(new SessionInfo(sessionId, userId));
        userSessionInfoRepository.findById(userId)
                .map(existing -> {
                    List<String> updatedSessions = new ArrayList<>(existing.sessionIds());
                    if (!updatedSessions.contains(sessionId)) {
                        updatedSessions.add(sessionId);
                    }
                    return userSessionInfoRepository.save(
                            new UserSessionInfo(existing.userId(), updatedSessions));
                })
                .orElseGet(() -> userSessionInfoRepository.save(
                        new UserSessionInfo(userId, List.of(sessionId))));
    }

    public String userDisconnected(String sessionId) {
        String userId = getUserIdBySession(sessionId);
        if (userId != null) {
            sessionInfoRepository.deleteById(sessionId);
            userSessionInfoRepository.findById(userId)
                    .ifPresent(existing -> {
                        if (!existing.sessionIds().contains(sessionId)) {
                            return;
                        }
                        if (existing.sessionIds().size() == 1) {
                            userSessionInfoRepository.deleteById(userId);
                            return;
                        }
                        List<String> updatedSessions = new ArrayList<>(existing.sessionIds());
                        updatedSessions.remove(sessionId);
                        userSessionInfoRepository.save(
                                new UserSessionInfo(existing.userId(), updatedSessions));
                    });
        }
        return userId;
    }

    public String getUserIdBySession(String sessionId) {
        return sessionInfoRepository.findById(sessionId)
                .map(SessionInfo::userId)
                .orElse(null);
    }

    public boolean isUserOnline(String userId) {
        int count = userSessionInfoRepository.findById(userId)
                .map(userSessionInfo -> userSessionInfo.sessionIds().size())
                .orElse(0);
        return count > 0;
    }

    public Set<Long> getActiveUsers(Set<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return Collections.emptySet();
        }
        return userIds.stream()
                .filter(userId -> userSessionInfoRepository.existsById(String.valueOf(userId)))
                .collect(Collectors.toSet());
    }

}

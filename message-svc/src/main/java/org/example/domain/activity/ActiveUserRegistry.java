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
    private final SessionStore sessionStore;

    public void userConnected(String userId, String sessionId) {
        sessionStore.saveSession(new SessionInfo(sessionId, userId));
        sessionStore.findUserSession(userId)
                .ifPresentOrElse(existing -> {
                    List<String> updatedSessions = new ArrayList<>(existing.sessionIds());
                    if (!updatedSessions.contains(sessionId)) {
                        updatedSessions.add(sessionId);
                    }
                    sessionStore.saveUserSession(
                            new UserSessionInfo(existing.userId(), updatedSessions));
                }, () -> sessionStore.saveUserSession(
                        new UserSessionInfo(userId, List.of(sessionId))));
    }

    public String userDisconnected(String sessionId) {
        String userId = getUserIdBySession(sessionId);
        if (userId != null) {
            sessionStore.deleteSession(sessionId);
            sessionStore.findUserSession(userId)
                    .ifPresent(existing -> {
                        if (!existing.sessionIds().contains(sessionId)) {
                            return;
                        }
                        if (existing.sessionIds().size() == 1) {
                            sessionStore.deleteUserSession(userId);
                            return;
                        }
                        List<String> updatedSessions = new ArrayList<>(existing.sessionIds());
                        updatedSessions.remove(sessionId);
                        sessionStore.saveUserSession(
                                new UserSessionInfo(existing.userId(), updatedSessions));
                    });
        }
        return userId;
    }

    public String getUserIdBySession(String sessionId) {
        return sessionStore.findSession(sessionId)
                .map(SessionInfo::userId)
                .orElse(null);
    }

    public boolean isUserOnline(String userId) {
        int count = sessionStore.findUserSession(userId)
                .map(userSessionInfo -> userSessionInfo.sessionIds().size())
                .orElse(0);
        return count > 0;
    }

    public Set<Long> getActiveUsers(Set<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return Collections.emptySet();
        }
        return userIds.stream()
                .filter(userId -> sessionStore.existUserSession(String.valueOf(userId)))
                .collect(Collectors.toSet());
    }

}

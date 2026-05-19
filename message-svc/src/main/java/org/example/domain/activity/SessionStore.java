package org.example.domain.activity;

import java.util.Optional;

public interface SessionStore {
    void saveSession(SessionInfo sessionInfo);

    void deleteSession(String sessionId);

    Optional<SessionInfo> findSession(String sessionId);

    UserSessionInfo saveUserSession(UserSessionInfo userSessionInfo);

    void deleteUserSession(String userId);

    Optional<UserSessionInfo> findUserSession(String userId);

    boolean existUserSession(String userId);
}

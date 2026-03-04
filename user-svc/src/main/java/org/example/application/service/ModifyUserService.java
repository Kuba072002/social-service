package org.example.application.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.ApplicationException;
import org.example.domain.service.UserService;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.example.comon.UserApplicationError.USER_NOT_EXISTS;

@Service
@RequiredArgsConstructor
public class ModifyUserService {
    private final UserService userService;

    @Transactional
    public void updateLastSeenAt(Long userId, Instant lastSeenAt) {
        var user = userService.findByIdForUpdate(userId)
                .orElseThrow(() -> new ApplicationException(USER_NOT_EXISTS));
        lastSeenAt = lastSeenAt.truncatedTo(ChronoUnit.SECONDS);
        if (user.getLastSeenAt() == null || lastSeenAt.isAfter(user.getLastSeenAt())) {
            user.setLastSeenAt(lastSeenAt);
            userService.save(user);
        }
    }
}

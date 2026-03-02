package org.example.application.event;

import java.time.Instant;

public record UserStatusEvent(
        String userId,
        Status status,
        Instant timestamp
) {

    public enum Status {
        ONLINE,
        OFFLINE
    }
}

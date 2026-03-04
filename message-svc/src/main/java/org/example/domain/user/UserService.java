package org.example.domain.user;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PatchExchange;

import java.time.Instant;

@HttpExchange(accept = "application/json", contentType = "application/json", url = "${user.service.url}")
public interface UserService {
    @PatchExchange("/internal/users/{userId}")
    void updateLastSeenAt(@PathVariable Long userId, @RequestParam Instant lastSeenAt);
}

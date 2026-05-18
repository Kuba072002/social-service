package org.example.domain.activity;

import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

import java.util.List;

@RedisHash("user_info")
public record UserSessionInfo(
        @Id
        String userId,
        List<String> sessionIds
) {
}

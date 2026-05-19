package org.example.domain.activity;

import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

import java.util.List;

@RedisHash(value = "user_info", timeToLive =  300)
public record UserSessionInfo(
        @Id
        String userId,
        List<String> sessionIds
) {
}

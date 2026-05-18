package org.example.domain.activity;

import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

@RedisHash("session_info")
public record SessionInfo(
    @Id
     String sessionId,
     String userId
){
}

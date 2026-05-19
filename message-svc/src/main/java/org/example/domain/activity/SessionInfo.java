package org.example.domain.activity;

import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

@RedisHash(value = "session_info", timeToLive =  300)
public record SessionInfo(
    @Id
     String sessionId,
     String userId
){
}

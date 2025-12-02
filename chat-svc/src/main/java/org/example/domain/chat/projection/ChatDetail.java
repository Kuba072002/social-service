package org.example.domain.chat.projection;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatDetail {
    private Long chatId;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String name;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String imageUrl;
    private Boolean isPrivate;
    private Instant lastMessageAt;
    private Instant lastReadAt;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Long otherUser;

    public ChatDetail(
            Long chatId, Boolean isPrivate,
            Instant lastMessageAt, Instant lastReadAt,
            Long otherUser
    ) {
        this.chatId = chatId;
        this.isPrivate = isPrivate;
        this.lastMessageAt = lastMessageAt;
        this.lastReadAt = lastReadAt;
        this.otherUser = otherUser;
    }

    public ChatDetail(
            Long chatId, String name,
            String imageUrl, Boolean isPrivate,
            Instant lastMessageAt, Instant lastReadAt
    ) {
        this.chatId = chatId;
        this.name = name;
        this.imageUrl = imageUrl;
        this.isPrivate = isPrivate;
        this.lastMessageAt = lastMessageAt;
        this.lastReadAt = lastReadAt;
    }

}

package org.example.domain.chat.projection;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.application.chat.dto.ParticipantDTO;
import org.example.domain.chat.entity.ChatType;

import java.time.Instant;
import java.util.List;

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
    @Enumerated(EnumType.STRING)
    private ChatType chatType;
    private Instant lastMessageAt;
    private Instant lastReadAt;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Long otherUserId;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<ParticipantDTO> participants;

    public ChatDetail(
            Long chatId, String chatType,
            Instant lastMessageAt, Instant lastReadAt,
            Long otherUserId
    ) {
        this.chatId = chatId;
        this.chatType = ChatType.valueOf(chatType);
        this.lastMessageAt = lastMessageAt;
        this.lastReadAt = lastReadAt;
        this.otherUserId = otherUserId;
    }

    public ChatDetail(
            Long chatId, String name,
            String imageUrl, String chatType,
            Instant lastMessageAt, Instant lastReadAt
    ) {
        this.chatId = chatId;
        this.name = name;
        this.imageUrl = imageUrl;
        this.chatType = ChatType.valueOf(chatType);
        this.lastMessageAt = lastMessageAt;
        this.lastReadAt = lastReadAt;
    }

}

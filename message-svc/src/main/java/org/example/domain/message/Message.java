package org.example.domain.message;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "messages")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@IdClass(MessageId.class)
public class Message {
    @Id
    @Column(name = "chat_id")
    private Long chatId;
    @Id
    @Column(name = "message_id")
    private UUID messageId;
    @Column(name = "sender_id")
    private Long senderId;
    @Column(length = 2000)
    private String content;
    @Column(name = "media_content")
    private String mediaContent;
    private Instant createdAt;

}

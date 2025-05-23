package org.example.domain.chat.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

@Entity
@Table(name = "chat_participants",
        indexes = @Index(columnList = "user_id"),
        uniqueConstraints = @UniqueConstraint(columnNames = {"chat_id", "user_id"})
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatParticipant {
    @Id
    @GeneratedValue
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_id", referencedColumnName = "id", nullable = false)
    private Chat chat;
    @Column(name = "chat_id", insertable = false, updatable = false)
    private Long chatId;
    private Long userId;
    private String role;
    private Instant lastReadAt;
    @CreationTimestamp
    private Instant joinedAt;

    public ChatParticipant(Chat chat, Long userId) {
        this.chat = chat;
        this.userId = userId;
        this.lastReadAt = Instant.now();
    }

    public ChatParticipant(Chat chat, Long userId, String role) {
        this.chat = chat;
        this.userId = userId;
        this.role = role;
        this.lastReadAt = Instant.now();
    }
}

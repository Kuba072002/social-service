package org.example.domain.chat.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

@Entity
@Table(name = "chat_participants")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatParticipant {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_id", referencedColumnName = "id", nullable = false)
    private Chat chat;
    @Column(name = "chat_id", insertable = false, updatable = false)
    private Long chatId;
    @Column(name = "user_id")
    private Long userId;
    @Enumerated(EnumType.STRING)
    private ChatParticipantRole role;
    @Column(name = "last_read_at")
    private Instant lastReadAt;
    @CreationTimestamp
    private Instant joinedAt;

    public ChatParticipant(Chat chat, Long userId) {
        this.chat = chat;
        this.userId = userId;
        this.lastReadAt = Instant.now();
    }

    public ChatParticipant(Chat chat, Long userId, ChatParticipantRole role) {
        this.chat = chat;
        this.userId = userId;
        this.role = role;
        this.lastReadAt = Instant.now();
    }
}

package org.example.domain.chat.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.List;

@Entity
@Table(name = "chats")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Chat {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String imageUrl;
    @Enumerated(EnumType.STRING)
    @Column(name = "chat_type")
    private ChatType chatType;//change to enum
    @Column(name = "last_message_at")
    private Instant lastMessageAt;
    @Column(name = "private_pair_key", updatable = false)
    private String privatePairKey;
    @CreationTimestamp
    private Instant createdAt;
    @OneToMany(mappedBy = "chat", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ChatParticipant> participants;

    @Override
    public String toString() {
        return "Chat{" +
                "name='" + name + '\'' +
                ", lastMessageAt=" + lastMessageAt +
                ", chatType=" + chatType +
                ", imageUrl='" + imageUrl + '\'' +
                ", id=" + id +
                ", createdAt=" + createdAt +
                '}';
    }
}

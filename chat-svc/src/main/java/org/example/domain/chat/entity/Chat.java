package org.example.domain.chat.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;

import java.time.Instant;
import java.util.List;

@Entity
@Table(name = "chats",
        indexes = {
                @Index(columnList = "char_id")
        }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Chat {
    @Id
    @GeneratedValue
    private Long id;
    private String name;
    private String imageUrl;
    private boolean isPrivate;
    @CreatedDate
    private Instant createdAt;
    @OneToMany(mappedBy = "chat")
    private List<ChatParticipant> participants;
}

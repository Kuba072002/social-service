package org.example.domain.message;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.springframework.data.annotation.CreatedDate;

import java.time.Instant;

@Entity
@Table(name = "messages",
        indexes = {
                @Index(columnList = "chat_id")
        }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Message {
    @Id
    @GeneratedValue
    private Long id;
    private Long chatId;
    private Long senderId;
    @JdbcTypeCode(SqlTypes.JSON)
    private MessageContent content;
    @CreatedDate
    private Instant createdAt;

}

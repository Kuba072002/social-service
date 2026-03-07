package org.example.domain.chat;

import lombok.Data;
import org.springframework.data.cassandra.core.mapping.Column;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;

import java.util.Set;

@Table("chats")
@Data
public class Chat {
    @PrimaryKey("chat_id")
    private Long chatId;

    @Column("participant_ids")
    private Set<Long> participantIds;

    @Column("updated_at")
    private Long updatedAt;
}

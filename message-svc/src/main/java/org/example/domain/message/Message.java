package org.example.domain.message;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.cassandra.core.cql.Ordering;
import org.springframework.data.cassandra.core.cql.PrimaryKeyType;
import org.springframework.data.cassandra.core.mapping.Column;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn;
import org.springframework.data.cassandra.core.mapping.Table;

import java.time.Instant;

@Table("messages")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Message {

    @PrimaryKeyColumn(name = "chat_id", type = PrimaryKeyType.PARTITIONED)
    private Long chatId;
    @Column("sender_id")
    private Long senderId;
    private String content;
    @Column("media_content")
    private String mediaContent;
    @PrimaryKeyColumn(name = "created_at", type = PrimaryKeyType.CLUSTERED, ordering = Ordering.DESCENDING)
    private Instant createdAt;

}

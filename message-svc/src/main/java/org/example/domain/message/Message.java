package org.example.domain.message;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;

import java.time.Instant;
import java.util.UUID;

@Table
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Message {

    @PrimaryKey
    private UUID id;
    private Long chatId;
    private Long senderId;
    private String content;
    private String mediaContent;
    @CreatedDate
    private Instant createdAt;

}

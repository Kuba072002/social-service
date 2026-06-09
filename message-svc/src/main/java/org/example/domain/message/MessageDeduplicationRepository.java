package org.example.domain.message;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.PreparedStatement;
import com.datastax.oss.driver.api.core.cql.Row;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class MessageDeduplicationRepository {
    private final CqlSession session;

    private static final String INSERT_IF_NOT_EXISTS = """
            INSERT INTO message_dedup (
                sender_id,
                client_message_id
            )
            VALUES (?, ?)
            IF NOT EXISTS
            """;

    public boolean insertIfNotExists(
            Long senderId,
            UUID clientMessageId
    ) {

        PreparedStatement preparedStatement = session.prepare(INSERT_IF_NOT_EXISTS);

        Row row = session
                .execute(preparedStatement.bind(senderId, clientMessageId))
                .one();

        return row != null && row.getBoolean("[applied]");
    }
}

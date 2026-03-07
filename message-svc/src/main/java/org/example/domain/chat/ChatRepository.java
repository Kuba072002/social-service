package org.example.domain.chat;

import org.springframework.data.cassandra.repository.Query;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Set;

@Repository
public interface ChatRepository extends ListCrudRepository<Chat, Long> {

    @Query("""
                INSERT INTO chats (chat_id, participant_ids, updated_at)
                VALUES (:chatId, :participants, :updatedAt)
                USING TIMESTAMP :timestamp
            """)
    void upsert(Long chatId, Set<Long> participants, Long updatedAt, long timestamp);

    @Query("""
                DELETE FROM chats
                WHERE chat_id = :chatId
                USING TIMESTAMP :timestamp
            """)
    void delete(Long chatId, long timestamp);
}

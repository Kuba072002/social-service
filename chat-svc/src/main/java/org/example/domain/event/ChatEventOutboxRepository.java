package org.example.domain.event;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ChatEventOutboxRepository extends JpaRepository<ChatEventOutbox,Long> {

    @Query("SELECT e FROM ChatEventOutbox e ORDER BY e.createdAt")
    List<ChatEventOutbox> findBatch(Pageable pageable);
}

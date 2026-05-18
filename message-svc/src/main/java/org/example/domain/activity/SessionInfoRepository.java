package org.example.domain.activity;

import org.springframework.data.repository.ListCrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SessionInfoRepository extends ListCrudRepository<SessionInfo, String> {
}

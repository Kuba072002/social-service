package org.example.domain.activity;

import org.springframework.data.repository.ListCrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserSessionInfoRepository extends ListCrudRepository<UserSessionInfo, String> {
}

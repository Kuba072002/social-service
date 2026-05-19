package org.example.domain.activity;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.stereotype.Repository;

@Repository
@ConditionalOnProperty(name = "spring.cache.type", havingValue = "redis")
interface UserSessionInfoRepository extends ListCrudRepository<UserSessionInfo, String> {
}

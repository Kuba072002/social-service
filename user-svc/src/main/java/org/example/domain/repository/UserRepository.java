package org.example.domain.repository;

import org.example.domain.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Boolean existsByUserNameOrEmail(String userName, String email);

    Optional<User> findByEmail(String email);
}

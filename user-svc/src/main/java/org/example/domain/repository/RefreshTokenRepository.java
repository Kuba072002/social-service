package org.example.domain.repository;

import jakarta.persistence.LockModeType;
import org.example.domain.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import java.util.List;
import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<RefreshToken> findByUserIdAndHashToken(Long userId, String hashToken);

    List<RefreshToken> findByUserId(Long userId);
}

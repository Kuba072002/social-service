package org.example.domain.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "refresh_tokens", indexes = @Index(columnList = "userId"))
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RefreshToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long userId;
    private String hashToken;
    private Long expiredAt;

    public RefreshToken(Long userId, String hashToken, Long expiredAt) {
        this.userId = userId;
        this.hashToken = hashToken;
        this.expiredAt = expiredAt;
    }
}

package org.example.domain.user;

public record UserDTO(
        Long id,
        String userName,
        String email,
        String imageUrl
) {
}

package org.example.dto.user;

public record UserDTO(
        Long id,
        String userName,
        String email,
        String imageUrl
) {
}

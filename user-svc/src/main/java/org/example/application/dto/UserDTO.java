package org.example.application.dto;

public record UserDTO(
        Long id,
        String userName,
        String email,
        String imageUrl
) {
}

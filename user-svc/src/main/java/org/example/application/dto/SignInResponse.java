package org.example.application.dto;

public record SignInResponse(
        UserDTO user,
        String token
) {
}

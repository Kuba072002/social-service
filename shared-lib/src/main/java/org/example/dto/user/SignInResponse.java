package org.example.dto.user;

public record SignInResponse(
        UserDTO user,
        String token
) {
}

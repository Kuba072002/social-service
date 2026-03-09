package org.example.application.dto;

import jakarta.validation.constraints.NotEmpty;

public record RefreshTokenRequest(
        @NotEmpty
        String refreshToken
) {
}

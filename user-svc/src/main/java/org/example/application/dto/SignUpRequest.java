package org.example.application.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SignUpRequest(
        @NotBlank
        @Size(min = 5, max = 20)
        String userName,
        @NotBlank
        @Size(max = 40)
        @Email
        String email,
        @NotBlank
        @Size(min = 6)
        String password,
        @NotBlank
        @Size(min = 6)
        String confirmPassword
) {
}

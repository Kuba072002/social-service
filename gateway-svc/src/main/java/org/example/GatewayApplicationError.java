package org.example;

import org.springframework.http.HttpStatus;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;


public enum GatewayApplicationError implements ApplicationError {
    INVALID_AUTHORIZATION(UNAUTHORIZED, "Invalid authorization."),
    INVALID_USER_HEADER(BAD_REQUEST, "Header is missing or invalid."),
    INVALID_RESPONSE(BAD_REQUEST, "Something went wrong.");

    private final HttpStatus status;
    private String message;

    GatewayApplicationError(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }

    public GatewayApplicationError formatted(Object... args) {
        this.message = String.format(message, args);
        return this;
    }

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public HttpStatus getStatus() {
        return status;
    }
}


package org.example;

import org.springframework.http.HttpStatus;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

public enum BasicApplicationError implements ApplicationError {
    INVALID_DATA(BAD_REQUEST, "Invalid data: %s.");

    private final HttpStatus status;
    private String message;

    BasicApplicationError(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }

    public BasicApplicationError formatted(Object... args) {
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

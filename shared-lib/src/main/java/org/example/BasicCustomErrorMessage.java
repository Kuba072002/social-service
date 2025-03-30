package org.example;

import org.springframework.http.HttpStatus;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

public enum BasicCustomErrorMessage implements CustomErrorMessage {
    INVALID_DATA(BAD_REQUEST, "Invalid data: %s.");

    private final HttpStatus status;
    private String message;

    BasicCustomErrorMessage(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }

    public BasicCustomErrorMessage formatted(Object... args) {
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

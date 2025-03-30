package org.example.common;

import lombok.Getter;
import org.springframework.http.HttpStatus;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

@Getter
public enum CustomErrorMessage {
    INVALID_DATA(BAD_REQUEST, "Invalid data: %s."),
    NOT_VALID_USERS(BAD_REQUEST, "Invalid users: %s"),
    PRIVATE_CHAT_ALREADY_EXISTS(BAD_REQUEST, "Private chat already exists."),
    REQUEST_GROUP_VALIDATION_NOT_EXISTS(BAD_REQUEST, "Request group validation not exists.");

    private final HttpStatus status;
    private String message;

    CustomErrorMessage(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }

    public CustomErrorMessage formatted(Object... args) {
        this.message = String.format(message, args);
        return this;
    }
}

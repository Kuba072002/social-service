package org.example.common;

import lombok.Getter;
import org.example.ApplicationError;
import org.springframework.http.HttpStatus;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

@Getter
public enum ChatApplicationError implements ApplicationError {
    NOT_VALID_USERS(BAD_REQUEST, "Invalid users: %s"),
    PRIVATE_CHAT_ALREADY_EXISTS(BAD_REQUEST, "Private chat already exists."),
    REQUEST_GROUP_VALIDATION_NOT_EXISTS(BAD_REQUEST, "Request group validation not exists."),
    CHAT_NOT_EXISTS(BAD_REQUEST, "Chat not exists."),
    CANNOT_ADD_TO_PRIVATE_CHAT(BAD_REQUEST, "Cannot add new user to private chat."),
    USER_IS_NOT_ADMIN(BAD_REQUEST, "User is not admin."),
    CHAT_PARTICIPANTS_ALREADY_EXISTS(BAD_REQUEST, "Chat participants already exists.");

    private final HttpStatus status;
    private String message;

    ChatApplicationError(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }

    public ChatApplicationError formatted(Object... args) {
        this.message = String.format(message, args);
        return this;
    }
}

package org.example.common;

import lombok.Getter;
import org.example.ApplicationError;
import org.springframework.http.HttpStatus;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@Getter
public enum ChatApplicationError implements ApplicationError {
    INVALID_USER_HEADER(BAD_REQUEST, "Header is missing or invalid."),
    INVALID_USERS(BAD_REQUEST, "Invalid users: %s"),
    PRIVATE_CHAT_ALREADY_EXISTS(BAD_REQUEST, "Private chat already exists."),
    REQUEST_CANNOT_CONTAIN_REQUESTER_ID(BAD_REQUEST, "Request cannot contain requester id."),
    CHAT_NOT_EXISTS(NOT_FOUND, "Chat not exists."),
    CANNOT_MODIFY_PRIVATE_CHAT(BAD_REQUEST, "Cannot modify private chat."),
    USER_IS_NOT_ADMIN(BAD_REQUEST, "User is not admin."),
    NO_PARTICIPANTS_LEFT_WITH_ADMIN_ROLE(BAD_REQUEST, "No participants left with admin role."),
    CANNOT_MODIFY_OWNER_ROLE(BAD_REQUEST, "Cannot modify owner role."),
    CHAT_PARTICIPANTS_ALREADY_EXISTS(BAD_REQUEST, "Chat participants already exists."),
    CHAT_PARTICIPANTS_NOT_EXISTS(BAD_REQUEST, "Chat participants not exists."),
    USER_DOES_NOT_BELONG_TO_CHAT(BAD_REQUEST, "User does not belong to chat."),
    FAILED_TO_FETCH_USERS(INTERNAL_SERVER_ERROR, "Failed to fetch users.");

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

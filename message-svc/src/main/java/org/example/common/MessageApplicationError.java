package org.example.common;

import lombok.Getter;
import org.example.ApplicationError;
import org.springframework.http.HttpStatus;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

@Getter
public enum MessageApplicationError implements ApplicationError {
    INVALID_USER_HEADER(BAD_REQUEST, "Header is missing or invalid."),
    NOT_INVOLVED_REQUESTER(BAD_REQUEST, "Requester is not involved in chat."),
    FROM_GREATER_THAN_TO(BAD_REQUEST, "From cannot be greater than to."),
    MESSAGE_NOT_FOUND(BAD_REQUEST, "Message not found."),
    SENDER_MISMATCH(BAD_REQUEST, "Sender of message mismatch.");

    private final HttpStatus status;
    private final String message;

    MessageApplicationError(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }
}

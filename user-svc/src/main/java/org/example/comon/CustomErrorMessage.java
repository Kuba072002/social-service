package org.example.comon;

import lombok.Getter;
import org.springframework.http.HttpStatus;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

@Getter
public enum CustomErrorMessage {
    INVALID_DATA(BAD_REQUEST, "Invalid data: %s."),
    USER_ALREADY_EXISTS(BAD_REQUEST, "User already exists."),
    CONFIRM_PASSWORD_DO_NOT_MATCH(BAD_REQUEST, "Confirm password do not match."),
    INVALID_AUTH_DATA(BAD_REQUEST, "Invalid authentication data");


    private final HttpStatus status;
    private final String message;

    private CustomErrorMessage(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }
}

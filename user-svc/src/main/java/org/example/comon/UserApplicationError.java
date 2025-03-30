package org.example.comon;

import lombok.Getter;
import org.example.ApplicationError;
import org.springframework.http.HttpStatus;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

@Getter
public enum UserApplicationError implements ApplicationError {
    USER_ALREADY_EXISTS(BAD_REQUEST, "User already exists."),
    CONFIRM_PASSWORD_DO_NOT_MATCH(BAD_REQUEST, "Confirm password do not match."),
    INVALID_AUTH_DATA(BAD_REQUEST, "Invalid authentication data."),
    USER_NOT_EXISTS(BAD_REQUEST, "User not exists.");


    private final HttpStatus status;
    private final String message;

    UserApplicationError(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }
}

package org.example;

import org.springframework.http.HttpStatus;

public interface CustomErrorMessage {
    String getMessage();

    HttpStatus getStatus();

}

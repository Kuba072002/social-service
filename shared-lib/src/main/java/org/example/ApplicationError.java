package org.example;

import org.springframework.http.HttpStatus;

public interface ApplicationError {
    String getMessage();

    HttpStatus getStatus();

}

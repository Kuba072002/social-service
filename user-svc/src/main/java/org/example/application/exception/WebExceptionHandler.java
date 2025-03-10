package org.example.application.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import static org.example.comon.CustomErrorMessage.INVALID_DATA;


@ControllerAdvice
public class WebExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ServiceResponse> handleValidationExceptions(MethodArgumentNotValidException e) {
        return ResponseEntity.status(INVALID_DATA.getStatus())
                .body(new ServiceResponse(INVALID_DATA.getMessage().formatted(e.getMessage())));

    }

    @ExceptionHandler(ApplicationException.class)
    public ResponseEntity<ServiceResponse> handleApplicationException(ApplicationException e) {
        return ResponseEntity.status(e.getErrorMessage().getStatus())
                .body(new ServiceResponse(e.getMessage()));
    }
}

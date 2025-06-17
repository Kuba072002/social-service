package org.example;

import jakarta.validation.ConstraintViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;

import static org.example.BasicApplicationError.INVALID_DATA;

@ControllerAdvice
public class WebExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ServiceResponse> handleValidationExceptions(MethodArgumentNotValidException e) {
        return ResponseEntity.status(INVALID_DATA.getStatus())
                .body(new ServiceResponse(INVALID_DATA.getMessage().formatted(e.getMessage())));

    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ServiceResponse> handleValidationExceptions(ConstraintViolationException e) {
        return ResponseEntity.status(INVALID_DATA.getStatus())
                .body(new ServiceResponse(INVALID_DATA.getMessage().formatted(e.getMessage())));

    }

    @ExceptionHandler(ApplicationException.class)
    public ResponseEntity<ServiceResponse> handleApplicationException(ApplicationException e) {
        return ResponseEntity.status(e.getApplicationError().getStatus())
                .body(new ServiceResponse(e.getApplicationError().getMessage()));
    }

    @ExceptionHandler(HttpClientErrorException.class)
    public ResponseEntity<ServiceResponse> handleHttpClientErrorException(HttpClientErrorException e) {
        var response = e.getResponseBodyAs(ServiceResponse.class);
        if (response == null) response = new ServiceResponse("Unknown error.");
        return ResponseEntity.status(e.getStatusCode())
                .body(response);
    }

    @ExceptionHandler(HttpServerErrorException.class)
    public ResponseEntity<ServiceResponse> handleHttpServerErrorException(HttpServerErrorException e) {
        return ResponseEntity.status(e.getStatusCode())
                .body(new ServiceResponse("Unknown server error."));
    }

}

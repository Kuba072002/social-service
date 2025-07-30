package org.example;

import io.jsonwebtoken.JwtException;
import org.springframework.boot.autoconfigure.web.WebProperties;
import org.springframework.boot.autoconfigure.web.reactive.error.AbstractErrorWebExceptionHandler;
import org.springframework.boot.web.reactive.error.ErrorAttributes;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.*;
import reactor.core.publisher.Mono;

import static org.example.GatewayApplicationError.INVALID_AUTHORIZATION;
import static org.example.GatewayApplicationError.INVALID_USER_HEADER;

public class GlobalErrorWebExceptionHandler extends AbstractErrorWebExceptionHandler {

    public GlobalErrorWebExceptionHandler(
            ErrorAttributes errorAttributes,
            WebProperties.Resources resources,
            ApplicationContext applicationContext
    ) {
        super(errorAttributes, resources, applicationContext);
    }

    @Override
    protected RouterFunction<ServerResponse> getRoutingFunction(ErrorAttributes errorAttributes) {
        return RouterFunctions.route(RequestPredicates.all(), this::renderErrorResponse);
    }

    private Mono<ServerResponse> renderErrorResponse(ServerRequest request) {
        Throwable error = getError(request);
        HttpStatus httpStatus;
        String message;
        switch (error) {
            case ApplicationException exception -> {
                httpStatus = exception.getApplicationError().getStatus();
                message = exception.getApplicationError().getMessage();
            }
            case JwtException ex -> {
                httpStatus = INVALID_AUTHORIZATION.getStatus();
                message = INVALID_AUTHORIZATION.getMessage();
            }
            case NumberFormatException ex -> {
                httpStatus = INVALID_USER_HEADER.getStatus();
                message = INVALID_USER_HEADER.getMessage();
            }
            default -> {
                httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
                message = "Unknown error";
            }
        }

        return ServerResponse.status(httpStatus)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new ServiceResponse(message));
    }
}

package org.example;

import graphql.GraphQLError;
import graphql.GraphqlErrorBuilder;
import graphql.schema.DataFetchingEnvironment;
import io.jsonwebtoken.JwtException;
import org.springframework.graphql.execution.DataFetcherExceptionResolverAdapter;
import org.springframework.graphql.execution.ErrorType;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.Map;
import java.util.Objects;

import static org.example.GatewayApplicationError.INVALID_AUTHORIZATION;
import static org.example.GatewayApplicationError.INVALID_USER_HEADER;

@Component
public class CustomExceptionResolver extends DataFetcherExceptionResolverAdapter {

    private static final Map<HttpStatus, ErrorType> statusmap = Map.of(
            HttpStatus.BAD_REQUEST, ErrorType.BAD_REQUEST,
            HttpStatus.NOT_FOUND, ErrorType.NOT_FOUND,
            HttpStatus.FORBIDDEN, ErrorType.FORBIDDEN,
            HttpStatus.INTERNAL_SERVER_ERROR, ErrorType.INTERNAL_ERROR
    );

    @Override
    protected GraphQLError resolveToSingleError(Throwable error, DataFetchingEnvironment env) {
        HttpStatus httpStatus;
        String message;
        switch (error) {
            case WebClientResponseException e -> {
                httpStatus = HttpStatus.valueOf(e.getStatusCode().value());
                message = Objects.requireNonNull(e.getResponseBodyAs(ServiceResponse.class)).message();
            }
            case IllegalStateException ex -> {
                httpStatus = HttpStatus.BAD_REQUEST;
                message = ex.getMessage();
            }
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
        return GraphqlErrorBuilder.newError()
                .errorType(statusmap.getOrDefault(httpStatus, ErrorType.NOT_FOUND))
                .message(message)
                .path(env.getExecutionStepInfo().getPath())
                .location(env.getField().getSourceLocation())
                .build();
    }
}

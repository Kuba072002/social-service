package org.example;


public class ApplicationException extends RuntimeException {
    private final ApplicationError applicationError;

    public ApplicationException(ApplicationError applicationError) {
        this.applicationError = applicationError;
    }

    public ApplicationError getApplicationError() {
        return applicationError;
    }
}

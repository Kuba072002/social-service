package org.example;


public class ApplicationException extends RuntimeException {
    private final CustomErrorMessage errorMessage;

    public ApplicationException(CustomErrorMessage errorMessage) {
        this.errorMessage = errorMessage;
    }

    public CustomErrorMessage getErrorMessage() {
        return errorMessage;
    }
}

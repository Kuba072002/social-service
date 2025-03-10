package org.example.application.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.example.comon.CustomErrorMessage;

@AllArgsConstructor
@Getter
public class ApplicationException extends RuntimeException {
    private CustomErrorMessage errorMessage;
}

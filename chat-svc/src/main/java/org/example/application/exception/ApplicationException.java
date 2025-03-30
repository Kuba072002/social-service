package org.example.application.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.example.common.CustomErrorMessage;

@AllArgsConstructor
@Getter
public class ApplicationException extends RuntimeException {
    private CustomErrorMessage errorMessage;

}

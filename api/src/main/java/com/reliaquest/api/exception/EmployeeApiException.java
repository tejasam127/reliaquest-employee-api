package com.reliaquest.api.exception;

/**
 * Exception thrown when there's an error communicating with the Mock Employee API.
 */
public class EmployeeApiException extends RuntimeException {

    public EmployeeApiException(String message) {
        super(message);
    }

    public EmployeeApiException(String message, Throwable cause) {
        super(message, cause);
    }
}

package com.reliaquest.api.exception;

/**
 * Exception thrown when an employee is not found.
 */
public class EmployeeNotFoundException extends RuntimeException {

    public EmployeeNotFoundException(String message) {
        super(message);
    }

    public EmployeeNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}

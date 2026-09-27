package org.example.hospitalmanagementsystem.exception;

public class AccessDeniedExceptionHandler extends RuntimeException {
    public AccessDeniedExceptionHandler(String message) {
        super(message);
    }
}

package org.example.hospitalmanagementsystem.exception;

public class TimeAlreadyFinishException extends RuntimeException {
    public TimeAlreadyFinishException(String message) {
        super(message);
    }
}

package com.noom.interview.fullstack.sleep.exception;

public class ValidationException extends IllegalArgumentException {
    public ValidationException(String s) {
        super(s);
    }

    public ValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}

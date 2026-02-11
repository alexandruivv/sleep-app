package com.noom.interview.fullstack.sleep.exception;

public class MissingUserIdHeaderException extends IllegalArgumentException {
    public MissingUserIdHeaderException(String message) {
        super(message);
    }

    public MissingUserIdHeaderException(String message, Throwable cause) {
        super(message, cause);
    }
}

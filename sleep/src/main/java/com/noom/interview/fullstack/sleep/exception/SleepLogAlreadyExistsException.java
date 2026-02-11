package com.noom.interview.fullstack.sleep.exception;

public class SleepLogAlreadyExistsException extends RuntimeException {

    public SleepLogAlreadyExistsException() {
        super("Sleep log for today already exists");
    }
}

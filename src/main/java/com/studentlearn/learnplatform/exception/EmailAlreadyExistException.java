package com.studentlearn.learnplatform.exception;

public class EmailAlreadyExistException extends RuntimeException {

    public EmailAlreadyExistException(String message) {
        super(message);
    }
}

package com.consumerfinance.exception;

/**
 * Exception thrown when attempting to register a consumer with duplicate email
 */
public class DuplicateEmailException extends RuntimeException {
    public DuplicateEmailException(String message) {
        super(message);
    }

    public DuplicateEmailException(String message, Throwable cause) {
        super(message, cause);
    }
}

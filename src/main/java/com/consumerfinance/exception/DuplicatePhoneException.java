package com.consumerfinance.exception;

/**
 * Exception thrown when attempting to register a consumer with duplicate phone
 */
public class DuplicatePhoneException extends RuntimeException {
    public DuplicatePhoneException(String message) {
        super(message);
    }

    public DuplicatePhoneException(String message, Throwable cause) {
        super(message, cause);
    }
}

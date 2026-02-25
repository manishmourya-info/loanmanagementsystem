package com.consumerfinance.exception;

/**
 * Exception thrown when a requested loan is not found.
 */
public class LoanNotFoundException extends RuntimeException {

    public LoanNotFoundException(String message) {
        super(message);
    }

    public LoanNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

}

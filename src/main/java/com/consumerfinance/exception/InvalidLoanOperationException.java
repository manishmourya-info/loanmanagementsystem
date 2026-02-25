package com.consumerfinance.exception;

/**
 * Exception thrown when an invalid loan operation is attempted.
 */
public class InvalidLoanOperationException extends RuntimeException {

    public InvalidLoanOperationException(String message) {
        super(message);
    }

    public InvalidLoanOperationException(String message, Throwable cause) {
        super(message, cause);
    }

}

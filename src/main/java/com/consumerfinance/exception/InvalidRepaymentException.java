package com.consumerfinance.exception;

/**
 * Exception thrown when an invalid repayment operation is attempted.
 */
public class InvalidRepaymentException extends RuntimeException {

    public InvalidRepaymentException(String message) {
        super(message);
    }

    public InvalidRepaymentException(String message, Throwable cause) {
        super(message, cause);
    }

}

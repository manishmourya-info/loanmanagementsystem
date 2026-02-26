package com.consumerfinance.exception;

/**
 * Exception thrown when consumer is not found
 */
public class ConsumerNotFoundException extends RuntimeException {
    public ConsumerNotFoundException(String message) {
        super(message);
    }

    public ConsumerNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}

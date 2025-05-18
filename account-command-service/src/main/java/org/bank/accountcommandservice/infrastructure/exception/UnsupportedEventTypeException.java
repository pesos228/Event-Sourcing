package org.bank.accountcommandservice.infrastructure.exception;

public class UnsupportedEventTypeException extends RuntimeException {
    public UnsupportedEventTypeException(String message) {
        super(message);
    }
}

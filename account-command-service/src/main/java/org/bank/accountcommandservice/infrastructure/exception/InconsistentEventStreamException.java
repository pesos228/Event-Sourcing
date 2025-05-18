package org.bank.accountcommandservice.infrastructure.exception;

public class InconsistentEventStreamException extends RuntimeException {
    public InconsistentEventStreamException(String message) {
        super(message);
    }
}

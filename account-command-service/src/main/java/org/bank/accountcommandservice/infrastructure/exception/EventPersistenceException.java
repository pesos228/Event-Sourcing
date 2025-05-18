package org.bank.accountcommandservice.infrastructure.exception;

public class EventPersistenceException extends RuntimeException {
    public EventPersistenceException(String message) {
        super(message);
    }
}

package org.bank.accountcommandservice.infrastructure.exception;

public class EventReplayException extends RuntimeException {
    public EventReplayException(String message) {
        super(message);
    }
}

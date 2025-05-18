package org.bank.accountcommandservice.domain.exception;

public class CommandNotFound extends RuntimeException {
    public CommandNotFound(String message) {
        super(message);
    }
}

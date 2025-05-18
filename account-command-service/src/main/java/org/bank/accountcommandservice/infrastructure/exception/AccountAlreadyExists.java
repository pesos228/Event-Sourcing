package org.bank.accountcommandservice.infrastructure.exception;

public class AccountAlreadyExists extends RuntimeException {
    public AccountAlreadyExists(String message) {
        super(message);
    }
}

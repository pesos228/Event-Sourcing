package org.bank.accountcommandservice.infrastructure.exception;

public class ProjectionVersionMismatchException extends RuntimeException {
    public ProjectionVersionMismatchException(String message) {
        super(message);
    }
}

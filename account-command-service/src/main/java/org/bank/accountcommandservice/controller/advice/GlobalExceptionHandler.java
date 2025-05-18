package org.bank.accountcommandservice.controller.advice;

import org.bank.accountcommandservice.domain.exception.CommandNotFound;
import org.bank.accountcommandservice.domain.exception.InvalidAccountOperationException;
import org.bank.accountcommandservice.domain.exception.NegativeAmountException;
import org.bank.accountcommandservice.domain.exception.NotEnoughMoneyException;
import org.bank.accountcommandservice.infrastructure.exception.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ResponseBody
    @ExceptionHandler(CommandNotFound.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    String commandNotFoundException(CommandNotFound commandNotFound) {
        return commandNotFound.getMessage();
    }

    @ResponseBody
    @ExceptionHandler(NotEnoughMoneyException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    String notEnoughMoneyException(NotEnoughMoneyException notEnoughMoneyException) {
        return notEnoughMoneyException.getMessage();
    }

    @ResponseBody
    @ExceptionHandler(NegativeAmountException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    String negativeAmountException(NegativeAmountException negativeAmountException) {
        return negativeAmountException.getMessage();
    }

    @ResponseBody
    @ExceptionHandler(InvalidAccountOperationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    String invalidAccountOperationException(InvalidAccountOperationException invalidAccountOperationException) {
        return invalidAccountOperationException.getMessage();
    }

    @ResponseBody
    @ExceptionHandler(EventPersistenceException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    String eventPersistenceException(EventPersistenceException eventPersistenceException) {
        return eventPersistenceException.getMessage();
    }

    @ResponseBody
    @ExceptionHandler(EventReplayException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    String eventReplayException(EventReplayException eventReplayException) {
        return eventReplayException.getMessage();
    }

    @ResponseBody
    @ExceptionHandler(InconsistentEventStreamException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    String eventStreamException(InconsistentEventStreamException inconsistentEventStreamException) {
        return inconsistentEventStreamException.getMessage();
    }

    @ResponseBody
    @ExceptionHandler(UnsupportedEventTypeException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    String eventTypeException(UnsupportedEventTypeException unsupportedEventTypeException) {
        return unsupportedEventTypeException.getMessage();
    }

    @ResponseBody
    @ExceptionHandler(ProjectionVersionMismatchException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    String projectionVersionMismatchException(ProjectionVersionMismatchException projectionVersionMismatchException) {
        return projectionVersionMismatchException.getMessage();
    }

    @ResponseBody
    @ExceptionHandler(AccountAlreadyExists.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    String accountAlreadyExists(AccountAlreadyExists accountAlreadyExists) {
        return accountAlreadyExists.getMessage();
    }

    @ResponseBody
    @ExceptionHandler(ProjectionException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    String projectionException(ProjectionException projectionException) {
        return projectionException.getMessage();
    }

    @ResponseBody
    @ExceptionHandler(AccountNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    String accountNotFoundException(AccountNotFoundException accountNotFoundException) {
        return accountNotFoundException.getMessage();
    }
}

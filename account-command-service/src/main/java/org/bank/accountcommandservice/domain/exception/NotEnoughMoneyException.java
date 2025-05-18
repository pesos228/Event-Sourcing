package org.bank.accountcommandservice.domain.exception;

public class NotEnoughMoneyException extends RuntimeException {
    public NotEnoughMoneyException(String amount, String amountWithDraw) {
        super("Not enough money to withdraw. You have: " + amount + " You want withdraw: " + amountWithDraw);
    }
}

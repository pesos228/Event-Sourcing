package org.bank.accountcommandservice.domain.model;

import org.bank.accountcommandservice.domain.exception.NegativeAmountException;
import org.bank.accountcommandservice.domain.exception.NotEnoughMoneyException;

import java.math.BigDecimal;

public record Money(
        BigDecimal amount
) {

    public Money(BigDecimal amount) {
        if (amount == null) {
            throw new NullPointerException("Amount cannot be null");
        }
        if (amount.floatValue() < 0){
            throw new NegativeAmountException("Amount must be positive");
        }
        this.amount = amount;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public Money deposit(BigDecimal amount) {
        if (amount == null) {
            throw new NullPointerException("Amount cannot be null");
        }
        if (amount.compareTo(BigDecimal.ZERO) < 0){
            throw new NegativeAmountException("Amount must be positive");
        }
        return new Money(this.amount.add(amount));
    }

    public Money withdraw(BigDecimal amount) {
        if (amount == null) {
            throw new NullPointerException("Amount cannot be null");
        }
        if (amount.compareTo(BigDecimal.ZERO) < 0){
            throw new NegativeAmountException("Amount must be positive");
        }
        if (this.amount.compareTo(amount) < 0) {
            throw new NotEnoughMoneyException(this.amount.toPlainString(), amount.toString());
        }
        return new Money(this.amount.subtract(amount));
    }
}

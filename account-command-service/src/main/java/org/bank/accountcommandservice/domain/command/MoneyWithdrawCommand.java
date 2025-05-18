package org.bank.accountcommandservice.domain.command;

import org.bank.accountcommandservice.domain.model.Money;

import java.util.UUID;

public record MoneyWithdrawCommand(
        UUID accountId,
        Money money
) {
}

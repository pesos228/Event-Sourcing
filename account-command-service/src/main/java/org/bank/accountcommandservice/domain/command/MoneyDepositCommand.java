package org.bank.accountcommandservice.domain.command;

import org.bank.accountcommandservice.domain.model.Money;

import java.util.UUID;

public record MoneyDepositCommand(
        UUID accountId,
        Money money
) {
}

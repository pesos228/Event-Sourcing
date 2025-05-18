package org.bank.accountcommandservice.domain.event;

import org.bank.accountcommandservice.domain.model.Money;

public record MoneyDepositedEvent(
        BaseEvent baseEvent,
        Money amountDeposited
) {
}

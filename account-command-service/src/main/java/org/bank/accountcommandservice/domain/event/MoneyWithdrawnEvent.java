package org.bank.accountcommandservice.domain.event;

import org.bank.accountcommandservice.domain.model.Money;

public record MoneyWithdrawnEvent(
        BaseEvent baseEvent,
        Money amountWithdrawn
) {
}

package org.bank.accountcommandservice.domain.event;

import org.bank.accountcommandservice.domain.model.Money;

public record AccountCreatedEvent(
        BaseEvent baseEvent,
        String accountHolderName,
        Money initMoney
) {
}

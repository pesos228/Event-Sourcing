package org.bank.accountcommandservice.domain.model;

import java.util.UUID;

public record AccountSnapshot(
        UUID accountId,
        String accountName,
        Money money,
        int version,
        long offset
) {
}

package org.bank.accountcommandservice.domain.command;

public record AccountCreateCommand(
        String accountHolderName
) {
}

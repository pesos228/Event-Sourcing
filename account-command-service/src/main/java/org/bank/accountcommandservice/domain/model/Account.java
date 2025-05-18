package org.bank.accountcommandservice.domain.model;

import org.bank.accountcommandservice.domain.command.AccountCreateCommand;
import org.bank.accountcommandservice.domain.command.MoneyDepositCommand;
import org.bank.accountcommandservice.domain.command.MoneyWithdrawCommand;
import org.bank.accountcommandservice.domain.event.AccountCreatedEvent;
import org.bank.accountcommandservice.domain.event.BaseEvent;
import org.bank.accountcommandservice.domain.event.MoneyDepositedEvent;
import org.bank.accountcommandservice.domain.event.MoneyWithdrawnEvent;
import org.bank.accountcommandservice.domain.exception.CommandNotFound;
import org.bank.accountcommandservice.domain.exception.InvalidAccountOperationException;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Account {
    private UUID accountId;
    private String accountName;
    private Money balance;
    private int version;
    private final List<Object> uncommittedChanges = new ArrayList<>();

    public Account(){

    }

    private Account(UUID accountId, String accountName, Money balance, int version) {
        this.accountId = accountId;
        this.accountName = accountName;
        this.balance = balance;
        this.version = version;
    }

    public Account(AccountCreateCommand command) {
        if (command == null) {
            throw new CommandNotFound("command cannot be null");
        }
        if (command.accountHolderName() == null || command.accountHolderName().isEmpty()) {
            throw new InvalidAccountOperationException("Account holder name cannot be null or empty");
        }

        var accountUuid = UUID.randomUUID();
        AccountCreatedEvent event = new AccountCreatedEvent(
                BaseEvent.newBaseEvent(accountUuid, 0),
                command.accountHolderName(),
                new Money(new BigDecimal(BigInteger.ZERO))
        );

        apply(event);
        uncommittedChanges.add(event);
    }

    public void deposit(MoneyDepositCommand command) {
        if (command == null) {
            throw new CommandNotFound("command cannot be null");
        }
        if (!command.accountId().equals(this.accountId)) {
            throw new InvalidAccountOperationException("deposit command does not belong to this account");
        }

        var event = new MoneyDepositedEvent(
                BaseEvent.newBaseEvent(accountId, version),
                command.money()
        );

        apply(event);
        uncommittedChanges.add(event);
    }

    public void withdraw(MoneyWithdrawCommand command) {
        if (command == null) {
            throw new CommandNotFound("command cannot be null");
        }
        if (!command.accountId().equals(this.accountId)) {
            throw new InvalidAccountOperationException("deposit command does not belong to this account");
        }

        var event = new MoneyWithdrawnEvent(
                BaseEvent.newBaseEvent(accountId, version),
                command.money()
        );

        apply(event);
        uncommittedChanges.add(event);
    }

    public static Account build(List<Object> history) {
        var account = new Account();
        account.replayEvents(history);
        return account;
    }

    public void replayEvents(List<Object> history) {
        for (Object event : history) {
            if (event instanceof AccountCreatedEvent e) {
                apply(e);
            }
            if (event instanceof MoneyDepositedEvent e) {
                apply(e);
            }
            if (event instanceof MoneyWithdrawnEvent e) {
                apply(e);
            }
        }
    }

    public static Account loadSnapshot(AccountSnapshot snapshot) {
        return new Account(snapshot.accountId(), snapshot.accountName(), snapshot.money(), snapshot.version());
    }

    public void apply(AccountCreatedEvent event) {
        this.accountId = event.baseEvent().accountId();
        this.accountName = event.accountHolderName();
        this.balance = event.initMoney();
        this.version = event.baseEvent().aggregateVersion() + 1;
    }

    public void apply(MoneyDepositedEvent event) {
        this.balance = this.balance.deposit(event.amountDeposited().amount());
        this.version = event.baseEvent().aggregateVersion() + 1;
    }

    public void apply(MoneyWithdrawnEvent event) {
        this.balance = this.balance.withdraw(event.amountWithdrawn().amount());
        this.version = event.baseEvent().aggregateVersion() + 1;
    }

    public UUID getAccountId() {
        return accountId;
    }

    public String getAccountName() {
        return accountName;
    }

    public Money getBalance() {
        return balance;
    }

    public int getVersion() {
        return version;
    }

    public List<Object> getUncommittedChanges() {
        return List.copyOf(uncommittedChanges);
    }

    public void markChangesAsCommitted() {
        this.uncommittedChanges.clear();
    }
}

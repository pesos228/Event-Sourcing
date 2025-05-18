package org.bank.accountcommandservice.application;


import org.bank.accountcommandservice.domain.command.AccountCreateCommand;
import org.bank.accountcommandservice.domain.command.MoneyDepositCommand;
import org.bank.accountcommandservice.domain.command.MoneyWithdrawCommand;
import org.bank.accountcommandservice.domain.model.Account;
import org.bank.accountcommandservice.domain.model.AccountSnapshot;
import org.bank.accountcommandservice.domain.repository.EventStore;
import org.bank.accountcommandservice.domain.repository.SnapshotStore;
import org.bank.accountcommandservice.infrastructure.exception.AccountNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
public class AccountApplicationService {

    private final EventStore eventStore;
    private final SnapshotStore snapshotStore;
    private final int snapshotFrequency;

    @Autowired
    public AccountApplicationService(EventStore eventStore,
                                     SnapshotStore snapshotStore,
                                     @Value("${app.snapshot.frequency}") int snapshotFrequency) {
        this.eventStore = eventStore;
        this.snapshotStore = snapshotStore;
        this.snapshotFrequency = snapshotFrequency;
    }

    @Transactional
    public void accountWithdraw(MoneyWithdrawCommand command) {
        var account = getAccount(command.accountId())
                .orElseThrow(() -> new AccountNotFoundException("Account not found for withdrawal: " + command.accountId()));

        account.withdraw(command);
        var lastOffset = eventStore.saveEvents(account.getAccountId(), account.getUncommittedChanges());
        account.markChangesAsCommitted();

        if (snapshotFrequency > 0 && lastOffset != -1L && account.getVersion() % snapshotFrequency == 0) {
            takeSnapshot(account, lastOffset);
        }
    }

    @Transactional
    public void accountDeposit(MoneyDepositCommand command) {
        var account = getAccount(command.accountId()).orElseThrow(() -> new AccountNotFoundException("Account not found for deposit" + command.accountId()));
        account.deposit(command);
        var lastOffset = eventStore.saveEvents(account.getAccountId(), account.getUncommittedChanges());
        account.markChangesAsCommitted();

        if (snapshotFrequency > 0 && lastOffset != -1L && account.getVersion() % snapshotFrequency == 0) {
            takeSnapshot(account, lastOffset);
        }
    }

    @Transactional
    public String accountCreate(AccountCreateCommand command) {
        var account = new Account(command);
        eventStore.saveEvents(account.getAccountId(), account.getUncommittedChanges());
        account.markChangesAsCommitted();

        return account.getAccountId().toString();
    }

    private Optional<Account> getAccount(UUID accountId) {
        var snapShot = snapshotStore.getLastSnapshot(accountId);
        if (snapShot.isPresent()){
            var events = eventStore.loadEventStreamAfter(snapShot.get().accountId(), snapShot.get().offset(), snapShot.get().version());
            var currentAccount = Account.loadSnapshot(snapShot.get());
            currentAccount.replayEvents(events.events());
            return Optional.of(currentAccount);
        }

        var events = eventStore.loadEventStream(accountId);
        if (events.isEmpty()){
            return Optional.empty();
        }

        return Optional.of(Account.build(events.events()));

    }

    public void takeSnapshot(Account account, long lastOffset) {
        snapshotStore.saveSnapshot(
                new AccountSnapshot(
                        account.getAccountId(),
                        account.getAccountName(),
                        account.getBalance(),
                        account.getVersion(),
                        lastOffset
                )
        );
    }

}

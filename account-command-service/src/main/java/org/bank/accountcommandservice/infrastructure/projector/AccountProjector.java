package org.bank.accountcommandservice.infrastructure.projector;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.bank.accountcommandservice.domain.event.AccountCreatedEvent;
import org.bank.accountcommandservice.domain.event.BaseEvent;
import org.bank.accountcommandservice.domain.event.MoneyDepositedEvent;
import org.bank.accountcommandservice.domain.event.MoneyWithdrawnEvent;
import org.bank.accountcommandservice.domain.model.Account;
import org.bank.accountcommandservice.domain.repository.EventStore;
import org.bank.accountcommandservice.domain.repository.SnapshotStore;
import org.bank.accountcommandservice.infrastructure.exception.AccountAlreadyExists;
import org.bank.accountcommandservice.infrastructure.exception.ProjectionException;
import org.bank.accountcommandservice.infrastructure.exception.ProjectionVersionMismatchException;
import org.bank.accountcommandservice.infrastructure.exception.UnsupportedEventTypeException;
import org.bank.accountcommandservice.infrastructure.persistence.entity.AccountEntity;
import org.bank.accountcommandservice.infrastructure.persistence.repository.AccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
public class AccountProjector {

    private final AccountRepository accountRepository;
    private final EventStore eventStore;
    private final SnapshotStore snapshotStore;

    @Autowired
    public AccountProjector(AccountRepository accountRepository, EventStore eventStore, SnapshotStore snapshotStore) {
        this.accountRepository = accountRepository;
        this.eventStore = eventStore;
        this.snapshotStore = snapshotStore;
    }

    @KafkaListener(topics = "${app.kafka.topic-name}", groupId = "account-projector-strict-group")
    @Transactional
    public void handleEvent(ConsumerRecord<String, Object> record) {

        Object event = record.value();
        String key = record.key();
        long offset = record.offset();

        if (event == null) {
            return;
        }

        var baseEvent = getBaseEvent(event).orElseThrow(() -> new UnsupportedEventTypeException(String.format("Unsupported event type: %s. Cannot extract BaseEvent. Key: %s, Offset: %d",
                event.getClass().getName(), key, offset)));

        var accountId = baseEvent.accountId();
        int eventVersion = baseEvent.aggregateVersion();


        try {
            processEvent(event, accountId, eventVersion, offset);
        }
        catch (ProjectionVersionMismatchException | UnsupportedEventTypeException | AccountAlreadyExists e){
            throw e;
        }
        catch (Exception e) {
            throw new ProjectionException(String.format("Unexpected error processing event for accountId %s. Event: %s, Offset: %d. Exception: %s",
                    accountId, event.getClass().getSimpleName(), offset, e.getMessage()));
        }
    }

    private void processEvent(Object event, UUID accountId, int eventVersion, long offset) {
        var account = restoreAccountState(accountId, eventVersion);

        if (account.getVersion() != eventVersion) {
            throw new ProjectionVersionMismatchException(String.format("Version mismatch for accountId: %s. Expected: %d, got: %d. Event: %s, offset: %d",
                    accountId, eventVersion, account.getVersion(), event.getClass().getSimpleName(), offset));
        }

        applyEvent(account, event);

        saveProjection(account, event.getClass().getSimpleName(), offset);
    }

    private Account restoreAccountState(UUID accountId, int targetVersion) {
        if (targetVersion == 0) {
            return new Account();
        }
        var history = eventStore.loadEventStreamUpToVersion(accountId, targetVersion);
        return Account.build(history.events());
    }

    private void applyEvent(Account account, Object event) {
        switch (event) {
            case AccountCreatedEvent e -> account.apply(e);
            case MoneyDepositedEvent e -> account.apply(e);
            case MoneyWithdrawnEvent e -> account.apply(e);
            default -> throw new UnsupportedEventTypeException("Unknown event type: " + event.getClass().getName());
        }
    }

    private void saveProjection(Account account, String eventType, long offset) {
        var accountId = account.getAccountId();

        Optional<AccountEntity> existingEntityOpt = accountRepository.findById(accountId);
        if (existingEntityOpt.isPresent()) {
            AccountEntity existing = existingEntityOpt.get();
            if (existing.getVersion() >= account.getVersion()) {
                return;
            }
            updateEntity(existing, account);
        } else {
            if (account.getVersion() != 1) {
                throw new AccountAlreadyExists(String.format("No projection found for accountId: %s with version: %d. Expected version 1. Event: %s, offset: %d",
                        accountId, account.getVersion(), eventType, offset));

            }
            createEntity(account);
        }
    }

    private void updateEntity(AccountEntity entity, Account account) {
        entity.setAccountName(account.getAccountName());
        entity.setBalance(account.getBalance().getAmount());
        entity.setVersion(account.getVersion());
        accountRepository.save(entity);
    }

    private void createEntity(Account account) {
        AccountEntity entity = new AccountEntity(
                account.getAccountId(),
                account.getAccountName(),
                account.getBalance().getAmount(),
                account.getVersion()
        );
        accountRepository.save(entity);
    }

    private Optional<BaseEvent> getBaseEvent(Object event) {
        return switch (event) {
            case AccountCreatedEvent e -> Optional.of(e.baseEvent());
            case MoneyDepositedEvent e -> Optional.of(e.baseEvent());
            case MoneyWithdrawnEvent e -> Optional.of(e.baseEvent());
            default -> Optional.empty();
        };
    }
}
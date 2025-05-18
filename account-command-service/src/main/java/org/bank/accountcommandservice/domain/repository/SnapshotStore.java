package org.bank.accountcommandservice.domain.repository;

import org.bank.accountcommandservice.domain.model.AccountSnapshot;

import java.util.Optional;
import java.util.UUID;

public interface SnapshotStore {
    void saveSnapshot(AccountSnapshot snapshot);
    Optional<AccountSnapshot> getLastSnapshot(UUID accountId);
}

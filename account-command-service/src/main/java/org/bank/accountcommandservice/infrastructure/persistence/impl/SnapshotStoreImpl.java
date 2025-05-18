package org.bank.accountcommandservice.infrastructure.persistence.impl;



import org.bank.accountcommandservice.domain.model.AccountSnapshot;
import org.bank.accountcommandservice.domain.model.Money;
import org.bank.accountcommandservice.domain.repository.SnapshotStore;
import org.bank.accountcommandservice.infrastructure.persistence.entity.SnapshotEntity;
import org.bank.accountcommandservice.infrastructure.persistence.repository.SnapshotRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class SnapshotStoreImpl implements SnapshotStore {

    private final SnapshotRepository snapshotRepository;

    public SnapshotStoreImpl(SnapshotRepository snapshotRepository) {
        this.snapshotRepository = snapshotRepository;
    }

    @Override
    public void saveSnapshot(AccountSnapshot snapshot) {
        SnapshotEntity entity = new SnapshotEntity(
                snapshot.accountId(),
                snapshot.accountName(),
                snapshot.money().getAmount(),
                snapshot.version(),
                snapshot.offset()
        );
        snapshotRepository.save(entity);
    }

    @Override
    public Optional<AccountSnapshot> getLastSnapshot(UUID accountId) {
        return snapshotRepository.findTopByAccountIdOrderByVersionDesc(accountId)
                .map(entity -> new AccountSnapshot(
                        entity.getAccountId(),
                        entity.getAccountName(),
                        new Money(entity.getBalance()),
                        entity.getVersion(),
                        entity.getOffset()
                ));
    }
}

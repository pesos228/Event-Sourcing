package org.bank.accountcommandservice.infrastructure.persistence.repository;

import org.bank.accountcommandservice.infrastructure.persistence.entity.SnapshotEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface SnapshotRepository extends JpaRepository<SnapshotEntity, UUID> {
    Optional<SnapshotEntity> findTopByAccountIdOrderByVersionDesc(UUID accountId);
}

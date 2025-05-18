package org.bank.accountcommandservice.infrastructure.persistence.repository;

import org.bank.accountcommandservice.infrastructure.persistence.entity.AccountEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface AccountRepository extends JpaRepository<AccountEntity, UUID> {
}

package org.bank.accountcommandservice.infrastructure.persistence.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "account_snapshots")
public class SnapshotEntity {
    private UUID id;
    private UUID accountId;
    private String accountName;
    private BigDecimal balance;
    private int version;
    private long kafkaEventOffset;

    protected SnapshotEntity() {

    }

    public SnapshotEntity(UUID accountId, String accountName, BigDecimal balance, int version, long kafkaEventOffset) {
        this.accountId = accountId;
        this.accountName = accountName;
        this.balance = balance;
        this.version = version;
        this.kafkaEventOffset = kafkaEventOffset;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, updatable = false)
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }


    @Column(name = "account_id", nullable = false)
    public UUID getAccountId() {
        return accountId;
    }

    public void setAccountId(UUID accountId) {
        this.accountId = accountId;
    }

    @Column(name = "name", nullable = false)
    public String getAccountName() {
        return accountName;
    }

    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }


    @Column(name = "balance", nullable = false)
    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    @Column(name = "version", nullable = false)
    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    @Column(name = "kafka_event_offset", nullable = false)
    public long getOffset() {
        return kafkaEventOffset;
    }

    public void setOffset(long kafkaEventOffset) {
        this.kafkaEventOffset = kafkaEventOffset;
    }
}

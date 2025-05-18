package org.bank.accountcommandservice.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "accounts")
public class AccountEntity {
    private UUID accountId;
    private String accountName;
    private BigDecimal balance;
    private int version;

    public AccountEntity() {

    }

    public AccountEntity(UUID accountId, String accountName, BigDecimal balance, int version) {
        this.accountId = accountId;
        this.accountName = accountName;
        this.balance = balance;
        this.version = version;
    }

    @Id
    @Column(name = "id", nullable = false)
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

    @Column(name = "version",nullable = false)
    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }
}

package com.bank.transfers;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Represents a bank account.
 */
public class Account {

    public enum AccountType {
        STANDARD_SAVINGS,
        PREMIUM_SAVINGS,
        CURRENT
    }

    private String accountId;
    private String customerId;
    private AccountType accountType;
    private BigDecimal balance;
    private BigDecimal averageMonthlyBalance;
    private LocalDateTime createdAt;

    public Account() {}

    public Account(String accountId, String customerId, AccountType accountType,
                   BigDecimal balance, BigDecimal averageMonthlyBalance) {
        this.accountId = accountId;
        this.customerId = customerId;
        this.accountType = accountType;
        this.balance = balance;
        this.averageMonthlyBalance = averageMonthlyBalance;
        this.createdAt = LocalDateTime.now();
    }

    // -------------------------------------------------------------------------
    // Getters and setters
    // -------------------------------------------------------------------------

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public AccountType getAccountType() {
        return accountType;
    }

    public void setAccountType(AccountType accountType) {
        this.accountType = accountType;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    public BigDecimal getAverageMonthlyBalance() {
        return averageMonthlyBalance;
    }

    public void setAverageMonthlyBalance(BigDecimal averageMonthlyBalance) {
        this.averageMonthlyBalance = averageMonthlyBalance;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "Account{accountId='" + accountId + "', type=" + accountType
                + ", balance=" + balance + "}";
    }
}

package com.bank.account;

import java.util.Date;
import java.util.Objects;

/**
 * Represents a bank account held by a customer.
 */
public class Account {

    private String accountNumber;
    private String customerId;
    private AccountType accountType;
    private double balance;
    private Date openedDate;
    private Date maturityDate;
    private boolean active;
    private String branchCode;

    // Default constructor required by some frameworks
    public Account() {
    }

    public Account(String accountNumber,
                   String customerId,
                   AccountType accountType,
                   double balance,
                   Date openedDate,
                   Date maturityDate,
                   boolean active,
                   String branchCode) {
        this.accountNumber = accountNumber;
        this.customerId = customerId;
        this.accountType = accountType;
        this.balance = balance;
        this.openedDate = openedDate;
        this.maturityDate = maturityDate;
        this.active = active;
        this.branchCode = branchCode;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
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

    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }

    public Date getOpenedDate() {
        return openedDate;
    }

    public void setOpenedDate(Date openedDate) {
        this.openedDate = openedDate;
    }

    public Date getMaturityDate() {
        return maturityDate;
    }

    public void setMaturityDate(Date maturityDate) {
        this.maturityDate = maturityDate;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getBranchCode() {
        return branchCode;
    }

    public void setBranchCode(String branchCode) {
        this.branchCode = branchCode;
    }

    // -------------------------------------------------------------------------
    // equals / hashCode / toString
    // -------------------------------------------------------------------------
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Account account = (Account) o;
        return Double.compare(account.balance, balance) == 0
                && active == account.active
                && Objects.equals(accountNumber, account.accountNumber)
                && Objects.equals(customerId, account.customerId)
                && accountType == account.accountType
                && Objects.equals(openedDate, account.openedDate)
                && Objects.equals(maturityDate, account.maturityDate)
                && Objects.equals(branchCode, account.branchCode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(accountNumber, customerId, accountType,
                balance, openedDate, maturityDate, active, branchCode);
    }

    @Override
    public String toString() {
        return "Account{" +
                "accountNumber='" + accountNumber + '\'' +
                ", customerId='" + customerId + '\'' +
                ", accountType=" + accountType +
                ", balance=" + balance +
                ", openedDate=" + openedDate +
                ", maturityDate=" + maturityDate +
                ", active=" + active +
                ", branchCode='" + branchCode + '\'' +
                '}';
    }
}

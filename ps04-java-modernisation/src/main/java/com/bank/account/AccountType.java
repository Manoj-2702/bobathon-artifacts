package com.bank.account;

/**
 * Enumerates the types of bank account supported by the system.
 */
public enum AccountType {

    SAVINGS,
    CURRENT,
    FIXED_DEPOSIT,
    RECURRING_DEPOSIT,
    LOAN;

    /**
     * Returns the minimum opening and maintenance balance for the account type.
     */
    public double getMinimumBalance() {
        switch (this) {
            case SAVINGS:
                return 1000.00;
            case CURRENT:
                return 5000.00;
            case FIXED_DEPOSIT:
                return 10000.00;
            case RECURRING_DEPOSIT:
                return 500.00;
            case LOAN:
                return 0.00;
            default:
                throw new IllegalArgumentException("Unknown account type: " + this);
        }
    }

    /**
     * Returns a human-readable description of the account type.
     */
    public String getDescription() {
        String description;
        switch (this) {
            case SAVINGS:
                description = "Savings Account - earns interest, limited withdrawals";
                break;
            case CURRENT:
                description = "Current Account - no interest, unlimited transactions";
                break;
            case FIXED_DEPOSIT:
                description = "Fixed Deposit - locked-in term with guaranteed return";
                break;
            case RECURRING_DEPOSIT:
                description = "Recurring Deposit - monthly instalments, fixed tenure";
                break;
            case LOAN:
                description = "Loan Account - credit facility with repayment schedule";
                break;
            default:
                description = "Unknown account type";
        }
        return description;
    }

    /**
     * Returns true if this account type accrues interest for the customer.
     */
    public boolean isInterestBearing() {
        switch (this) {
            case SAVINGS:
            case FIXED_DEPOSIT:
            case RECURRING_DEPOSIT:
                return true;
            case CURRENT:
            case LOAN:
                return false;
            default:
                return false;
        }
    }
}

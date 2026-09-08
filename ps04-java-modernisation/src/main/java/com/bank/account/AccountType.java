package com.bank.account;

/**
 * Account type enumeration.
 *
 * Java 8 style: plain enum with a switch statement that uses string concatenation
 * and lacks exhaustiveness guarantees. A modern Java 21 refactor would introduce
 * sealed classes or at minimum an enhanced switch expression with pattern matching
 * to make the type hierarchy exhaustive and expressive.
 */
public enum AccountType {

    SAVINGS,
    CURRENT,
    FIXED_DEPOSIT,
    RECURRING_DEPOSIT,
    LOAN;

    /**
     * Returns the minimum balance required for the given account type.
     * Old-style switch statement — no exhaustiveness check, falls through to default.
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
     * Returns a human-readable description.
     * Uses old-style switch with String concatenation instead of a switch expression.
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

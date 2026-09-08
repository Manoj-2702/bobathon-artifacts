package com.bank.transfers;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

/**
 * Calculates and applies interest to savings accounts.
 *
 * <p>Business rules implemented (Requirement 2):
 * <ul>
 *   <li>Accounts with balance &gt; £10,000 receive <strong>daily compound interest</strong>.</li>
 *   <li>Accounts with balance ≤ £10,000 receive <strong>simple monthly interest</strong>.</li>
 *   <li>Annual rate is <strong>2.5%</strong> for {@code STANDARD_SAVINGS} accounts.</li>
 *   <li>Annual rate is <strong>3.8%</strong> for {@code PREMIUM_SAVINGS} accounts.</li>
 * </ul>
 */
@Service
public class InterestCalculationService {

    /** Balance threshold above which daily compounding applies. */
    static final BigDecimal COMPOUND_THRESHOLD = new BigDecimal("10000.00");

    /** Annual rate for standard savings (2.5 %). */
    static final BigDecimal STANDARD_ANNUAL_RATE = new BigDecimal("0.025");

    /** Annual rate for premium savings (3.8 %). */
    static final BigDecimal PREMIUM_ANNUAL_RATE  = new BigDecimal("0.038");

    private static final int    DAYS_IN_YEAR   = 365;
    private static final int    MONTHS_IN_YEAR = 12;
    private static final MathContext MATH_CTX   = new MathContext(10, RoundingMode.HALF_UP);

    // -------------------------------------------------------------------------
    // Public API
    // -------------------------------------------------------------------------

    /**
     * Calculates one day's compound interest for the given account.
     *
     * <p>Formula: {@code balance × ((1 + r/365) − 1)}, where {@code r} is the
     * applicable annual rate.
     *
     * @param account a savings account with balance &gt; £10,000
     * @return the interest amount for one day, rounded to 2 decimal places
     * @throws IllegalArgumentException if the account type does not support compound interest
     *         or the balance is at or below the threshold
     */
    public BigDecimal calculateDailyCompoundInterest(Account account) {
        BigDecimal rate = resolveAnnualRate(account);

        if (account.getBalance().compareTo(COMPOUND_THRESHOLD) <= 0) {
            throw new IllegalArgumentException(
                    "Daily compound interest only applies to accounts with balance above £"
                            + COMPOUND_THRESHOLD + ". Current balance: £" + account.getBalance());
        }

        // daily factor = (1 + r/365) − 1  ≡  r/365  for a single day increment
        BigDecimal dailyRate = rate.divide(BigDecimal.valueOf(DAYS_IN_YEAR), MATH_CTX);
        return account.getBalance()
                .multiply(dailyRate, MATH_CTX)
                .setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Calculates one month's simple interest for the given account.
     *
     * <p>Formula: {@code balance × r / 12}, where {@code r} is the applicable annual rate.
     *
     * @param account a savings account with balance ≤ £10,000
     * @return the interest amount for one month, rounded to 2 decimal places
     * @throws IllegalArgumentException if the balance exceeds the threshold
     */
    public BigDecimal calculateMonthlySimpleInterest(Account account) {
        BigDecimal rate = resolveAnnualRate(account);

        if (account.getBalance().compareTo(COMPOUND_THRESHOLD) > 0) {
            throw new IllegalArgumentException(
                    "Simple monthly interest only applies to accounts with balance at or below £"
                            + COMPOUND_THRESHOLD + ". Current balance: £" + account.getBalance());
        }

        BigDecimal monthlyRate = rate.divide(BigDecimal.valueOf(MONTHS_IN_YEAR), MATH_CTX);
        return account.getBalance()
                .multiply(monthlyRate, MATH_CTX)
                .setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Applies the appropriate interest to the account balance in-place.
     *
     * <p>The correct calculation method (daily compound or simple monthly) is chosen
     * automatically based on the account's current balance and the threshold rule.
     *
     * @param account the account to update
     */
    public void applyInterest(Account account) {
        BigDecimal interest;
        if (account.getBalance().compareTo(COMPOUND_THRESHOLD) > 0) {
            interest = calculateDailyCompoundInterest(account);
        } else {
            interest = calculateMonthlySimpleInterest(account);
        }
        account.setBalance(account.getBalance().add(interest));
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    private BigDecimal resolveAnnualRate(Account account) {
        switch (account.getAccountType()) {
            case STANDARD_SAVINGS:
                return STANDARD_ANNUAL_RATE;
            case PREMIUM_SAVINGS:
                return PREMIUM_ANNUAL_RATE;
            default:
                throw new IllegalArgumentException(
                        "Interest calculation is not applicable to account type: "
                                + account.getAccountType());
        }
    }
}

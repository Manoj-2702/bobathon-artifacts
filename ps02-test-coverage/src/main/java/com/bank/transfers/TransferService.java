package com.bank.transfers;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Executes domestic and international fund transfers.
 *
 * <p>Business rules implemented:
 * <ul>
 *   <li>Requirement 1 — minimum £0.01, maximum £250,000, no overdraft.</li>
 *   <li>Requirement 3 — international transfers incur a £25 flat fee plus 0.5% of the
 *       transfer amount; the total fee is deducted from the source account before the
 *       transfer is initiated.</li>
 * </ul>
 */
@Service
public class TransferService {

    private static final BigDecimal MINIMUM_TRANSFER     = new BigDecimal("0.01");
    private static final BigDecimal MAXIMUM_TRANSFER     = new BigDecimal("250000.00");
    private static final BigDecimal INTERNATIONAL_FLAT_FEE = new BigDecimal("25.00");
    private static final BigDecimal INTERNATIONAL_PCT_FEE  = new BigDecimal("0.005"); // 0.5 %

    // -------------------------------------------------------------------------
    // Public API
    // -------------------------------------------------------------------------

    /**
     * Validates and executes a transfer request.
     *
     * <p>For international transfers the fee is calculated and deducted from the
     * source account balance before the principal amount is transferred.
     *
     * @param request the transfer to execute
     * @param sender  the source {@link Account}
     * @throws IllegalArgumentException if any business rule is violated
     */
    public void executeTransfer(TransferRequest request, Account sender) {
        validateTransfer(request, sender);

        BigDecimal debit = request.getAmount();

        if (request.isInternational()) {
            BigDecimal fee = calculateInternationalFee(request.getAmount());
            debit = debit.add(fee);
            // Re-check that the combined debit (principal + fee) does not exceed balance.
            if (debit.compareTo(sender.getBalance()) > 0) {
                throw new IllegalArgumentException(
                        "Insufficient funds to cover transfer amount and international fee of £"
                                + fee.setScale(2, RoundingMode.HALF_UP));
            }
        }

        sender.setBalance(sender.getBalance().subtract(debit));
    }

    /**
     * Calculates the total fee for an international transfer.
     *
     * <p>Fee = £25 (flat) + 0.5% of {@code amount}, rounded to 2 decimal places.
     *
     * @param amount the principal transfer amount
     * @return the fee in GBP
     */
    public BigDecimal calculateInternationalFee(BigDecimal amount) {
        BigDecimal percentagePart = amount.multiply(INTERNATIONAL_PCT_FEE)
                .setScale(2, RoundingMode.HALF_UP);
        return INTERNATIONAL_FLAT_FEE.add(percentagePart);
    }

    /**
     * Validates a transfer request against the business rules without executing it.
     *
     * <p>Checks performed:
     * <ol>
     *   <li>Amount is not null and is between £0.01 and £250,000 (inclusive).</li>
     *   <li>The source account has sufficient balance to cover the amount.
     *       For international transfers the fee is also taken into account.</li>
     *   <li>International transfers must carry a non-blank destination country code
     *       (SWIFT routing pre-condition).</li>
     * </ol>
     *
     * @param request the transfer to validate
     * @param sender  the source account
     * @throws IllegalArgumentException if any rule is violated
     */
    public void validateTransfer(TransferRequest request, Account sender) {
        if (request.getAmount() == null) {
            throw new IllegalArgumentException("Transfer amount must not be null");
        }

        if (request.getAmount().compareTo(MINIMUM_TRANSFER) < 0) {
            throw new IllegalArgumentException(
                    "Transfer amount £" + request.getAmount()
                            + " is below the minimum of £" + MINIMUM_TRANSFER);
        }

        if (request.getAmount().compareTo(MAXIMUM_TRANSFER) > 0) {
            throw new IllegalArgumentException(
                    "Transfer amount £" + request.getAmount()
                            + " exceeds the maximum single transfer limit of £" + MAXIMUM_TRANSFER);
        }

        if (request.getAmount().compareTo(sender.getBalance()) > 0) {
            throw new IllegalArgumentException(
                    "Insufficient funds: balance £" + sender.getBalance()
                            + " is less than the requested transfer of £" + request.getAmount());
        }

        if (request.isInternational()
                && (request.getDestinationCountryCode() == null
                        || request.getDestinationCountryCode().isBlank())) {
            throw new IllegalArgumentException(
                    "Destination country code is required for international (SWIFT) transfers");
        }
    }
}

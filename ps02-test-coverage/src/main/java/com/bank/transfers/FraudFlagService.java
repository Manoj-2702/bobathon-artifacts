package com.bank.transfers;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

/**
 * Evaluates whether a transfer should be flagged for manual fraud review.
 *
 * <p>Business rules implemented (Requirement 4):
 * <ul>
 *   <li>Flag if the destination account has received <strong>more than 3</strong> inbound
 *       transfers in the past 24 hours.</li>
 *   <li>Flag if the transfer amount exceeds <strong>80%</strong> of the sender's average
 *       monthly balance.</li>
 * </ul>
 */
@Service
public class FraudFlagService {

    /** Transfers in the last 24 h that trigger flagging (exclusive threshold). */
    static final int INBOUND_TRANSFER_LIMIT = 3;

    /** Fraction of average monthly balance above which a transfer is flagged. */
    static final BigDecimal MONTHLY_BALANCE_THRESHOLD_PCT = new BigDecimal("0.80");

    /**
     * In-memory store of inbound transfer timestamps keyed by destination account ID.
     * In a real system this would be backed by a database or cache.
     */
    private final Map<String, List<LocalDateTime>> inboundTransferLog = new ConcurrentHashMap<>();

    // -------------------------------------------------------------------------
    // Public API
    // -------------------------------------------------------------------------

    /**
     * Determines whether the given transfer should be flagged for manual review.
     *
     * @param request the transfer being evaluated
     * @param sender  the source account
     * @return {@code true} if either fraud-flag rule is triggered
     */
    public boolean shouldFlagForReview(TransferRequest request, Account sender) {
        boolean highFrequencyDestination =
                getRecentInboundTransferCount(request.getDestinationAccountId(), 24)
                        > INBOUND_TRANSFER_LIMIT;

        boolean exceedsMonthlyBalanceProportion =
                exceedsAverageMonthlyBalanceThreshold(request.getAmount(), sender);

        return highFrequencyDestination || exceedsMonthlyBalanceProportion;
    }

    /**
     * Returns the number of inbound transfers recorded for {@code accountId} within
     * the last {@code hours} hours.
     *
     * @param accountId the destination account to query
     * @param hours     look-back window in hours (e.g. {@code 24})
     * @return count of inbound transfers in the window
     */
    public int getRecentInboundTransferCount(String accountId, int hours) {
        List<LocalDateTime> timestamps = inboundTransferLog.getOrDefault(accountId, List.of());
        LocalDateTime cutoff = LocalDateTime.now().minusHours(hours);
        return (int) timestamps.stream()
                .filter(ts -> ts.isAfter(cutoff))
                .count();
    }

    /**
     * Records an inbound transfer arrival time for {@code destinationAccountId}.
     *
     * <p>This must be called after a transfer has been successfully executed so that
     * the frequency check in {@link #shouldFlagForReview} has accurate data.
     *
     * @param destinationAccountId the account that received the transfer
     * @param arrivedAt            the timestamp of the transfer
     */
    public void recordInboundTransfer(String destinationAccountId, LocalDateTime arrivedAt) {
        inboundTransferLog
                .computeIfAbsent(destinationAccountId, k -> new CopyOnWriteArrayList<>())
                .add(arrivedAt);
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    private boolean exceedsAverageMonthlyBalanceThreshold(BigDecimal amount, Account sender) {
        if (sender.getAverageMonthlyBalance() == null
                || sender.getAverageMonthlyBalance().compareTo(BigDecimal.ZERO) <= 0) {
            // No average balance data — cannot apply this rule; default to safe (no flag).
            return false;
        }
        BigDecimal threshold = sender.getAverageMonthlyBalance()
                .multiply(MONTHLY_BALANCE_THRESHOLD_PCT)
                .setScale(2, RoundingMode.HALF_UP);
        return amount.compareTo(threshold) > 0;
    }
}

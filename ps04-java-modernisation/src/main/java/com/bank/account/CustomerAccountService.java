package com.bank.account;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Core service for managing customer bank accounts.
 *
 * This class is written in idiomatic Java 8 and intentionally uses a wide range
 * of patterns that have better replacements in modern Java.  Participants are
 * asked to identify each legacy pattern and refactor it using Java 21 features.
 *
 * ============================================================
 * LEGACY PATTERNS PRESENT — modernisation targets
 * ============================================================
 *
 *  1. java.util.Date / Calendar  → java.time.LocalDate / Period / DateTimeFormatter
 *  2. Raw types (List, ArrayList) → Generics / var
 *  3. Anonymous Comparator inner class → lambda / Comparator.comparing(...)
 *  4. Manual null checks          → Optional<T>
 *  5. new Thread(new Runnable(){}) → Virtual Threads (Thread.ofVirtual())
 *  6. Verbose try-catch-finally   → try-with-resources
 *  7. StringBuffer                → StringBuilder
 *  8. Enumeration (Hashtable)     → Map.forEach / entrySet stream
 *  9. instanceof + explicit cast  → Pattern matching instanceof (Java 16+)
 * 10. AccountResult wrapper class → Sealed interface + record variants
 * ============================================================
 */
public class CustomerAccountService {

    private static final Logger LOGGER = Logger.getLogger(CustomerAccountService.class.getName());

    private final NotificationService notificationService;

    // Legacy Hashtable (synchronised, old API) — holds active sessions keyed by customer ID
    private final Hashtable customerSessions = new Hashtable();  // raw type intentional

    public CustomerAccountService(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    // -----------------------------------------------------------------------
    // 1. java.util.Date + java.util.Calendar for date logic
    // -----------------------------------------------------------------------

    /**
     * Calculates the maturity date for a Fixed Deposit account.
     *
     * Uses {@code java.util.Calendar} — verbose and error-prone.
     * Modernise with {@code java.time.LocalDate.plusMonths(termMonths)}.
     */
    public Date calculateMaturityDate(Date openingDate, int termMonths) {
        if (openingDate == null) {
            throw new IllegalArgumentException("Opening date must not be null");
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(openingDate);
        calendar.add(Calendar.MONTH, termMonths);
        // Zero out time components to get a pure date — Calendar makes this tedious
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }

    /**
     * Returns the number of days since the account was opened.
     *
     * Manual millisecond arithmetic — {@code java.time.temporal.ChronoUnit.DAYS.between()}
     * is cleaner and avoids the DST edge cases present here.
     */
    public long getDaysSinceOpening(Date openedDate) {
        if (openedDate == null) {
            return 0L;
        }
        long diffMillis = new Date().getTime() - openedDate.getTime();
        return diffMillis / (1000L * 60 * 60 * 24);
    }

    // -----------------------------------------------------------------------
    // 2. Raw types
    // -----------------------------------------------------------------------

    /**
     * Returns all accounts for a given customer.
     *
     * Raw {@code List} and {@code ArrayList} — no type parameter.
     * Modernise with {@code List<Account>} and let the compiler enforce type safety.
     */
    public List getAccountsForCustomer(String customerId) {  // raw List — intentional
        List accounts = new ArrayList();                      // raw ArrayList — intentional
        // Simulated data load; in production this would query a repository
        accounts.add(new Account("ACC001", customerId, AccountType.SAVINGS,
                15000.00, new Date(), null, true, "BR001"));
        accounts.add(new Account("ACC002", customerId, AccountType.FIXED_DEPOSIT,
                50000.00, new Date(), calculateMaturityDate(new Date(), 12), true, "BR001"));
        return accounts;
    }

    // -----------------------------------------------------------------------
    // 3. Anonymous Comparator inner class instead of lambda
    // -----------------------------------------------------------------------

    /**
     * Sorts a list of accounts by balance in descending order.
     *
     * Uses an anonymous {@code Comparator} inner class.
     * Modernise with: {@code accounts.sort(Comparator.comparingDouble(Account::getBalance).reversed())}
     */
    public List getSortedAccountsByBalance(String customerId) {
        List accounts = getAccountsForCustomer(customerId);  // raw type flows through

        Collections.sort(accounts, new Comparator<Account>() {
            @Override
            public int compare(Account a1, Account a2) {
                // Descending order
                if (a2.getBalance() > a1.getBalance()) return 1;
                if (a2.getBalance() < a1.getBalance()) return -1;
                return 0;
            }
        });

        return accounts;
    }

    // -----------------------------------------------------------------------
    // 4. Manual null checks instead of Optional
    // -----------------------------------------------------------------------

    /**
     * Finds a single account by account number.
     *
     * Returns {@code null} when not found — callers must null-check.
     * Modernise with {@code Optional<Account>} as the return type.
     */
    public Account findAccountByNumber(String accountNumber, String customerId) {
        if (accountNumber == null) {
            return null;
        }
        if (customerId == null) {
            return null;
        }
        List accounts = getAccountsForCustomer(customerId);
        for (int i = 0; i < accounts.size(); i++) {
            Account account = (Account) accounts.get(i);  // cast needed because raw List
            if (accountNumber.equals(account.getAccountNumber())) {
                return account;
            }
        }
        return null;  // explicit null — callers must guard
    }

    /**
     * Gets the branch code for an account, with a manual null-check fallback.
     *
     * Modernise with: {@code Optional.ofNullable(account).map(Account::getBranchCode).orElse("UNKNOWN")}
     */
    public String getBranchCodeSafely(Account account) {
        if (account == null) {
            return "UNKNOWN";
        }
        String branchCode = account.getBranchCode();
        if (branchCode == null) {
            return "UNKNOWN";
        }
        if (branchCode.trim().isEmpty()) {
            return "UNKNOWN";
        }
        return branchCode;
    }

    // -----------------------------------------------------------------------
    // 5. new Thread(new Runnable() { ... }) for async work
    // -----------------------------------------------------------------------

    /**
     * Opens a new account and asynchronously notifies the customer.
     *
     * Notification dispatch uses a raw platform thread.
     * Modernise with {@code Thread.ofVirtual().start(() -> notificationService.sendWelcomeNotification(...))}
     * or a virtual-thread executor.
     */
    public AccountResult<Account> openAccount(String customerId,
                                               String email,
                                               AccountType type,
                                               double initialDeposit) {
        // Manual pre-condition checks — could use Objects.requireNonNull
        if (customerId == null || customerId.trim().isEmpty()) {
            return AccountResult.failure("INVALID_CUSTOMER", "Customer ID must not be blank");
        }
        if (initialDeposit < type.getMinimumBalance()) {
            return AccountResult.failure("INSUFFICIENT_DEPOSIT",
                    "Initial deposit " + initialDeposit
                            + " is below minimum balance " + type.getMinimumBalance()
                            + " for " + type.name());
        }

        String accountNumber = generateAccountNumber(customerId, type);
        Date openedDate = new Date();
        Date maturityDate = null;
        if (type == AccountType.FIXED_DEPOSIT) {
            maturityDate = calculateMaturityDate(openedDate, 12);
        } else if (type == AccountType.RECURRING_DEPOSIT) {
            maturityDate = calculateMaturityDate(openedDate, 60);
        }

        Account account = new Account(accountNumber, customerId, type,
                initialDeposit, openedDate, maturityDate, true, "BR001");

        // Async notification via anonymous Runnable + raw platform thread
        final String finalEmail = email;
        final String finalCustomerId = customerId;
        new Thread(new Runnable() {
            @Override
            public void run() {
                notificationService.sendWelcomeNotification(finalCustomerId, finalEmail);
            }
        }).start();

        return AccountResult.success(account);
    }

    // -----------------------------------------------------------------------
    // 6. Verbose try-catch-finally instead of try-with-resources
    // -----------------------------------------------------------------------

    /**
     * Loads account details from a database connection.
     *
     * Manual resource cleanup in a finally block.
     * Modernise with try-with-resources: {@code try (PreparedStatement ps = conn.prepareStatement(...)) { ... }}
     */
    public AccountResult<Account> loadAccountFromDatabase(Connection connection,
                                                           String accountNumber) {
        PreparedStatement statement = null;
        ResultSet resultSet = null;

        try {
            statement = connection.prepareStatement(
                    "SELECT * FROM accounts WHERE account_number = ?");
            statement.setString(1, accountNumber);
            resultSet = statement.executeQuery();

            if (resultSet.next()) {
                Account account = mapResultSetToAccount(resultSet);
                return AccountResult.success(account);
            } else {
                return AccountResult.failure("NOT_FOUND",
                        "No account found with number: " + accountNumber);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database error loading account: " + accountNumber, e);
            return AccountResult.failure("DB_ERROR", "Database error: " + e.getMessage());
        } finally {
            // Verbose manual close — error-prone if close() itself throws
            if (resultSet != null) {
                try {
                    resultSet.close();
                } catch (SQLException e) {
                    LOGGER.log(Level.WARNING, "Failed to close ResultSet", e);
                }
            }
            if (statement != null) {
                try {
                    statement.close();
                } catch (SQLException e) {
                    LOGGER.log(Level.WARNING, "Failed to close PreparedStatement", e);
                }
            }
        }
    }

    // -----------------------------------------------------------------------
    // 7. StringBuffer instead of StringBuilder (single-threaded context)
    // -----------------------------------------------------------------------

    /**
     * Builds a formatted account summary string.
     *
     * Uses {@code StringBuffer} — which is synchronised — even though
     * this method is single-threaded.  Replace with {@code StringBuilder}.
     */
    public String buildAccountSummary(List accounts) {
        StringBuffer buffer = new StringBuffer();  // synchronised — unnecessary here
        buffer.append("=== Account Summary ===\n");
        for (int i = 0; i < accounts.size(); i++) {
            Account account = (Account) accounts.get(i);
            buffer.append("Account: ").append(account.getAccountNumber())
                    .append(" | Type: ").append(account.getAccountType())
                    .append(" | Balance: ").append(account.getBalance())
                    .append(" | Active: ").append(account.isActive())
                    .append("\n");
        }
        buffer.append("Total accounts: ").append(accounts.size());
        return buffer.toString();
    }

    // -----------------------------------------------------------------------
    // 8. Enumeration from legacy Hashtable API
    // -----------------------------------------------------------------------

    /**
     * Logs all active customer sessions.
     *
     * Iterates with {@code Hashtable.keys()} which returns an {@code Enumeration}.
     * Modernise by replacing {@code Hashtable} with {@code ConcurrentHashMap}
     * and using {@code forEach} or an enhanced for-loop over {@code entrySet()}.
     */
    public void logActiveSessions() {
        Enumeration keys = customerSessions.keys();   // raw Enumeration — intentional
        StringBuffer logLine = new StringBuffer("Active sessions: ");
        while (keys.hasMoreElements()) {
            String customerId = (String) keys.nextElement();  // cast needed
            logLine.append(customerId).append(" ");
        }
        LOGGER.info(logLine.toString());
    }

    /**
     * Registers a session token for a customer.
     */
    public void registerSession(String customerId, String sessionToken) {
        customerSessions.put(customerId, sessionToken);
    }

    // -----------------------------------------------------------------------
    // 9. instanceof + explicit cast (no pattern matching)
    // -----------------------------------------------------------------------

    /**
     * Processes a generic account event object.
     *
     * Uses the old {@code instanceof} + explicit cast idiom.
     * Java 16+ pattern matching: {@code if (event instanceof DepositEvent de) { ... }}
     */
    public void processAccountEvent(Object event) {
        if (event instanceof DepositEvent) {
            DepositEvent depositEvent = (DepositEvent) event;  // redundant cast in Java 16+
            LOGGER.info("Processing deposit: " + depositEvent.amount
                    + " for account " + depositEvent.accountNumber);
            handleDeposit(depositEvent.accountNumber, depositEvent.amount);

        } else if (event instanceof WithdrawalEvent) {
            WithdrawalEvent withdrawalEvent = (WithdrawalEvent) event;  // redundant cast
            LOGGER.info("Processing withdrawal: " + withdrawalEvent.amount
                    + " for account " + withdrawalEvent.accountNumber);
            handleWithdrawal(withdrawalEvent.accountNumber, withdrawalEvent.amount);

        } else if (event instanceof AccountClosureEvent) {
            AccountClosureEvent closureEvent = (AccountClosureEvent) event;  // redundant cast
            LOGGER.info("Processing closure for account: " + closureEvent.accountNumber);
            handleClosure(closureEvent.accountNumber, closureEvent.reason);

        } else {
            LOGGER.warning("Unknown event type: " + event.getClass().getName());
        }
    }

    // -----------------------------------------------------------------------
    // Static inner event classes (candidates for records)
    // -----------------------------------------------------------------------

    public static class DepositEvent {
        public final String accountNumber;
        public final double amount;

        public DepositEvent(String accountNumber, double amount) {
            this.accountNumber = accountNumber;
            this.amount = amount;
        }
    }

    public static class WithdrawalEvent {
        public final String accountNumber;
        public final double amount;

        public WithdrawalEvent(String accountNumber, double amount) {
            this.accountNumber = accountNumber;
            this.amount = amount;
        }
    }

    public static class AccountClosureEvent {
        public final String accountNumber;
        public final String reason;

        public AccountClosureEvent(String accountNumber, String reason) {
            this.accountNumber = accountNumber;
            this.reason = reason;
        }
    }

    // -----------------------------------------------------------------------
    // Private helpers
    // -----------------------------------------------------------------------

    private String generateAccountNumber(String customerId, AccountType type) {
        StringBuffer sb = new StringBuffer();
        sb.append(type.name().substring(0, 2).toUpperCase());
        sb.append(customerId.hashCode() & 0xFFFFF);
        sb.append(System.currentTimeMillis() % 10000);
        return sb.toString();
    }

    private Account mapResultSetToAccount(ResultSet rs) throws SQLException {
        return new Account(
                rs.getString("account_number"),
                rs.getString("customer_id"),
                AccountType.valueOf(rs.getString("account_type")),
                rs.getDouble("balance"),
                rs.getDate("opened_date"),
                rs.getDate("maturity_date"),
                rs.getBoolean("active"),
                rs.getString("branch_code")
        );
    }

    private void handleDeposit(String accountNumber, double amount) {
        LOGGER.info("Deposit of " + amount + " applied to " + accountNumber);
    }

    private void handleWithdrawal(String accountNumber, double amount) {
        LOGGER.info("Withdrawal of " + amount + " from " + accountNumber);
    }

    private void handleClosure(String accountNumber, String reason) {
        LOGGER.info("Account " + accountNumber + " closed. Reason: " + reason);
    }
}

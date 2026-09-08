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
 * Responsible for account lifecycle operations: opening accounts,
 * calculating maturity dates, sorting and querying accounts,
 * loading account data from persistence, and dispatching
 * asynchronous customer notifications.
 */
public class CustomerAccountService {

    private static final Logger LOGGER = Logger.getLogger(CustomerAccountService.class.getName());

    private final NotificationService notificationService;

    // Holds active session tokens keyed by customer ID
    private final Hashtable customerSessions = new Hashtable();

    public CustomerAccountService(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    /**
     * Calculates the maturity date for a Fixed Deposit account.
     *
     * @param openingDate the date the account was opened
     * @param termMonths  the fixed deposit term in months
     * @return the date on which the fixed deposit matures
     */
    public Date calculateMaturityDate(Date openingDate, int termMonths) {
        if (openingDate == null) {
            throw new IllegalArgumentException("Opening date must not be null");
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(openingDate);
        calendar.add(Calendar.MONTH, termMonths);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }

    /**
     * Returns the number of days since the account was opened.
     *
     * @param openedDate the date the account was opened
     * @return days elapsed, or 0 if the date is null
     */
    public long getDaysSinceOpening(Date openedDate) {
        if (openedDate == null) {
            return 0L;
        }
        long diffMillis = new Date().getTime() - openedDate.getTime();
        return diffMillis / (1000L * 60 * 60 * 24);
    }

    /**
     * Returns all accounts held by the given customer.
     *
     * @param customerId the customer identifier
     * @return list of accounts
     */
    public List getAccountsForCustomer(String customerId) {
        List accounts = new ArrayList();
        accounts.add(new Account("ACC001", customerId, AccountType.SAVINGS,
                15000.00, new Date(), null, true, "BR001"));
        accounts.add(new Account("ACC002", customerId, AccountType.FIXED_DEPOSIT,
                50000.00, new Date(), calculateMaturityDate(new Date(), 12), true, "BR001"));
        return accounts;
    }

    /**
     * Returns all accounts for the given customer, sorted by balance descending.
     *
     * @param customerId the customer identifier
     * @return accounts sorted highest balance first
     */
    public List getSortedAccountsByBalance(String customerId) {
        List accounts = getAccountsForCustomer(customerId);

        Collections.sort(accounts, new Comparator<Account>() {
            @Override
            public int compare(Account a1, Account a2) {
                if (a2.getBalance() > a1.getBalance()) return 1;
                if (a2.getBalance() < a1.getBalance()) return -1;
                return 0;
            }
        });

        return accounts;
    }

    /**
     * Finds a single account by account number for a given customer.
     * Returns null if the account is not found.
     *
     * @param accountNumber the account number to search for
     * @param customerId    the owning customer's identifier
     * @return the matching account, or null
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
            Account account = (Account) accounts.get(i);
            if (accountNumber.equals(account.getAccountNumber())) {
                return account;
            }
        }
        return null;
    }

    /**
     * Returns the branch code for an account, or "UNKNOWN" if unavailable.
     *
     * @param account the account to query
     * @return branch code string, never null
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

    /**
     * Opens a new account for a customer and dispatches a welcome notification.
     *
     * @param customerId     the customer identifier
     * @param email          the customer's email address for notifications
     * @param type           the type of account to open
     * @param initialDeposit the opening deposit amount
     * @return a result containing the new account, or a failure with an error code
     */
    public AccountResult<Account> openAccount(String customerId,
                                               String email,
                                               AccountType type,
                                               double initialDeposit) {
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

    /**
     * Loads account details from the database for the given account number.
     *
     * @param connection    an active database connection
     * @param accountNumber the account number to load
     * @return a result containing the account, or a failure with an error code
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

    /**
     * Builds a formatted summary string for a list of accounts.
     *
     * @param accounts the accounts to summarise
     * @return formatted multi-line summary
     */
    public String buildAccountSummary(List accounts) {
        StringBuffer buffer = new StringBuffer();
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

    /**
     * Logs all customer IDs that currently have active sessions.
     */
    public void logActiveSessions() {
        Enumeration keys = customerSessions.keys();
        StringBuffer logLine = new StringBuffer("Active sessions: ");
        while (keys.hasMoreElements()) {
            String customerId = (String) keys.nextElement();
            logLine.append(customerId).append(" ");
        }
        LOGGER.info(logLine.toString());
    }

    /**
     * Registers a session token for a customer.
     *
     * @param customerId   the customer identifier
     * @param sessionToken the session token to store
     */
    public void registerSession(String customerId, String sessionToken) {
        customerSessions.put(customerId, sessionToken);
    }

    /**
     * Processes an account event object, dispatching to the appropriate handler.
     *
     * Supported event types: DepositEvent, WithdrawalEvent, AccountClosureEvent.
     *
     * @param event the event to process
     */
    public void processAccountEvent(Object event) {
        if (event instanceof DepositEvent) {
            DepositEvent depositEvent = (DepositEvent) event;
            LOGGER.info("Processing deposit: " + depositEvent.amount
                    + " for account " + depositEvent.accountNumber);
            handleDeposit(depositEvent.accountNumber, depositEvent.amount);

        } else if (event instanceof WithdrawalEvent) {
            WithdrawalEvent withdrawalEvent = (WithdrawalEvent) event;
            LOGGER.info("Processing withdrawal: " + withdrawalEvent.amount
                    + " for account " + withdrawalEvent.accountNumber);
            handleWithdrawal(withdrawalEvent.accountNumber, withdrawalEvent.amount);

        } else if (event instanceof AccountClosureEvent) {
            AccountClosureEvent closureEvent = (AccountClosureEvent) event;
            LOGGER.info("Processing closure for account: " + closureEvent.accountNumber);
            handleClosure(closureEvent.accountNumber, closureEvent.reason);

        } else {
            LOGGER.warning("Unknown event type: " + event.getClass().getName());
        }
    }

    // -------------------------------------------------------------------------
    // Inner event classes
    // -------------------------------------------------------------------------

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

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

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

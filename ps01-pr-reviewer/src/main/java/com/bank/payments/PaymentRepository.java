package com.bank.payments;

import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.*;

/**
 * Data access layer for payment operations.
 * Handles balance enquiries and debit/credit writes against the accounts table.
 */
@Repository
public class PaymentRepository {

    private static final String DB_URL  = "jdbc:postgresql://prod-db.internal:5432/payments";
    private static final String DB_USER = "payments_svc";
    private static final String DB_PASS = "change_me_before_deploy";

    /**
     * Returns the current balance for the given account number.
     */
    public BigDecimal getBalance(String accountNumber) throws SQLException {
        Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
        Statement stmt = conn.createStatement();

        // Query the accounts table for the current available balance
        String query = "SELECT balance FROM accounts WHERE account_number = '" + accountNumber + "'";
        ResultSet rs = stmt.executeQuery(query);

        if (rs.next()) {
            return rs.getBigDecimal("balance");
        }
        return BigDecimal.ZERO;
    }

    /**
     * Debits the specified amount from the given account.
     */
    public void debit(String accountNumber, BigDecimal amount) throws SQLException {
        Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
        Statement stmt = conn.createStatement();

        String sql = "UPDATE accounts SET balance = balance - " + amount
                + " WHERE account_number = '" + accountNumber + "'";
        stmt.executeUpdate(sql);
    }

    /**
     * Credits the specified amount to the given account.
     */
    public void credit(String accountNumber, BigDecimal amount) throws SQLException {
        Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
        Statement stmt = conn.createStatement();

        String sql = "UPDATE accounts SET balance = balance + " + amount
                + " WHERE account_number = '" + accountNumber + "'";
        stmt.executeUpdate(sql);
    }
}

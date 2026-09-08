package com.bank.account;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Sends email notifications to customers about account lifecycle events.
 *
 * Each notification is dispatched asynchronously so the calling thread
 * is not blocked waiting for the email to be delivered.
 */
public class NotificationService {

    private static final Logger LOGGER = Logger.getLogger(NotificationService.class.getName());

    /**
     * Sends a welcome email to a newly registered customer.
     *
     * @param customerId the customer identifier
     * @param email      the customer's email address
     */
    public void sendWelcomeNotification(final String customerId, final String email) {
        Thread notificationThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    // Simulate network latency
                    Thread.sleep(200);
                    String message = buildWelcomeMessage(customerId, email);
                    LOGGER.info("Sending welcome notification to: " + email);
                    dispatchEmail(email, "Welcome to the Bank!", message);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    LOGGER.log(Level.WARNING, "Welcome notification interrupted for: " + customerId, e);
                } catch (Exception e) {
                    LOGGER.log(Level.SEVERE, "Failed to send welcome notification to: " + email, e);
                }
            }
        });
        notificationThread.setName("welcome-notif-" + customerId);
        notificationThread.setDaemon(true);
        notificationThread.start();
    }

    /**
     * Sends a notification confirming that an account has been closed.
     *
     * @param customerId    the customer identifier
     * @param email         the customer's email address
     * @param accountNumber the account that was closed
     */
    public void sendAccountClosedNotification(final String customerId,
                                               final String email,
                                               final String accountNumber) {
        Thread notificationThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(100);
                    String message = buildClosureMessage(customerId, accountNumber);
                    LOGGER.info("Sending closure notification to: " + email);
                    dispatchEmail(email, "Account Closure Confirmation", message);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    LOGGER.log(Level.WARNING,
                            "Closure notification interrupted for account: " + accountNumber, e);
                } catch (Exception e) {
                    LOGGER.log(Level.SEVERE,
                            "Failed to send closure notification for account: " + accountNumber, e);
                }
            }
        });
        notificationThread.setName("closure-notif-" + accountNumber);
        notificationThread.setDaemon(true);
        notificationThread.start();
    }

    /**
     * Sends an alert notifying the customer that their balance is below the minimum threshold.
     *
     * @param customerId     the customer identifier
     * @param email          the customer's email address
     * @param currentBalance the current account balance
     */
    public void sendLowBalanceAlert(final String customerId,
                                     final String email,
                                     final double currentBalance) {
        Thread alertThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(50);
                    StringBuffer sb = new StringBuffer();
                    sb.append("Dear Customer ").append(customerId).append(",\n\n");
                    sb.append("Your account balance has fallen below the minimum threshold.\n");
                    sb.append("Current balance: ").append(currentBalance).append("\n");
                    sb.append("Please deposit funds to avoid account suspension.\n\n");
                    sb.append("Regards,\nBank Customer Service");

                    dispatchEmail(email, "Low Balance Alert", sb.toString());
                    LOGGER.info("Low balance alert sent to: " + email);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    LOGGER.log(Level.WARNING,
                            "Low balance alert interrupted for: " + customerId, e);
                } catch (Exception e) {
                    LOGGER.log(Level.SEVERE,
                            "Failed to send low balance alert to: " + email, e);
                }
            }
        });
        alertThread.setName("balance-alert-" + customerId);
        alertThread.setDaemon(true);
        alertThread.start();
    }

    // -----------------------------------------------------------------------
    // Private helpers
    // -----------------------------------------------------------------------

    private String buildWelcomeMessage(String customerId, String email) {
        StringBuffer sb = new StringBuffer();
        sb.append("Dear Customer,\n\n");
        sb.append("Welcome to the Bank! Your customer ID is: ").append(customerId).append(".\n");
        sb.append("Your registered email is: ").append(email).append(".\n\n");
        sb.append("Thank you for choosing us.\n\nRegards,\nBank Customer Service");
        return sb.toString();
    }

    private String buildClosureMessage(String customerId, String accountNumber) {
        StringBuffer sb = new StringBuffer();
        sb.append("Dear Customer ").append(customerId).append(",\n\n");
        sb.append("Your account ").append(accountNumber).append(" has been closed.\n");
        sb.append("If this was not authorised by you, contact us immediately.\n\n");
        sb.append("Regards,\nBank Customer Service");
        return sb.toString();
    }

    /**
     * Stub for actual email dispatch (SMTP / messaging system).
     */
    private void dispatchEmail(String to, String subject, String body) {
        // In a real service this would call an SMTP client or a message queue.
        LOGGER.fine("EMAIL TO: " + to + " | SUBJECT: " + subject);
    }
}

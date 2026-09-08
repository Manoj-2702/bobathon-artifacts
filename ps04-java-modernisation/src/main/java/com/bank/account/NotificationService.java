package com.bank.account;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Sends notifications to customers about account events.
 *
 * Java 8 style: every async operation spawns a raw platform thread via
 * {@code new Thread(new Runnable() { ... })}.  For a high-throughput
 * notification service this is wasteful — each thread consumes ~1 MB of stack.
 *
 * Modernisation targets (Java 21):
 *  - Replace {@code new Thread(runnable)} with
 *    {@code Thread.ofVirtual().start(runnable)} or
 *    {@code Executors.newVirtualThreadPerTaskExecutor()}
 *  - Replace anonymous {@code Runnable} inner classes with lambdas
 *  - Replace {@code StringBuffer} with {@code StringBuilder}
 *  - Use structured concurrency (JEP 453) for coordinated async operations
 */
public class NotificationService {

    private static final Logger LOGGER = Logger.getLogger(NotificationService.class.getName());

    /**
     * Sends a welcome email to a new customer.
     * Uses an anonymous Runnable and a raw platform Thread.
     */
    public void sendWelcomeNotification(final String customerId, final String email) {
        // Anonymous inner class — should become a lambda in Java 8+,
        // and the Thread itself should become a virtual thread in Java 21.
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
     * Sends an account-closed notification.
     * Another raw Thread + anonymous Runnable combination.
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
     * Sends a low-balance alert.
     * Yet another anonymous Runnable — repetitive boilerplate across all methods.
     */
    public void sendLowBalanceAlert(final String customerId,
                                     final String email,
                                     final double currentBalance) {
        Thread alertThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(50);
                    // StringBuffer used here — should be StringBuilder (not thread-safe context)
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
        // StringBuffer instead of StringBuilder — no concurrent access here
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

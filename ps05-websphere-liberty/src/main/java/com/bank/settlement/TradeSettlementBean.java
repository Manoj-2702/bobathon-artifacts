package com.bank.settlement;

import com.ibm.websphere.security.auth.WSSubject;
import com.ibm.ws.ffdc.FFDCFilter;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.math.BigDecimal;
import java.security.PrivilegedAction;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Stateless session bean orchestrating the end-of-day trade settlement cycle.
 *
 * Responsibilities:
 *   1. Authenticate the calling principal via the WAS security service.
 *   2. Delegate persistence and caching to TradeRepository.
 *   3. Write settlement audit records directly via JDBC.
 *   4. Record unhandled exceptions with WAS FFDC before re-throwing.
 *
 * Deployed as part of TradeSettlement.ear on a WAS 8.5.5.x ND cluster.
 * EJB bindings and security role mappings are in ibm-ejb-jar-bnd.xml.
 */
@Stateless
@TransactionAttribute(TransactionAttributeType.REQUIRED)
public class TradeSettlementBean {

    private static final Logger LOG = Logger.getLogger(TradeSettlementBean.class.getName());

    /** Component ID used when recording exceptions with WAS FFDC. */
    private static final String FFDC_SOURCE_ID =
        "com.bank.settlement.TradeSettlementBean";

    /**
     * EJB reference to the trade repository.
     * Bound via ibm-ejb-jar-bnd.xml.
     */
    @EJB(name = "java:comp/env/ejb/TradeRepo")
    private TradeRepository tradeRepository;

    // -------------------------------------------------------------------------
    // Public service operations
    // -------------------------------------------------------------------------

    /**
     * Settles a single trade by ID.
     *
     * Verifies the calling identity via WSSubject, then delegates to the
     * repository.  Any infrastructure exception is recorded with FFDC before
     * propagation so that WAS server logs contain a first-failure data capture
     * entry for support triage.
     *
     * @param tradeId  the primary key of the trade to settle
     * @return the updated Trade entity
     * @throws TradeSettlementException if the trade cannot be settled
     */
    public Trade settleTrade(Long tradeId) throws TradeSettlementException {
        // Retrieve the authenticated caller's identity from the WAS security runtime.
        String callerIdentity = "UNKNOWN";
        try {
            javax.security.auth.Subject callerSubject =
                WSSubject.getCallerSubject();
            if (callerSubject != null) {
                // Extract the WAS WSPrincipal from the subject credentials set
                java.util.Set<java.security.Principal> principals =
                    callerSubject.getPrincipals();
                if (!principals.isEmpty()) {
                    callerIdentity = principals.iterator().next().getName();
                }
            }
        } catch (com.ibm.websphere.security.WSSecurityException e) {
            // Non-fatal — log and continue; audit trail will show UNKNOWN
            LOG.log(Level.WARNING, "Could not retrieve caller subject from WSSubject", e);
            FFDCFilter.processException(
                e,
                FFDC_SOURCE_ID + ".settleTrade",
                "100",
                this
            );
        }

        LOG.info("settleTrade invoked by '" + callerIdentity + "' for tradeId=" + tradeId);

        Trade trade;
        try {
            trade = tradeRepository.findById(tradeId);
            if (trade == null) {
                throw new TradeSettlementException("Trade not found: " + tradeId);
            }
            if (!trade.isPending()) {
                throw new TradeSettlementException(
                    "Trade " + tradeId + " is in status " + trade.getStatus()
                    + " — only PENDING trades can be settled");
            }

            trade.markSettled();
            trade = tradeRepository.update(trade);

            writeAuditRecord(tradeId, callerIdentity, "SETTLED");

        } catch (TradeSettlementException tse) {
            throw tse;
        } catch (Exception e) {
            FFDCFilter.processException(
                e,
                FFDC_SOURCE_ID + ".settleTrade",
                "200",
                this
            );
            throw new TradeSettlementException("Unexpected error settling trade " + tradeId, e);
        }

        return trade;
    }

    /**
     * Runs the batch end-of-day settlement cycle for all PENDING trades.
     *
     * Iterates over pending trades and attempts to settle each one individually.
     * Failures are recorded with FFDC and logged; the batch continues to the
     * next trade rather than rolling back the entire cycle.
     *
     * @return count of successfully settled trades
     */
    public int runEndOfDaySettlement() {
        List<Trade> pending = tradeRepository.findByStatus(Trade.SettlementStatus.PENDING);
        LOG.info("EOD settlement cycle: found " + pending.size() + " pending trades");

        int settled = 0;
        for (Trade trade : pending) {
            try {
                settleTrade(trade.getTradeId());
                settled++;
            } catch (TradeSettlementException e) {
                LOG.log(Level.SEVERE, "Failed to settle trade " + trade.getTradeId() + " during EOD batch", e);
                FFDCFilter.processException(
                    e,
                    FFDC_SOURCE_ID + ".runEndOfDaySettlement",
                    "300",
                    this
                );
            }
        }

        LOG.info("EOD settlement complete: " + settled + "/" + pending.size() + " trades settled");
        return settled;
    }

    /**
     * Books a new trade into the settlement system.
     *
     * The caller's identity is captured from WSSubject and stored as the
     * traderId on the entity.
     */
    public Trade bookTrade(String isin, String counterpartyId,
                           BigDecimal quantity, BigDecimal price,
                           Trade.InstrumentType instrumentType) throws TradeSettlementException {
        String callerIdentity = resolveCallerIdentity();
        Trade trade = new Trade(isin, counterpartyId, quantity, price, instrumentType, callerIdentity);
        try {
            return tradeRepository.persist(trade);
        } catch (Exception e) {
            FFDCFilter.processException(
                e,
                FFDC_SOURCE_ID + ".bookTrade",
                "400",
                this
            );
            throw new TradeSettlementException("Failed to book trade for ISIN=" + isin, e);
        }
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    /**
     * Resolves the current caller principal name using WSSubject.
     * Falls back to "SYSTEM" if the security context is unavailable.
     */
    private String resolveCallerIdentity() {
        try {
            javax.security.auth.Subject subject = WSSubject.getCallerSubject();
            if (subject != null && !subject.getPrincipals().isEmpty()) {
                return subject.getPrincipals().iterator().next().getName();
            }
        } catch (com.ibm.websphere.security.WSSecurityException e) {
            LOG.log(Level.WARNING, "WSSubject.getCallerSubject() failed", e);
        }
        return "SYSTEM";
    }

    /**
     * Writes a settlement audit row to the AUDIT_LOG table via direct JDBC.
     *
     * @param tradeId the trade that was settled
     * @param userId  the identity of the user who triggered settlement
     * @param action  the action being audited
     */
    private void writeAuditRecord(Long tradeId, String userId, String action) {
        DataSource ds = lookupDataSource();
        if (ds == null) {
            LOG.warning("Datasource unavailable — skipping audit record for trade " + tradeId);
            return;
        }

        String sql = "INSERT INTO BANKDB.SETTLEMENT_AUDIT "
                   + "(TRADE_ID, USER_ID, ACTION, ACTION_TS) "
                   + "VALUES (?, ?, ?, CURRENT TIMESTAMP)";

        try (Connection conn = ds.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, tradeId);
            ps.setString(2, userId);
            ps.setString(3, action);
            ps.executeUpdate();
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Failed to write audit record for trade " + tradeId, e);
            FFDCFilter.processException(
                e,
                FFDC_SOURCE_ID + ".writeAuditRecord",
                "500",
                this
            );
        }
    }

    /**
     * Looks up the JDBC DataSource from the JNDI namespace.
     *
     * @return the DataSource, or null if the lookup fails
     */
    private DataSource lookupDataSource() {
        try {
            InitialContext ctx = new InitialContext();
            return (DataSource) ctx.lookup("jdbc/TradeDS");
        } catch (NamingException e) {
            LOG.log(Level.SEVERE, "JNDI lookup failed for jdbc/TradeDS", e);
            FFDCFilter.processException(
                e,
                FFDC_SOURCE_ID + ".lookupDataSource",
                "600",
                this
            );
            return null;
        }
    }

    // -------------------------------------------------------------------------
    // Checked exception
    // -------------------------------------------------------------------------

    /**
     * Application-level exception for settlement failures.
     * Declared as a checked exception so the EJB container does not
     * automatically mark the transaction for rollback on throw.
     */
    public static class TradeSettlementException extends Exception {
        public TradeSettlementException(String message) {
            super(message);
        }
        public TradeSettlementException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}

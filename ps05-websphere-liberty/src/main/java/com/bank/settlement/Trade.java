package com.bank.settlement;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * JPA entity representing a single trade awaiting or completed settlement.
 *
 * Table: TRADE_SETTLEMENT (schema: BANKDB)
 *
 * This entity is standard JPA 2.0 — it requires no WAS-specific changes
 * and will work unchanged on Open Liberty.
 */
@Entity
@Table(name = "TRADE_SETTLEMENT", schema = "BANKDB")
@NamedQueries({
    @NamedQuery(
        name  = "Trade.findByStatus",
        query = "SELECT t FROM Trade t WHERE t.status = :status ORDER BY t.tradeDate ASC"
    ),
    @NamedQuery(
        name  = "Trade.findByCounterpartyAndStatus",
        query = "SELECT t FROM Trade t WHERE t.counterpartyId = :counterpartyId AND t.status = :status"
    )
})
public class Trade implements Serializable {

    private static final long serialVersionUID = 1L;

    public enum SettlementStatus {
        PENDING, MATCHED, SETTLED, FAILED, CANCELLED
    }

    public enum InstrumentType {
        EQUITY, FIXED_INCOME, DERIVATIVE
    }

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(name = "TRADE_ID", nullable = false)
    private Long tradeId;

    @Column(name = "ISIN", nullable = false, length = 12)
    private String isin;

    @Column(name = "COUNTERPARTY_ID", nullable = false, length = 20)
    private String counterpartyId;

    @Column(name = "QUANTITY", nullable = false, precision = 18, scale = 4)
    private BigDecimal quantity;

    @Column(name = "PRICE", nullable = false, precision = 18, scale = 6)
    private BigDecimal price;

    @Column(name = "NOTIONAL", nullable = false, precision = 24, scale = 4)
    private BigDecimal notional;

    @Enumerated(EnumType.STRING)
    @Column(name = "STATUS", nullable = false, length = 20)
    private SettlementStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "INSTRUMENT_TYPE", nullable = false, length = 20)
    private InstrumentType instrumentType;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "TRADE_DATE", nullable = false)
    private Date tradeDate;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "SETTLEMENT_DATE")
    private Date settlementDate;

    @Column(name = "TRADER_ID", nullable = false, length = 30)
    private String traderId;

    @Column(name = "DESK_CODE", length = 10)
    private String deskCode;

    @Column(name = "VERSION")
    private Long version;

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    public Trade() {
        // Required by JPA
    }

    public Trade(String isin, String counterpartyId, BigDecimal quantity,
                 BigDecimal price, InstrumentType instrumentType, String traderId) {
        this.isin           = isin;
        this.counterpartyId = counterpartyId;
        this.quantity       = quantity;
        this.price          = price;
        this.notional       = quantity.multiply(price);
        this.instrumentType = instrumentType;
        this.traderId       = traderId;
        this.status         = SettlementStatus.PENDING;
        this.tradeDate      = new Date();
    }

    // -------------------------------------------------------------------------
    // Business helpers
    // -------------------------------------------------------------------------

    /**
     * Marks the trade as settled and records the settlement timestamp.
     */
    public void markSettled() {
        this.status         = SettlementStatus.SETTLED;
        this.settlementDate = new Date();
    }

    /**
     * Marks the trade as failed with no settlement date recorded.
     */
    public void markFailed() {
        this.status = SettlementStatus.FAILED;
    }

    public boolean isPending() {
        return SettlementStatus.PENDING.equals(this.status);
    }

    // -------------------------------------------------------------------------
    // Accessors
    // -------------------------------------------------------------------------

    public Long getTradeId()                    { return tradeId; }
    public String getIsin()                     { return isin; }
    public void setIsin(String isin)            { this.isin = isin; }
    public String getCounterpartyId()           { return counterpartyId; }
    public void setCounterpartyId(String id)    { this.counterpartyId = id; }
    public BigDecimal getQuantity()             { return quantity; }
    public void setQuantity(BigDecimal q)       { this.quantity = q; }
    public BigDecimal getPrice()                { return price; }
    public void setPrice(BigDecimal p)          { this.price = p; }
    public BigDecimal getNotional()             { return notional; }
    public void setNotional(BigDecimal n)       { this.notional = n; }
    public SettlementStatus getStatus()         { return status; }
    public void setStatus(SettlementStatus s)   { this.status = s; }
    public InstrumentType getInstrumentType()   { return instrumentType; }
    public void setInstrumentType(InstrumentType t) { this.instrumentType = t; }
    public Date getTradeDate()                  { return tradeDate; }
    public void setTradeDate(Date d)            { this.tradeDate = d; }
    public Date getSettlementDate()             { return settlementDate; }
    public void setSettlementDate(Date d)       { this.settlementDate = d; }
    public String getTraderId()                 { return traderId; }
    public void setTraderId(String id)          { this.traderId = id; }
    public String getDeskCode()                 { return deskCode; }
    public void setDeskCode(String code)        { this.deskCode = code; }
    public Long getVersion()                    { return version; }
    public void setVersion(Long v)              { this.version = v; }

    @Override
    public String toString() {
        return "Trade{tradeId=" + tradeId + ", isin='" + isin + '\''
             + ", status=" + status + ", notional=" + notional + '}';
    }
}

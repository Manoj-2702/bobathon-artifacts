package com.bank.transfers;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO representing a single transfer request.
 */
public class TransferRequest {

    @NotBlank(message = "Transfer ID must not be blank")
    private String transferId;

    @NotBlank(message = "Source account ID must not be blank")
    private String sourceAccountId;

    @NotBlank(message = "Destination account ID must not be blank")
    private String destinationAccountId;

    @NotNull(message = "Amount must not be null")
    @DecimalMin(value = "0.01", message = "Transfer amount must be at least £0.01")
    @DecimalMax(value = "250000.00", message = "Transfer amount must not exceed £250,000")
    private BigDecimal amount;

    /** When true the transfer is routed via SWIFT and international fee rules apply. */
    private boolean international;

    /** ISO 3166-1 alpha-2 destination country code; required for international transfers. */
    private String destinationCountryCode;

    private LocalDateTime requestedAt;

    public TransferRequest() {
        this.transferId = UUID.randomUUID().toString();
        this.requestedAt = LocalDateTime.now();
    }

    public TransferRequest(String sourceAccountId, String destinationAccountId,
                           BigDecimal amount, boolean international) {
        this();
        this.sourceAccountId = sourceAccountId;
        this.destinationAccountId = destinationAccountId;
        this.amount = amount;
        this.international = international;
    }

    // -------------------------------------------------------------------------
    // Getters and setters
    // -------------------------------------------------------------------------

    public String getTransferId() {
        return transferId;
    }

    public void setTransferId(String transferId) {
        this.transferId = transferId;
    }

    public String getSourceAccountId() {
        return sourceAccountId;
    }

    public void setSourceAccountId(String sourceAccountId) {
        this.sourceAccountId = sourceAccountId;
    }

    public String getDestinationAccountId() {
        return destinationAccountId;
    }

    public void setDestinationAccountId(String destinationAccountId) {
        this.destinationAccountId = destinationAccountId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public boolean isInternational() {
        return international;
    }

    public void setInternational(boolean international) {
        this.international = international;
    }

    public String getDestinationCountryCode() {
        return destinationCountryCode;
    }

    public void setDestinationCountryCode(String destinationCountryCode) {
        this.destinationCountryCode = destinationCountryCode;
    }

    public LocalDateTime getRequestedAt() {
        return requestedAt;
    }

    public void setRequestedAt(LocalDateTime requestedAt) {
        this.requestedAt = requestedAt;
    }

    @Override
    public String toString() {
        return "TransferRequest{id='" + transferId + "', from='" + sourceAccountId
                + "', to='" + destinationAccountId + "', amount=" + amount
                + ", international=" + international + "}";
    }
}

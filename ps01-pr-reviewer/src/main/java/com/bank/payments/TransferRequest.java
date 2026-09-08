package com.bank.payments;

import java.math.BigDecimal;

/**
 * Represents a payment transfer request submitted by the client.
 */
public class TransferRequest {

    public String fromAccount;
    public String toAccount;
    public BigDecimal amount;
    public String currency;
    public String reference;

}

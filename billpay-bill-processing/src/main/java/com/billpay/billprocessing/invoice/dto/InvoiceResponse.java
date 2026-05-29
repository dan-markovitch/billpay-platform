package com.billpay.billprocessing.invoice.dto;

import com.billpay.billprocessing.invoice.Invoice;
import com.billpay.billprocessing.invoice.InvoiceStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record InvoiceResponse(
    UUID id,
    String externalReference,
    BigDecimal amount,
    String currency,
    InvoiceStatus status,
    Instant receivedAt
) {
    public static InvoiceResponse from(Invoice invoice) {
        return new InvoiceResponse(
            invoice.getId(),
            invoice.getExternalReference(),
            invoice.getAmount(),
            invoice.getCurrency(),
            invoice.getStatus(),
            invoice.getReceivedAt()
        );
    }
}

package com.billpay.billprocessing.transaction.dto;

import com.billpay.billprocessing.transaction.Transaction;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record TransactionResponse(
    UUID id,
    String description,
    BigDecimal amount,
    String currency,
    LocalDate transactionDate,
    Instant createdAt
) {
    public static TransactionResponse from(Transaction transaction) {
        return new TransactionResponse(
            transaction.getId(),
            transaction.getDescription(),
            transaction.getAmount(),
            transaction.getCurrency(),
            transaction.getTransactionDate(),
            transaction.getCreatedAt()
        );
    }
}

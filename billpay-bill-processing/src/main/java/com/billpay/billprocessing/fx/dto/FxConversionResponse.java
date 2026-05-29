package com.billpay.billprocessing.fx.dto;

import com.billpay.billprocessing.transaction.Transaction;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record FxConversionResponse(
    UUID transactionId,
    String sourceCurrency,
    String targetCurrency,
    BigDecimal originalAmount,
    BigDecimal convertedAmount,
    BigDecimal exchangeRate,
    LocalDate exchangeRateDate
) {
    public static FxConversionResponse of(
        Transaction transaction,
        String targetCurrency,
        BigDecimal convertedAmount,
        BigDecimal exchangeRate,
        LocalDate exchangeRateDate
    ) {
        return new FxConversionResponse(
            transaction.getId(),
            transaction.getCurrency(),
            targetCurrency,
            transaction.getAmount(),
            convertedAmount,
            exchangeRate,
            exchangeRateDate
        );
    }
}

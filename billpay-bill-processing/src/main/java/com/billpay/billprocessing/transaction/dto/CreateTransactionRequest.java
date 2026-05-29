package com.billpay.billprocessing.transaction.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CreateTransactionRequest(
    @NotBlank
    @Size(max = 50, message = "must be at most 50 characters")
    String description,

    @NotNull
    @DecimalMin(value = "0.01", message = "must be greater than zero")
    BigDecimal amount,

    @NotBlank
    @Pattern(regexp = "[A-Z]{3}", message = "must be a 3-letter ISO currency code")
    String currency,

    @NotNull
    @PastOrPresent(message = "must not be in the future")
    LocalDate transactionDate
) {
}

package com.billpay.billprocessing.invoice.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record CreateInvoiceRequest(
    @NotBlank
    @Size(max = 100)
    String externalReference,

    @NotNull
    @DecimalMin(value = "0.01", message = "must be greater than zero")
    BigDecimal amount,

    @NotBlank
    @Pattern(regexp = "[A-Z]{3}", message = "must be a 3-letter ISO currency code")
    String currency
) {
}

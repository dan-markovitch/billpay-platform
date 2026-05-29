package com.billpay.billprocessing.fx;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ExchangeRateSelection(
    BigDecimal unitsOfForeignPerUsd,
    LocalDate effectiveDate
) {
}

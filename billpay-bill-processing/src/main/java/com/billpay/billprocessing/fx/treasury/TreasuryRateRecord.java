package com.billpay.billprocessing.fx.treasury;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;

public record TreasuryRateRecord(
    @JsonProperty("country_currency_desc") String countryCurrencyDesc,
    @JsonProperty("exchange_rate") String exchangeRate,
    @JsonProperty("record_date") LocalDate recordDate
) {
}

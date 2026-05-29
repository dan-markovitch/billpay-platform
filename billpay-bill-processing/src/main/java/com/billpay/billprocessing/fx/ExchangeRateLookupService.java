package com.billpay.billprocessing.fx;

import com.billpay.billprocessing.fx.treasury.TreasuryExchangeRateClient;
import com.billpay.billprocessing.fx.treasury.TreasuryRateRecord;
import com.billpay.common.api.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Comparator;

@Service
public class ExchangeRateLookupService {

    private final TreasuryExchangeRateClient treasuryClient;

    public ExchangeRateLookupService(TreasuryExchangeRateClient treasuryClient) {
        this.treasuryClient = treasuryClient;
    }

    public ExchangeRateSelection findRate(String isoCurrency, LocalDate transactionDate) {
        if (TreasuryCurrencyMapper.isUsd(isoCurrency)) {
            throw new IllegalArgumentException("USD does not have a Treasury exchange rate");
        }

        String countryCurrencyDesc = TreasuryCurrencyMapper.toCountryCurrencyDesc(isoCurrency);
        LocalDate earliestAllowed = transactionDate.minusMonths(6);

        return treasuryClient.fetchRates(countryCurrencyDesc, earliestAllowed, transactionDate).stream()
            .filter(rate -> !rate.recordDate().isAfter(transactionDate))
            .filter(rate -> !rate.recordDate().isBefore(earliestAllowed))
            .max(Comparator.comparing(TreasuryRateRecord::recordDate))
            .map(rate -> new ExchangeRateSelection(
                new BigDecimal(rate.exchangeRate()),
                rate.recordDate()
            ))
            .orElseThrow(() -> new ApiException(
                HttpStatus.BAD_REQUEST,
                "No exchange rate found for %s on or before %s within the last 6 months"
                    .formatted(isoCurrency, transactionDate)
            ));
    }
}

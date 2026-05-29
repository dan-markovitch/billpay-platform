package com.billpay.billprocessing.fx;

import com.billpay.billprocessing.fx.treasury.TreasuryExchangeRateClient;
import com.billpay.billprocessing.fx.treasury.TreasuryRateRecord;
import com.billpay.common.api.ApiException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExchangeRateLookupServiceTest {

    @Mock
    private TreasuryExchangeRateClient treasuryClient;

    @InjectMocks
    private ExchangeRateLookupService exchangeRateLookupService;

    @Test
    void findRate_returnsLatestRecordOnOrBeforeTransactionDate() {
        LocalDate transactionDate = LocalDate.of(2026, 5, 28);
        when(treasuryClient.fetchRates(
            eq("Euro Zone-Euro"),
            eq(transactionDate.minusMonths(6)),
            eq(transactionDate)
        )).thenReturn(List.of(
            new TreasuryRateRecord("Euro Zone-Euro", "0.80", LocalDate.of(2025, 9, 30)),
            new TreasuryRateRecord("Euro Zone-Euro", "0.851", LocalDate.of(2025, 12, 31))
        ));

        ExchangeRateSelection selection = exchangeRateLookupService.findRate("EUR", transactionDate);

        assertThat(selection.unitsOfForeignPerUsd()).isEqualByComparingTo("0.851");
        assertThat(selection.effectiveDate()).isEqualTo(LocalDate.of(2025, 12, 31));
        verify(treasuryClient).fetchRates(
            "Euro Zone-Euro",
            transactionDate.minusMonths(6),
            transactionDate
        );
    }

    @Test
    void findRate_throwsWhenNoQualifyingRateExists() {
        LocalDate transactionDate = LocalDate.of(2026, 5, 28);
        when(treasuryClient.fetchRates(eq("Euro Zone-Euro"), eq(transactionDate.minusMonths(6)), eq(transactionDate)))
            .thenReturn(List.of());

        assertThatThrownBy(() -> exchangeRateLookupService.findRate("EUR", transactionDate))
            .isInstanceOf(ApiException.class)
            .hasMessageContaining("No exchange rate found");
    }
}

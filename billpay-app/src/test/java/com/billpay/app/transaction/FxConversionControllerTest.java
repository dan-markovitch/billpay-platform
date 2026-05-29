package com.billpay.app.transaction;

import com.billpay.billprocessing.fx.treasury.TreasuryExchangeRateClient;
import com.billpay.billprocessing.fx.treasury.TreasuryRateRecord;
import com.billpay.common.api.ApiException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class FxConversionControllerTest {

    private static final LocalDate TRANSACTION_DATE = LocalDate.of(2026, 5, 28);
    private static final TreasuryRateRecord EURO_RATE = new TreasuryRateRecord(
        "Euro Zone-Euro",
        "0.851",
        LocalDate.of(2025, 12, 31)
    );

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TreasuryExchangeRateClient treasuryExchangeRateClient;

    @Test
    void convert_eurToUsd_usesLatestRateOnOrBeforeTransactionDate() throws Exception {
        stubEuroRates(EURO_RATE);
        String transactionId = createEurTransaction("100.00");

        mockMvc.perform(get("/api/v1/transactions/{id}/convert", transactionId)
                .param("targetCurrency", "USD"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.sourceCurrency").value("EUR"))
            .andExpect(jsonPath("$.data.targetCurrency").value("USD"))
            .andExpect(jsonPath("$.data.originalAmount").value(100.00))
            .andExpect(jsonPath("$.data.convertedAmount").value(117.5088))
            .andExpect(jsonPath("$.data.exchangeRateDate").value("2025-12-31"));
    }

    @Test
    void convert_usdToEur_multipliesByTreasuryRate() throws Exception {
        stubEuroRates(EURO_RATE);
        String transactionId = createUsdTransaction("100.00");

        mockMvc.perform(get("/api/v1/transactions/{id}/convert", transactionId)
                .param("targetCurrency", "EUR"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.convertedAmount").value(85.1000))
            .andExpect(jsonPath("$.data.exchangeRateDate").value("2025-12-31"));
    }

    @Test
    void convert_sameCurrency_returnsOriginalAmountWithUnitRate() throws Exception {
        String transactionId = createUsdTransaction("50.00");

        mockMvc.perform(get("/api/v1/transactions/{id}/convert", transactionId)
                .param("targetCurrency", "USD"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.convertedAmount").value(50.00))
            .andExpect(jsonPath("$.data.exchangeRate").value(1));
    }

    @Test
    void convert_returns400_whenNoRateWithinSixMonthWindow() throws Exception {
        when(treasuryExchangeRateClient.fetchRates(eq("Euro Zone-Euro"), any(), any()))
            .thenReturn(List.of());
        String transactionId = createEurTransaction("25.00");

        mockMvc.perform(get("/api/v1/transactions/{id}/convert", transactionId)
                .param("targetCurrency", "USD"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.error", containsString("No exchange rate found")))
            .andExpect(jsonPath("$.error", containsString("EUR")));
    }

    @Test
    void convert_returns400_whenOnlyRateIsAfterTransactionDate() throws Exception {
        TreasuryRateRecord futureRate = new TreasuryRateRecord(
            "Euro Zone-Euro",
            "0.90",
            TRANSACTION_DATE.plusMonths(1)
        );
        stubEuroRates(futureRate);
        String transactionId = createEurTransaction("10.00");

        mockMvc.perform(get("/api/v1/transactions/{id}/convert", transactionId)
                .param("targetCurrency", "USD"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error", containsString("No exchange rate found")));
    }

    @Test
    void convert_returns400_whenOnlyRateIsOlderThanSixMonths() throws Exception {
        TreasuryRateRecord staleRate = new TreasuryRateRecord(
            "Euro Zone-Euro",
            "0.90",
            TRANSACTION_DATE.minusMonths(7)
        );
        stubEuroRates(staleRate);
        String transactionId = createEurTransaction("10.00");

        mockMvc.perform(get("/api/v1/transactions/{id}/convert", transactionId)
                .param("targetCurrency", "USD"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error", containsString("No exchange rate found")));
    }

    @Test
    void convert_picksMostRecentRate_whenMultipleQualifyingRatesExist() throws Exception {
        TreasuryRateRecord older = new TreasuryRateRecord(
            "Euro Zone-Euro",
            "0.80",
            LocalDate.of(2025, 9, 30)
        );
        stubEuroRates(older, EURO_RATE);
        String transactionId = createEurTransaction("85.10");

        mockMvc.perform(get("/api/v1/transactions/{id}/convert", transactionId)
                .param("targetCurrency", "USD"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.convertedAmount").value(100.0000))
            .andExpect(jsonPath("$.data.exchangeRateDate").value("2025-12-31"));
    }

    @Test
    void convert_returns404_whenTransactionMissing() throws Exception {
        mockMvc.perform(get("/api/v1/transactions/{id}/convert", "00000000-0000-0000-0000-000000000099")
                .param("targetCurrency", "USD"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.error").value("Transaction not found"));
    }

    @Test
    void convert_returns400_forUnsupportedTargetCurrency() throws Exception {
        String transactionId = createUsdTransaction("10.00");

        mockMvc.perform(get("/api/v1/transactions/{id}/convert", transactionId)
                .param("targetCurrency", "ZZZ"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error", containsString("Unsupported currency")));
    }

    @Test
    void convert_returns400_forUnsupportedSourceCurrency() throws Exception {
        String transactionId = createTransaction("SEK", "10.00");

        mockMvc.perform(get("/api/v1/transactions/{id}/convert", transactionId)
                .param("targetCurrency", "USD"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error", containsString("Unsupported currency")));
    }

    @Test
    void convert_returns503_whenTreasuryIsUnavailable() throws Exception {
        when(treasuryExchangeRateClient.fetchRates(eq("Euro Zone-Euro"), any(), any()))
            .thenThrow(new ApiException(
                HttpStatus.SERVICE_UNAVAILABLE,
                "Treasury exchange rate service is unavailable"
            ));
        String transactionId = createEurTransaction("10.00");

        mockMvc.perform(get("/api/v1/transactions/{id}/convert", transactionId)
                .param("targetCurrency", "USD"))
            .andExpect(status().isServiceUnavailable())
            .andExpect(jsonPath("$.error", containsString("Treasury")));
    }

    private void stubEuroRates(TreasuryRateRecord... rates) {
        when(treasuryExchangeRateClient.fetchRates(eq("Euro Zone-Euro"), any(), any()))
            .thenReturn(List.of(rates));
    }

    private String createEurTransaction(String amount) throws Exception {
        return createTransaction("EUR", amount);
    }

    private String createUsdTransaction(String amount) throws Exception {
        return createTransaction("USD", amount);
    }

    private String createTransaction(String currency, String amount) throws Exception {
        MvcResult created = mockMvc.perform(post("/api/v1/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "description": "FX test",
                      "amount": %s,
                      "currency": "%s",
                      "transactionDate": "%s"
                    }
                    """.formatted(amount, currency, TRANSACTION_DATE)))
            .andExpect(status().isCreated())
            .andReturn();

        return com.jayway.jsonpath.JsonPath.read(
            created.getResponse().getContentAsString(),
            "$.data.id"
        ).toString();
    }
}

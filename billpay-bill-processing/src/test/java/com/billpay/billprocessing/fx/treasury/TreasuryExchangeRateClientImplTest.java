package com.billpay.billprocessing.fx.treasury;

import com.billpay.common.api.ApiException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class TreasuryExchangeRateClientImplTest {

    private MockRestServiceServer server;
    private TreasuryExchangeRateClient client;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder();
        server = MockRestServiceServer.bindTo(builder).build();
        client = new TreasuryExchangeRateClientImpl(builder, "https://api.test");
    }

    @AfterEach
    void verifyServer() {
        server.verify();
    }

    @Test
    void fetchRates_returnsParsedRecords() {
        server.expect(requestTo(containsString("rates_of_exchange")))
            .andExpect(method(HttpMethod.GET))
            .andRespond(withSuccess("""
                {
                  "data": [
                    {
                      "country_currency_desc": "Euro Zone-Euro",
                      "exchange_rate": "0.851",
                      "record_date": "2025-12-31"
                    }
                  ]
                }
                """, MediaType.APPLICATION_JSON));

        var rates = client.fetchRates(
            "Euro Zone-Euro",
            LocalDate.of(2025, 6, 1),
            LocalDate.of(2026, 5, 28)
        );

        assertThat(rates).hasSize(1);
        assertThat(rates.get(0).exchangeRate()).isEqualTo("0.851");
    }

    @Test
    void fetchRates_throws503_whenTreasuryReturnsServerError() {
        server.expect(requestTo(containsString("rates_of_exchange")))
            .andRespond(withServerError());

        assertThatThrownBy(() -> client.fetchRates(
            "Euro Zone-Euro",
            LocalDate.of(2025, 6, 1),
            LocalDate.of(2026, 5, 28)
        ))
            .isInstanceOf(ApiException.class)
            .satisfies(ex -> assertThat(((ApiException) ex).getStatus()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE));
    }
}

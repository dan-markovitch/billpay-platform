package com.billpay.billprocessing.fx.treasury;

import com.billpay.common.api.ApiException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDate;
import java.util.List;

@Component
public class TreasuryExchangeRateClientImpl implements TreasuryExchangeRateClient {

    private final RestClient restClient;

    public TreasuryExchangeRateClientImpl(
        RestClient.Builder restClientBuilder,
        @Value("${billpay.treasury.base-url}") String baseUrl
    ) {
        this.restClient = restClientBuilder.baseUrl(baseUrl).build();
    }

    @Override
    public List<TreasuryRateRecord> fetchRates(String countryCurrencyDesc, LocalDate fromDate, LocalDate toDate) {
        String filter = "country_currency_desc:eq:%s,record_date:gte:%s,record_date:lte:%s".formatted(
            countryCurrencyDesc,
            fromDate,
            toDate
        );

        String uri = UriComponentsBuilder.fromPath("/v1/accounting/od/rates_of_exchange")
            .queryParam("fields", "country_currency_desc,exchange_rate,record_date")
            .queryParam("filter", filter)
            .queryParam("sort", "-record_date")
            .queryParam("page[size]", "100")
            .build()
            .encode()
            .toUriString();

        try {
            TreasuryRatesResponse response = restClient.get()
                .uri(uri)
                .retrieve()
                .body(TreasuryRatesResponse.class);

            return response != null && response.data() != null ? response.data() : List.of();
        } catch (RestClientResponseException ex) {
            HttpStatus status = ex.getStatusCode().is5xxServerError()
                ? HttpStatus.SERVICE_UNAVAILABLE
                : HttpStatus.BAD_GATEWAY;
            throw new ApiException(
                status,
                "Unable to retrieve exchange rates from Treasury (HTTP %d)".formatted(ex.getStatusCode().value())
            );
        } catch (ResourceAccessException ex) {
            throw new ApiException(
                HttpStatus.SERVICE_UNAVAILABLE,
                "Treasury exchange rate service is unavailable"
            );
        }
    }
}

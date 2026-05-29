package com.billpay.billprocessing.fx;

import com.billpay.common.api.ApiException;
import org.springframework.http.HttpStatus;

import java.util.Map;

public final class TreasuryCurrencyMapper {

    private static final Map<String, String> ISO_TO_COUNTRY_CURRENCY_DESC = Map.of(
        "EUR", "Euro Zone-Euro",
        "GBP", "United Kingdom-Pound",
        "CAD", "Canada-Dollar",
        "MXN", "Mexico-Peso",
        "JPY", "Japan-Yen",
        "CHF", "Switzerland-Franc",
        "AUD", "Australia-Dollar"
    );

    private TreasuryCurrencyMapper() {
    }

    public static boolean isUsd(String isoCode) {
        return "USD".equals(isoCode);
    }

    public static String toCountryCurrencyDesc(String isoCode) {
        if (isUsd(isoCode)) {
            return null;
        }
        String description = ISO_TO_COUNTRY_CURRENCY_DESC.get(isoCode);
        if (description == null) {
            throw new ApiException(
                HttpStatus.BAD_REQUEST,
                "Unsupported currency for Treasury conversion: " + isoCode
            );
        }
        return description;
    }
}

package com.billpay.billprocessing.fx.treasury;

import java.time.LocalDate;
import java.util.List;

public interface TreasuryExchangeRateClient {

    List<TreasuryRateRecord> fetchRates(String countryCurrencyDesc, LocalDate fromDate, LocalDate toDate);
}

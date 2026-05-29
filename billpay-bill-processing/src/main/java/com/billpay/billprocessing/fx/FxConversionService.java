package com.billpay.billprocessing.fx;

import com.billpay.billprocessing.fx.dto.FxConversionResponse;
import com.billpay.billprocessing.transaction.Transaction;
import com.billpay.billprocessing.transaction.TransactionRepository;
import com.billpay.common.api.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.UUID;

@Service
public class FxConversionService {

    private static final int MONEY_SCALE = 4;

    private final TransactionRepository transactionRepository;
    private final ExchangeRateLookupService exchangeRateLookupService;

    public FxConversionService(
        TransactionRepository transactionRepository,
        ExchangeRateLookupService exchangeRateLookupService
    ) {
        this.transactionRepository = transactionRepository;
        this.exchangeRateLookupService = exchangeRateLookupService;
    }

    @Transactional(readOnly = true)
    public FxConversionResponse convert(UUID transactionId, String targetCurrency) {
        Transaction transaction = transactionRepository.findById(transactionId)
            .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Transaction not found"));

        String sourceCurrency = transaction.getCurrency();
        if (!targetCurrency.matches("[A-Z]{3}")) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "targetCurrency must be a 3-letter ISO code");
        }

        if (sourceCurrency.equals(targetCurrency)) {
            return FxConversionResponse.of(
                transaction,
                targetCurrency,
                transaction.getAmount(),
                BigDecimal.ONE,
                transaction.getTransactionDate()
            );
        }

        LocalDate transactionDate = transaction.getTransactionDate();
        BigDecimal amountInUsd = toUsd(transaction.getAmount(), sourceCurrency, transactionDate);
        BigDecimal convertedAmount = fromUsd(amountInUsd, targetCurrency, transactionDate);
        BigDecimal effectiveRate = transaction.getAmount().compareTo(BigDecimal.ZERO) == 0
            ? BigDecimal.ZERO
            : convertedAmount.divide(transaction.getAmount(), MONEY_SCALE, RoundingMode.HALF_UP);

        LocalDate rateDate = resolveRateDate(sourceCurrency, targetCurrency, transactionDate);

        return FxConversionResponse.of(
            transaction,
            targetCurrency,
            convertedAmount,
            effectiveRate,
            rateDate
        );
    }

    private LocalDate resolveRateDate(String sourceCurrency, String targetCurrency, LocalDate transactionDate) {
        if (!TreasuryCurrencyMapper.isUsd(sourceCurrency)) {
            return exchangeRateLookupService.findRate(sourceCurrency, transactionDate).effectiveDate();
        }
        return exchangeRateLookupService.findRate(targetCurrency, transactionDate).effectiveDate();
    }

    /**
     * Treasury rates are foreign currency units per one U.S. dollar.
     */
    private BigDecimal toUsd(BigDecimal amount, String currency, LocalDate transactionDate) {
        if (TreasuryCurrencyMapper.isUsd(currency)) {
            return amount;
        }
        ExchangeRateSelection rate = exchangeRateLookupService.findRate(currency, transactionDate);
        return amount.divide(rate.unitsOfForeignPerUsd(), MONEY_SCALE, RoundingMode.HALF_UP);
    }

    private BigDecimal fromUsd(BigDecimal amountUsd, String currency, LocalDate transactionDate) {
        if (TreasuryCurrencyMapper.isUsd(currency)) {
            return amountUsd;
        }
        ExchangeRateSelection rate = exchangeRateLookupService.findRate(currency, transactionDate);
        return amountUsd.multiply(rate.unitsOfForeignPerUsd()).setScale(MONEY_SCALE, RoundingMode.HALF_UP);
    }
}

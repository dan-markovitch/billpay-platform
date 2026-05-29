package com.billpay.billprocessing.transaction;

import com.billpay.billprocessing.fx.FxConversionService;
import com.billpay.billprocessing.fx.dto.FxConversionResponse;
import com.billpay.billprocessing.transaction.dto.CreateTransactionRequest;
import com.billpay.billprocessing.transaction.dto.TransactionResponse;
import com.billpay.common.api.ApiResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/transactions")
@Validated
public class TransactionController {

    private final TransactionService transactionService;
    private final FxConversionService fxConversionService;

    public TransactionController(
        TransactionService transactionService,
        FxConversionService fxConversionService
    ) {
        this.transactionService = transactionService;
        this.fxConversionService = fxConversionService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<TransactionResponse> create(@Valid @RequestBody CreateTransactionRequest request) {
        return ApiResponse.ok(transactionService.create(request));
    }

    @GetMapping("/{id}")
    public ApiResponse<TransactionResponse> getById(@PathVariable UUID id) {
        return ApiResponse.ok(transactionService.getById(id));
    }

    @GetMapping
    public ApiResponse<List<TransactionResponse>> list() {
        return ApiResponse.ok(transactionService.listAll());
    }

    @GetMapping("/{id}/convert")
    public ApiResponse<FxConversionResponse> convert(
        @PathVariable UUID id,
        @RequestParam @Pattern(regexp = "[A-Z]{3}", message = "must be a 3-letter ISO currency code") String targetCurrency
    ) {
        return ApiResponse.ok(fxConversionService.convert(id, targetCurrency.toUpperCase()));
    }
}

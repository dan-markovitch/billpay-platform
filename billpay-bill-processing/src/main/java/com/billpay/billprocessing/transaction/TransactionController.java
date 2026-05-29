package com.billpay.billprocessing.transaction;

import com.billpay.billprocessing.transaction.dto.CreateTransactionRequest;
import com.billpay.billprocessing.transaction.dto.TransactionResponse;
import com.billpay.common.api.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/transactions")
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
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
}

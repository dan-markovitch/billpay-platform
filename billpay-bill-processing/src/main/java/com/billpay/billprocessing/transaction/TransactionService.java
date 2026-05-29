package com.billpay.billprocessing.transaction;

import com.billpay.billprocessing.transaction.dto.CreateTransactionRequest;
import com.billpay.billprocessing.transaction.dto.TransactionResponse;
import com.billpay.common.api.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class TransactionService {

    private final TransactionRepository transactionRepository;

    public TransactionService(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    @Transactional
    public TransactionResponse create(CreateTransactionRequest request) {
        Transaction transaction = new Transaction(
            request.description(),
            request.amount(),
            request.currency(),
            request.transactionDate()
        );
        return TransactionResponse.from(transactionRepository.save(transaction));
    }

    @Transactional(readOnly = true)
    public TransactionResponse getById(UUID id) {
        return transactionRepository.findById(id)
            .map(TransactionResponse::from)
            .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Transaction not found"));
    }

    @Transactional(readOnly = true)
    public List<TransactionResponse> listAll() {
        return transactionRepository.findAllByOrderByCreatedAtDesc().stream()
            .map(TransactionResponse::from)
            .toList();
    }
}

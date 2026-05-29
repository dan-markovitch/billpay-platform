package com.billpay.billprocessing.invoice;

import com.billpay.billprocessing.invoice.dto.CreateInvoiceRequest;
import com.billpay.billprocessing.invoice.dto.InvoiceResponse;
import com.billpay.common.api.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class InvoiceService {

    private final InvoiceRepository invoiceRepository;

    public InvoiceService(InvoiceRepository invoiceRepository) {
        this.invoiceRepository = invoiceRepository;
    }

    @Transactional
    public InvoiceResponse ingest(CreateInvoiceRequest request) {
        invoiceRepository.findByExternalReference(request.externalReference())
            .ifPresent(existing -> {
                throw new ApiException(
                    HttpStatus.CONFLICT,
                    "Invoice with external reference already exists: " + request.externalReference()
                );
            });

        Invoice invoice = new Invoice(
            request.externalReference(),
            request.amount(),
            request.currency()
        );
        return InvoiceResponse.from(invoiceRepository.save(invoice));
    }

    @Transactional(readOnly = true)
    public InvoiceResponse getById(UUID id) {
        return invoiceRepository.findById(id)
            .map(InvoiceResponse::from)
            .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Invoice not found"));
    }
}

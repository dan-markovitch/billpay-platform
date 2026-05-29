package com.billpay.billprocessing.invoice;

import com.billpay.billprocessing.invoice.dto.CreateInvoiceRequest;
import com.billpay.billprocessing.invoice.dto.InvoiceResponse;
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

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/invoices")
public class InvoiceController {

    private final InvoiceService invoiceService;

    public InvoiceController(InvoiceService invoiceService) {
        this.invoiceService = invoiceService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<InvoiceResponse> ingest(@Valid @RequestBody CreateInvoiceRequest request) {
        return ApiResponse.ok(invoiceService.ingest(request));
    }

    @GetMapping("/{id}")
    public ApiResponse<InvoiceResponse> getById(@PathVariable UUID id) {
        return ApiResponse.ok(invoiceService.getById(id));
    }
}

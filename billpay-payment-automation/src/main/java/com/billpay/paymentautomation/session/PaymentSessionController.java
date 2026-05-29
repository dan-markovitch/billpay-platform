package com.billpay.paymentautomation.session;

import com.billpay.paymentautomation.session.dto.CreatePaymentSessionRequest;
import com.billpay.paymentautomation.session.dto.PaymentSessionResponse;
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
@RequestMapping("/api/v1/payment-sessions")
public class PaymentSessionController {

    private final PaymentSessionService paymentSessionService;

    public PaymentSessionController(PaymentSessionService paymentSessionService) {
        this.paymentSessionService = paymentSessionService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<PaymentSessionResponse> create(@Valid @RequestBody CreatePaymentSessionRequest request) {
        return ApiResponse.ok(paymentSessionService.create(request));
    }

    @GetMapping("/{id}")
    public ApiResponse<PaymentSessionResponse> getById(@PathVariable UUID id) {
        return ApiResponse.ok(paymentSessionService.getById(id));
    }
}

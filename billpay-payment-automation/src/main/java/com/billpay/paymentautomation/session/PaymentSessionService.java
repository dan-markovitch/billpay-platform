package com.billpay.paymentautomation.session;

import com.billpay.paymentautomation.session.dto.CreatePaymentSessionRequest;
import com.billpay.paymentautomation.session.dto.PaymentSessionResponse;
import com.billpay.common.api.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class PaymentSessionService {

    private final PaymentSessionRepository paymentSessionRepository;

    public PaymentSessionService(PaymentSessionRepository paymentSessionRepository) {
        this.paymentSessionRepository = paymentSessionRepository;
    }

    @Transactional
    public PaymentSessionResponse create(CreatePaymentSessionRequest request) {
        PaymentSession session = new PaymentSession(request.channel(), request.merchantId());
        return PaymentSessionResponse.from(paymentSessionRepository.save(session));
    }

    @Transactional(readOnly = true)
    public PaymentSessionResponse getById(UUID id) {
        return paymentSessionRepository.findById(id)
            .map(PaymentSessionResponse::from)
            .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Payment session not found"));
    }
}

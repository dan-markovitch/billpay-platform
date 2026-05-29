package com.billpay.paymentautomation.session.dto;

import com.billpay.paymentautomation.session.PaymentChannel;
import com.billpay.paymentautomation.session.PaymentSession;
import com.billpay.paymentautomation.session.PaymentSessionStatus;

import java.time.Instant;
import java.util.UUID;

public record PaymentSessionResponse(
    UUID id,
    PaymentChannel channel,
    String merchantId,
    PaymentSessionStatus status,
    Instant createdAt
) {
    public static PaymentSessionResponse from(PaymentSession session) {
        return new PaymentSessionResponse(
            session.getId(),
            session.getChannel(),
            session.getMerchantId(),
            session.getStatus(),
            session.getCreatedAt()
        );
    }
}

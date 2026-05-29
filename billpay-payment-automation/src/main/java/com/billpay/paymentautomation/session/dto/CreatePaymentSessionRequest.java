package com.billpay.paymentautomation.session.dto;

import com.billpay.paymentautomation.session.PaymentChannel;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreatePaymentSessionRequest(
    @NotNull
    PaymentChannel channel,

    @NotBlank
    @Size(max = 100)
    String merchantId
) {
}

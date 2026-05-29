package com.billpay.paymentautomation.session;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PaymentSessionRepository extends JpaRepository<PaymentSession, UUID> {
}

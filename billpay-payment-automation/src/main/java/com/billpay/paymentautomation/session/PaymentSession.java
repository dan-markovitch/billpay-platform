package com.billpay.paymentautomation.session;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "payment_sessions")
public class PaymentSession {

    @Id
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private PaymentChannel channel;

    @Column(name = "merchant_id", nullable = false, length = 100)
    private String merchantId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PaymentSessionStatus status;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected PaymentSession() {
    }

    public PaymentSession(PaymentChannel channel, String merchantId) {
        this.channel = channel;
        this.merchantId = merchantId;
        this.status = PaymentSessionStatus.INITIATED;
    }

    @PrePersist
    void onCreate() {
        if (id == null) {
            id = UUID.randomUUID();
        }
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }

    public UUID getId() {
        return id;
    }

    public PaymentChannel getChannel() {
        return channel;
    }

    public String getMerchantId() {
        return merchantId;
    }

    public PaymentSessionStatus getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}

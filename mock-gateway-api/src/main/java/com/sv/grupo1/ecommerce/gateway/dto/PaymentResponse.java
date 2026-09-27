package com.sv.grupo1.ecommerce.gateway.dto;

import com.sv.grupo1.ecommerce.gateway.enums.PaymentStatus;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * @author alexemestica
 * */
public record PaymentResponse (
        String transactionId,
        String merchantOrderId,
        PaymentStatus status,
        String responseCode,
        String responseMessage,
        String authorizationCode,
        BigDecimal amount,
        String currency,
        String lastFourDigits,
        Instant processedAt
) {}

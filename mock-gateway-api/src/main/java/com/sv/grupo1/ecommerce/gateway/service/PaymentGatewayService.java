package com.sv.grupo1.ecommerce.gateway.service;

import com.sv.grupo1.ecommerce.gateway.dto.PaymentRequest;
import com.sv.grupo1.ecommerce.gateway.dto.PaymentResponse;

/**
 * @author alexemestica
 * */
public interface PaymentGatewayService {

    public PaymentResponse processPayment(PaymentRequest request, String idempotencyKey);

    public PaymentResponse getTransactionById(String transactionId);

}

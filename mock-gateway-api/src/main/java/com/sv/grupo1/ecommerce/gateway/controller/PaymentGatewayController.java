package com.sv.grupo1.ecommerce.gateway.controller;

import com.sv.grupo1.ecommerce.gateway.dto.PaymentRequest;
import com.sv.grupo1.ecommerce.gateway.dto.PaymentResponse;
import com.sv.grupo1.ecommerce.gateway.service.PaymentGatewayService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author alexemestica
 * */
@RestController
@RequestMapping("/mock-gateway/v1/payments")
public class PaymentGatewayController {

    private final static Logger LOGGER = LoggerFactory.getLogger(PaymentGatewayController.class);
    private final PaymentGatewayService paymentGatewayService;

    public PaymentGatewayController(PaymentGatewayService paymentGatewayService) {
        this.paymentGatewayService = paymentGatewayService;
    }

    @PostMapping
    public ResponseEntity<PaymentResponse> createPayment(
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
            @Valid @RequestBody PaymentRequest request) {
        LOGGER.info("createPayment() => Start");
        PaymentResponse response = this.paymentGatewayService.processPayment(request, idempotencyKey);

        HttpStatus httpStatus = switch (response.status()) {
            case APPROVED -> HttpStatus.CREATED;
            case PENDING  -> HttpStatus.ACCEPTED;
            case DECLINED -> HttpStatus.PAYMENT_REQUIRED;
            case ERROR    -> HttpStatus.GATEWAY_TIMEOUT;
        };

        LOGGER.info("createPayment() => Completed");
        return ResponseEntity.status(httpStatus).body(response);
    }

    @GetMapping("/{transactionId}")
    public ResponseEntity<PaymentResponse> getPaymentStatus(@PathVariable String transactionId) {
        PaymentResponse response = this.paymentGatewayService.getTransactionById(transactionId);
        return (response != null) ? ResponseEntity.ok(response) : ResponseEntity.notFound().build();
    }

}

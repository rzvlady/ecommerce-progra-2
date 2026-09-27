package com.sv.grupo1.ecommerce.gateway.service.impl;

import com.sv.grupo1.ecommerce.gateway.dto.PaymentRequest;
import com.sv.grupo1.ecommerce.gateway.dto.PaymentResponse;
import com.sv.grupo1.ecommerce.gateway.enums.PaymentStatus;
import com.sv.grupo1.ecommerce.gateway.service.PaymentGatewayService;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

/**
 * @author alexemestica
 * */
@Service
public class PaymentGatewayServiceImpl implements PaymentGatewayService {

    private final Map<String, PaymentResponse> transactionsStore = new ConcurrentHashMap<>();
    private final Map<String, PaymentResponse> idempotencyCache = new ConcurrentHashMap<>();

    public PaymentResponse processPayment(PaymentRequest request, String idempotencyKey) {

        if (idempotencyKey != null && !idempotencyKey.isBlank() && idempotencyCache.containsKey(idempotencyKey)) {
            return idempotencyCache.get(idempotencyKey);
        }

        String transactionId = "pay_" + UUID.randomUUID().toString().replace("-", "").substring(0, 12);
        String lastFour = request.cardNumber().substring(12);

        PaymentGatewayServiceImpl.RuleResult evaluation = evaluateCardRules(lastFour);

        PaymentResponse response = new PaymentResponse(
                transactionId,
                request.merchantOrderId(),
                evaluation.status(),
                evaluation.code(),
                evaluation.message(),
                evaluation.authCode(),
                request.amount(),
                request.currency(),
                lastFour,
                Instant.now()
        );

        transactionsStore.put(transactionId, response);
        if (idempotencyKey != null && !idempotencyKey.isBlank()) {
            idempotencyCache.put(idempotencyKey, response);
        }

        return response;
    }

    public PaymentResponse getTransactionById(String transactionId) {
        return transactionsStore.get(transactionId);
    }

    private PaymentGatewayServiceImpl.RuleResult evaluateCardRules(String lastFour) {
        return switch (lastFour) {
            case "0002" -> new PaymentGatewayServiceImpl.RuleResult(PaymentStatus.DECLINED, "51", "Fondos insuficientes", null);
            case "0054" -> new PaymentGatewayServiceImpl.RuleResult(PaymentStatus.DECLINED, "54", "Tarjeta expirada", null);
            case "0069" -> new PaymentGatewayServiceImpl.RuleResult(PaymentStatus.DECLINED, "59", "Rechazado por regla antifraude", null);
            case "1111" -> new PaymentGatewayServiceImpl.RuleResult(PaymentStatus.PENDING, "09", "Pago en validación asíncrona", null);
            case "9999" -> {
                simulateNetworkDelay(4000);
                yield new PaymentGatewayServiceImpl.RuleResult(PaymentStatus.ERROR, "91", "Timeout con el banco emisor", null);
            }
            default -> {
                String authCode = String.format("%06d", ThreadLocalRandom.current().nextInt(100000, 999999));
                yield new PaymentGatewayServiceImpl.RuleResult(PaymentStatus.APPROVED, "00", "Transacción aprobada", authCode);
            }
        };
    }

    private void simulateNetworkDelay(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private record RuleResult(PaymentStatus status, String code, String message, String authCode) {}
}

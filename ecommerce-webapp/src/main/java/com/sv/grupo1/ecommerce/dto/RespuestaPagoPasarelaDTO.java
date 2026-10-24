package com.sv.grupo1.ecommerce.dto;

import java.math.BigDecimal;
import java.time.Instant;

import com.sv.grupo1.ecommerce.enums.EstadoPasarela;

public class RespuestaPagoPasarelaDTO {

    private String transactionId;
    private String merchantOrderId;
    private EstadoPasarela status;
    private String responseCode;
    private String responseMessage;
    private String authorizationCode;
    private BigDecimal amount;
    private String currency;
    private String lastFourDigits;
    private Instant processedAt;

    public RespuestaPagoPasarelaDTO() {
    }

    public String getTransactionId() {
        return this.transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public String getMerchantOrderId() {
        return this.merchantOrderId;
    }

    public void setMerchantOrderId(String merchantOrderId) {
        this.merchantOrderId = merchantOrderId;
    }

    public EstadoPasarela getStatus() {
        return this.status;
    }

    public void setStatus(EstadoPasarela status) {
        this.status = status;
    }

    public String getResponseCode() {
        return this.responseCode;
    }

    public void setResponseCode(String responseCode) {
        this.responseCode = responseCode;
    }

    public String getResponseMessage() {
        return this.responseMessage;
    }

    public void setResponseMessage(String responseMessage) {
        this.responseMessage = responseMessage;
    }

    public String getAuthorizationCode() {
        return this.authorizationCode;
    }

    public void setAuthorizationCode(String authorizationCode) {
        this.authorizationCode = authorizationCode;
    }

    public BigDecimal getAmount() {
        return this.amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getCurrency() {
        return this.currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getLastFourDigits() {
        return this.lastFourDigits;
    }

    public void setLastFourDigits(String lastFourDigits) {
        this.lastFourDigits = lastFourDigits;
    }

    public Instant getProcessedAt() {
        return this.processedAt;
    }

    public void setProcessedAt(Instant processedAt) {
        this.processedAt = processedAt;
    }
}

package com.sv.grupo1.ecommerce.gateway.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.math.BigDecimal;

/**
 * @author alexemestica
 * */
public record PaymentRequest (

    @NotBlank(message = "El ID de la orden del comercio es obligatorio")
    String merchantOrderId,

    @NotNull(message = "El monto es obligatorio")
    @DecimalMin(value = "0.50", message = "El monto minimo de transaccion es 0.50")
    @Digits(integer = 10, fraction = 2, message = "Formato de monto invalido")
    BigDecimal amount,

    @NotBlank(message = "La moneda es obligatoria")
    @Pattern(regexp = "^(USD|EUR)$", message = "Moneda no soportada (use USD o EUR)")
    String currency,

    @NotBlank(message = "El numero de tarjeta es obligatorio")
    @Pattern(regexp = "^[0-9]{16}$", message = "El numero de tarjeta debe contener exactamente 16 digitos")
    String cardNumber,

    @NotBlank(message = "El nombre del titular es obligatorio")
    String cardHolderName,

    @NotBlank(message = "La fecha de expiracion es obligatoria")
    @Pattern(regexp = "^(0[1-9]|1[0-2])/[0-9]{2}$", message = "Formato de expiracion invalido (MM/YY)")
    String cardExpirationDate,

    @NotBlank(message = "El CVV es obligatorio")
    @Pattern(regexp = "^[0-9]{3,4}$", message = "El CVV debe tener 3 o 4 digitos")
    String cvv
){}

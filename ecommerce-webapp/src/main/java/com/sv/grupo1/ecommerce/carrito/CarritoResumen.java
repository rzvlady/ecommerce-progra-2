package com.sv.grupo1.ecommerce.carrito;

import java.math.BigDecimal;

public class CarritoResumen {

    private final BigDecimal subtotal;
    private final BigDecimal envio;
    private final BigDecimal total;
    private final int cantidadTotal;

    public CarritoResumen(BigDecimal subtotal, BigDecimal envio, BigDecimal total, int cantidadTotal) {
        this.subtotal = subtotal;
        this.envio = envio;
        this.total = total;
        this.cantidadTotal = cantidadTotal;
    }

    public BigDecimal getSubtotal() { return subtotal; }
    public BigDecimal getEnvio() { return envio; }
    public BigDecimal getTotal() { return total; }
    public int getCantidadTotal() { return cantidadTotal; }
}

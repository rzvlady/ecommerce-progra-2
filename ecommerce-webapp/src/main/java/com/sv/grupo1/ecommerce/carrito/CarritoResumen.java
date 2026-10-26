package com.sv.grupo1.ecommerce.carrito;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class CarritoResumen {

    private final BigDecimal subtotal;
    private final BigDecimal envio;
    private final BigDecimal total;
    private final int cantidadTotal;
    private final BigDecimal envioGratisDesde;

    public CarritoResumen(BigDecimal subtotal, BigDecimal envio, BigDecimal total, int cantidadTotal) {
        this(subtotal, envio, total, cantidadTotal, BigDecimal.valueOf(50.00));
    }

    public CarritoResumen(BigDecimal subtotal, BigDecimal envio, BigDecimal total, int cantidadTotal, BigDecimal envioGratisDesde) {
        this.subtotal = subtotal != null ? subtotal : BigDecimal.ZERO;
        this.envio = envio != null ? envio : BigDecimal.ZERO;
        this.total = total != null ? total : BigDecimal.ZERO;
        this.cantidadTotal = cantidadTotal;
        this.envioGratisDesde = envioGratisDesde != null ? envioGratisDesde : BigDecimal.valueOf(50.00);
    }

    public BigDecimal getSubtotal() { return subtotal; }
    public BigDecimal getEnvio() { return envio; }
    public BigDecimal getTotal() { return total; }
    public int getCantidadTotal() { return cantidadTotal; }
    public BigDecimal getEnvioGratisDesde() { return envioGratisDesde; }

    public boolean isTieneEnvioGratis() {
        return cantidadTotal > 0 && envio.compareTo(BigDecimal.ZERO) == 0;
    }

    public BigDecimal getFaltanteParaEnvioGratis() {
        if (envioGratisDesde == null || subtotal == null) return BigDecimal.ZERO;
        BigDecimal diff = envioGratisDesde.subtract(subtotal);
        return diff.compareTo(BigDecimal.ZERO) > 0 ? diff.setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO;
    }

    public int getPorcentajeEnvioGratis() {
        if (envioGratisDesde == null || envioGratisDesde.compareTo(BigDecimal.ZERO) <= 0 || subtotal == null) {
            return 100;
        }
        if (subtotal.compareTo(envioGratisDesde) >= 0) {
            return 100;
        }
        return subtotal.multiply(BigDecimal.valueOf(100))
                .divide(envioGratisDesde, 0, RoundingMode.HALF_UP)
                .intValue();
    }
}

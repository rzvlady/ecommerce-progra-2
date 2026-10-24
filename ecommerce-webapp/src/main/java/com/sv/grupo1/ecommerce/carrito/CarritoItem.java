package com.sv.grupo1.ecommerce.carrito;

import java.io.Serializable;
import java.math.BigDecimal;

public class CarritoItem implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer idProducto;
    private String sku;
    private String nombre;
    private BigDecimal precioUnitario;
    private int cantidad;
    private int stockDisponible;

    public CarritoItem(Integer idProducto, String sku, String nombre,
                       BigDecimal precioUnitario, int cantidad, int stockDisponible) {
        this.idProducto = idProducto;
        this.sku = sku;
        this.nombre = nombre;
        this.precioUnitario = precioUnitario;
        this.cantidad = cantidad;
        this.stockDisponible = stockDisponible;
    }

    public BigDecimal getSubtotal() {
        return precioUnitario.multiply(BigDecimal.valueOf(cantidad));
    }

    public Integer getIdProducto() { return idProducto; }
    public String getSku() { return sku; }
    public String getNombre() { return nombre; }
    public BigDecimal getPrecioUnitario() { return precioUnitario; }
    public int getCantidad() { return cantidad; }
    public int getStockDisponible() { return stockDisponible; }

    public void setPrecioUnitario(BigDecimal precioUnitario) { this.precioUnitario = precioUnitario; }
    public void setCantidad(int cantidad) { this.cantidad = cantidad; }
    public void setStockDisponible(int stockDisponible) { this.stockDisponible = stockDisponible; }
}

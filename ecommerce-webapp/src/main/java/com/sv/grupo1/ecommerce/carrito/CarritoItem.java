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
    private String imagenUrl;

    public CarritoItem() {
    }

    public CarritoItem(Integer idProducto, String sku, String nombre,
                       BigDecimal precioUnitario, int cantidad, int stockDisponible) {
        this(idProducto, sku, nombre, precioUnitario, cantidad, stockDisponible, null);
    }

    public CarritoItem(Integer idProducto, String sku, String nombre,
                       BigDecimal precioUnitario, int cantidad, int stockDisponible, String imagenUrl) {
        this.idProducto = idProducto;
        this.sku = sku;
        this.nombre = nombre;
        this.precioUnitario = precioUnitario;
        this.cantidad = cantidad;
        this.stockDisponible = stockDisponible;
        this.imagenUrl = imagenUrl;
    }

    public BigDecimal getSubtotal() {
        return precioUnitario != null ? precioUnitario.multiply(BigDecimal.valueOf(cantidad)) : BigDecimal.ZERO;
    }

    public Integer getIdProducto() { return idProducto; }
    public String getSku() { return sku; }
    public String getNombre() { return nombre; }
    public BigDecimal getPrecioUnitario() { return precioUnitario; }
    public int getCantidad() { return cantidad; }
    public int getStockDisponible() { return stockDisponible; }
    public String getImagenUrl() { return imagenUrl; }

    public void setIdProducto(Integer idProducto) { this.idProducto = idProducto; }
    public void setSku(String sku) { this.sku = sku; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public void setPrecioUnitario(BigDecimal precioUnitario) { this.precioUnitario = precioUnitario; }
    public void setCantidad(int cantidad) { this.cantidad = cantidad; }
    public void setStockDisponible(int stockDisponible) { this.stockDisponible = stockDisponible; }
    public void setImagenUrl(String imagenUrl) { this.imagenUrl = imagenUrl; }
}

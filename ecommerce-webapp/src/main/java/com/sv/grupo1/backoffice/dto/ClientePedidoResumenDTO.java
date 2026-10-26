package com.sv.grupo1.backoffice.dto;

import java.math.BigDecimal;

public class ClientePedidoResumenDTO {

    private Integer idPedido;
    private String codigoPedido;
    private String fecha;
    private String estadoPedido;
    private String metodoPago;
    private BigDecimal total;
    private int articulos;

    public ClientePedidoResumenDTO() {
    }

    public ClientePedidoResumenDTO(Integer idPedido, String codigoPedido, String fecha, String estadoPedido, String metodoPago, BigDecimal total, int articulos) {
        this.idPedido = idPedido;
        this.codigoPedido = codigoPedido;
        this.fecha = fecha;
        this.estadoPedido = estadoPedido;
        this.metodoPago = metodoPago;
        this.total = total;
        this.articulos = articulos;
    }

    public Integer getIdPedido() {
        return idPedido;
    }

    public void setIdPedido(Integer idPedido) {
        this.idPedido = idPedido;
    }

    public String getCodigoPedido() {
        return codigoPedido;
    }

    public void setCodigoPedido(String codigoPedido) {
        this.codigoPedido = codigoPedido;
    }

    public String getFecha() {
        return fecha;
    }

    public void setFecha(String fecha) {
        this.fecha = fecha;
    }

    public String getEstadoPedido() {
        return estadoPedido;
    }

    public void setEstadoPedido(String estadoPedido) {
        this.estadoPedido = estadoPedido;
    }

    public String getMetodoPago() {
        return metodoPago;
    }

    public void setMetodoPago(String metodoPago) {
        this.metodoPago = metodoPago;
    }

    public BigDecimal getTotal() {
        return total != null ? total : BigDecimal.ZERO;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    public int getArticulos() {
        return articulos;
    }

    public void setArticulos(int articulos) {
        this.articulos = articulos;
    }
}

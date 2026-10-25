package com.sv.grupo1.ecommerce.dto;

import java.math.BigDecimal;

public class OrdenEcommerceDTO {

    private Integer idPedido;
    private String codigoOrden;
    private String fecha;
    private String tipoEntrega;
    private String metodoPago;
    private String estado;
    private String estadoClass;
    private BigDecimal total;
    private boolean confirmed;

    public OrdenEcommerceDTO() {
    }

    public Integer getIdPedido() {
        return idPedido;
    }

    public void setIdPedido(Integer idPedido) {
        this.idPedido = idPedido;
    }

    public String getCodigoOrden() {
        return codigoOrden;
    }

    public void setCodigoOrden(String codigoOrden) {
        this.codigoOrden = codigoOrden;
    }

    public String getFecha() {
        return fecha;
    }

    public void setFecha(String fecha) {
        this.fecha = fecha;
    }

    public String getTipoEntrega() {
        return tipoEntrega;
    }

    public void setTipoEntrega(String tipoEntrega) {
        this.tipoEntrega = tipoEntrega;
    }

    public String getMetodoPago() {
        return metodoPago;
    }

    public void setMetodoPago(String metodoPago) {
        this.metodoPago = metodoPago;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getEstadoClass() {
        return estadoClass;
    }

    public void setEstadoClass(String estadoClass) {
        this.estadoClass = estadoClass;
    }

    public BigDecimal getTotal() {
        return total != null ? total : BigDecimal.ZERO;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    public boolean isConfirmed() {
        return confirmed;
    }

    public void setConfirmed(boolean confirmed) {
        this.confirmed = confirmed;
    }
}

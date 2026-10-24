package com.sv.grupo1.ecommerce.dto;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class ResumenCompraDTO {

    private Integer idPedido;
    private List<ItemCompraDTO> items = new ArrayList<>();
    private BigDecimal total = BigDecimal.ZERO;
    private String direccionEntrega;
    private String referenciaPago;

    public ResumenCompraDTO() {
    }

    public Integer getIdPedido() {
        return this.idPedido;
    }

    public void setIdPedido(Integer idPedido) {
        this.idPedido = idPedido;
    }

    public List<ItemCompraDTO> getItems() {
        return this.items;
    }

    public void setItems(List<ItemCompraDTO> items) {
        this.items = items;
    }

    public BigDecimal getTotal() {
        return this.total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    public String getDireccionEntrega() {
        return this.direccionEntrega;
    }

    public void setDireccionEntrega(String direccionEntrega) {
        this.direccionEntrega = direccionEntrega;
    }

    public String getReferenciaPago() {
        return this.referenciaPago;
    }

    public void setReferenciaPago(String referenciaPago) {
        this.referenciaPago = referenciaPago;
    }
}

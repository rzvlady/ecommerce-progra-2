package com.sv.grupo1.ecommerce.dto;

public class ResultadoCompraDTO {

    private boolean aprobado;
    private Integer idPedido;
    private String mensaje;

    public ResultadoCompraDTO() {
    }

    public boolean isAprobado() {
        return this.aprobado;
    }

    public void setAprobado(boolean aprobado) {
        this.aprobado = aprobado;
    }

    public Integer getIdPedido() {
        return this.idPedido;
    }

    public void setIdPedido(Integer idPedido) {
        this.idPedido = idPedido;
    }

    public String getMensaje() {
        return this.mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }
}

package com.sv.grupo1.ecommerce.enums;

public enum EstadoPedido {

    PENDIENTE('P', "Pendiente"),
    CONFIRMADO('C', "Confirmado"),
    ENVIADO('E', "Enviado"),
    FINALIZADO('F', "Finalizado"),
    ANULADO('A', "Anulado");

    private final char codigoEstado;
    private final String estado;

    EstadoPedido(char codigoEstado, String estado) {
        this.codigoEstado = codigoEstado;
        this.estado = estado;
    }

    public char getCodigoEstado() {
        return codigoEstado;
    }

    public String getEstado() {
        return estado;
    }
}

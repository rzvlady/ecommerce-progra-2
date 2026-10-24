package com.sv.grupo1.ecommerce.enums;

public enum EstadoTransaccion {

    PENDIENTE('P', "Pendiente"),
    EXITOSO('E', "Exitoso"),
    RECHAZADO('R', "Rechazado"),
    REVERSADO('V', "Reversado");

    private final char codigoEstado;
    private final String estado;

    EstadoTransaccion(char codigoEstado, String estado) {
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

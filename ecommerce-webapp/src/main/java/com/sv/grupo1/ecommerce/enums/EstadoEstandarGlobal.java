package com.sv.grupo1.ecommerce.enums;

/*
 * @author alexemestica
 * */
public enum EstadoEstandarGlobal {

    ACTIVO('A', "Activo"),
    INACTIVO('I', "Inactivo"),
    ELIMINADO('E', "Eliminado");

    private final char codigoEstado;
    private final String estado;

    EstadoEstandarGlobal(char codigoEstado, String estado) {
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

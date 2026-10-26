package com.sv.grupo1.ecommerce.enums;

public enum TipoDireccionEnum {

    CASA('C', "Casa"),
    TRABAJO('T', "Trabajo");

    private final char codigo;
    private final String descripcion;

    TipoDireccionEnum(char codigo, String descripcion) {
        this.codigo = codigo;
        this.descripcion = descripcion;
    }

    public char getCodigo() {
        return codigo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public static TipoDireccionEnum fromCodigo(Character codigo) {
        if (codigo == null) {
            return null;
        }
        for (TipoDireccionEnum tipo : values()) {
            if (tipo.codigo == Character.toUpperCase(codigo)) {
                return tipo;
            }
        }
        return null;
    }
}

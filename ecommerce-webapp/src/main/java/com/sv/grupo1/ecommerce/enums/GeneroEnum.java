package com.sv.grupo1.ecommerce.enums;

public enum GeneroEnum {

    MASCULINO('M', "Masculino"),
    FEMENINO('F', "Femenino");

    private final char codigo;
    private final String descripcion;

    GeneroEnum(char codigo, String descripcion) {
        this.codigo = codigo;
        this.descripcion = descripcion;
    }

    public char getCodigo() {
        return codigo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public static GeneroEnum fromCodigo(Character codigo) {
        if (codigo == null) {
            return null;
        }
        for (GeneroEnum genero : values()) {
            if (genero.codigo == Character.toUpperCase(codigo)) {
                return genero;
            }
        }
        return null;
    }
}

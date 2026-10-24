package com.sv.grupo1.ecommerce.enums;

public enum TipoContactoEnum {

    CORREO('C', "Correo Electrónico"),
    MOVIL('M', "Dispositivo Móvil");

    private final char codigo;
    private final String descripcion;

    TipoContactoEnum(char codigo, String descripcion) {
        this.codigo = codigo;
        this.descripcion = descripcion;
    }

    public char getCodigo() {
        return codigo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public static TipoContactoEnum fromCodigo(Character codigo) {
        if (codigo == null) {
            return null;
        }
        for (TipoContactoEnum tipo : values()) {
            if (tipo.codigo == Character.toUpperCase(codigo)) {
                return tipo;
            }
        }
        return null;
    }
}

package com.sv.grupo1.ecommerce.utils;

import com.sv.grupo1.ecommerce.enums.TipoContactoEnum;

public class Constantes {

    public static final Character TIPO_CONTACTO_CORREO      = TipoContactoEnum.CORREO.getCodigo();
    public static final Character TIPO_CONTACTO_TELEFONO    = 'T';
    public static final Character TIPO_CONTACTO_MOVIL       = TipoContactoEnum.MOVIL.getCodigo();

    private Constantes() {
        /* Constructor Privado */
    }
}

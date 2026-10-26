package com.sv.grupo1.ecommerce.entities.seguridad;

import jakarta.persistence.Entity;
import jakarta.persistence.DiscriminatorValue;

//@Entity
//@DiscriminatorValue("ADMINISTRADOR")
public class Administrador extends Usuario {

    public Administrador() {
        super();
    }
}
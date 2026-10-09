package com.sv.grupo1.ecommerce.entities.seguridad;

import jakarta.persistence.Entity;
import jakarta.persistence.DiscriminatorValue;

//@Entity
//@DiscriminatorValue("CLIENTE")
public class Cliente extends Usuario {

    public Cliente() {
        super();
    }
}
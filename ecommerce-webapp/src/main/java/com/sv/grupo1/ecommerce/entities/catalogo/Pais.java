package com.sv.grupo1.ecommerce.entities.catalogo;

import jakarta.persistence.*;

@Entity
@Table(name = "paises", schema = "catalogo")
public class Pais {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_pais")
    private Integer idPais;

    @Column(name = "nombre_pais", nullable = false)
    private String nombrePais;

    public Pais() {}

    // Getters y Setters
    public Integer getIdPais() { return idPais; }
    public void setIdPais(Integer idPais) { this.idPais = idPais; }
    public String getNombrePais() { return nombrePais; }
    public void setNombrePais(String nombrePais) { this.nombrePais = nombrePais; }
}

package com.sv.grupo1.ecommerce.entities.catalogo;

import jakarta.persistence.*;

@Entity
@Table(name = "municipios", schema = "catalogo")
public class Municipio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_municipio")
    private Integer idMunicipio;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_departamento", nullable = false)
    private Departamento departamento;

    @Column(name = "nombre_municipio", nullable = false)
    private String nombreMunicipio;

    public Municipio() {}

    // Getters y Setters
    public Integer getIdMunicipio() { return idMunicipio; }
    public void setIdMunicipio(Integer idMunicipio) { this.idMunicipio = idMunicipio; }
    public Departamento getDepartamento() { return departamento; }
    public void setDepartamento(Departamento departamento) { this.departamento = departamento; }
    public String getNombreMunicipio() { return nombreMunicipio; }
    public void setNombreMunicipio(String nombreMunicipio) { this.nombreMunicipio = nombreMunicipio; }
}

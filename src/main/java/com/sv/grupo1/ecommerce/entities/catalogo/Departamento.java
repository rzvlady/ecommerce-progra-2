package com.sv.grupo1.ecommerce.entities.catalogo;

import jakarta.persistence.*;

@Entity
@Table(name = "departamentos", schema = "catalogo")
public class Departamento {

    @Id
    @Column(name = "id_departamento")
    private Integer idDepartamento;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_pais", nullable = false)
    private Pais pais;

    @Column(name = "nombre_departamento", nullable = false)
    private String nombreDepartamento;

    public Departamento() {}

    // Getters y Setters
    public Integer getIdDepartamento() { return idDepartamento; }
    public void setIdDepartamento(Integer idDepartamento) { this.idDepartamento = idDepartamento; }
    public Pais getPais() { return pais; }
    public void setPais(Pais pais) { this.pais = pais; }
    public String getNombreDepartamento() { return nombreDepartamento; }
    public void setNombreDepartamento(String nombreDepartamento) { this.nombreDepartamento = nombreDepartamento; }
}
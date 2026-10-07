package com.sv.grupo1.ecommerce.entities.catalogo;

import jakarta.persistence.*;

import java.util.List;

@Entity
@Table(name = "paises", schema = "catalogo")
public class Pais {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_pais")
    private Integer idPais;

    @Column(name = "nombre_pais", nullable = false)
    private String nombrePais;

    @OneToMany(mappedBy = "pais", fetch = FetchType.LAZY)
    private List<Departamento> departamentos;

    public Pais() {
        /* Constructor Vacio */
    }

    public Integer getIdPais() {
        return idPais;
    }

    public void setIdPais(Integer idPais) {
        this.idPais = idPais;
    }

    public String getNombrePais() {
        return nombrePais;
    }

    public void setNombrePais(String nombrePais) {
        this.nombrePais = nombrePais;
    }

    public List<Departamento> getDepartamentos() {
        return departamentos;
    }

    public void setDepartamentos(List<Departamento> departamentos) {
        this.departamentos = departamentos;
    }
}

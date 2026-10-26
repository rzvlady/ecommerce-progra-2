package com.sv.grupo1.ecommerce.dto;

import java.util.ArrayList;
import java.util.List;

public class CategoriaNodoDTO {

    private Integer idCategoria;
    private String nombreCategoria;
    private Integer idCategoriaPadre;
    private String nombreCategoriaPadre;
    private List<CategoriaNodoDTO> subcategorias = new ArrayList<>();

    public CategoriaNodoDTO() {
    }

    public CategoriaNodoDTO(Integer idCategoria, String nombreCategoria) {
        this.idCategoria = idCategoria;
        this.nombreCategoria = nombreCategoria;
    }

    public Integer getIdCategoria() {
        return idCategoria;
    }

    public void setIdCategoria(Integer idCategoria) {
        this.idCategoria = idCategoria;
    }

    public String getNombreCategoria() {
        return nombreCategoria;
    }

    public void setNombreCategoria(String nombreCategoria) {
        this.nombreCategoria = nombreCategoria;
    }

    public Integer getIdCategoriaPadre() {
        return idCategoriaPadre;
    }

    public void setIdCategoriaPadre(Integer idCategoriaPadre) {
        this.idCategoriaPadre = idCategoriaPadre;
    }

    public String getNombreCategoriaPadre() {
        return nombreCategoriaPadre;
    }

    public void setNombreCategoriaPadre(String nombreCategoriaPadre) {
        this.nombreCategoriaPadre = nombreCategoriaPadre;
    }

    public boolean isTieneSubcategorias() {
        return subcategorias != null && !subcategorias.isEmpty();
    }

    public List<CategoriaNodoDTO> getSubcategorias() {
        return subcategorias;
    }

    public void setSubcategorias(List<CategoriaNodoDTO> subcategorias) {
        this.subcategorias = subcategorias;
    }
}

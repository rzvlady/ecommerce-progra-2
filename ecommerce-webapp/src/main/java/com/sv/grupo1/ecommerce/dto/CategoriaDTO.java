package com.sv.grupo1.ecommerce.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.OffsetDateTime;

public class CategoriaDTO {

    private Integer idCategoria;

    @NotBlank(message = "El nombre de la categoría es obligatorio")
    @Size(max = 100, message = "El nombre de la categoría no puede exceder los 100 caracteres")
    private String nombreCategoria;

    @Size(max = 255, message = "La descripción no puede exceder los 255 caracteres")
    private String descripcion;
    private Integer idCategoriaPadre;
    
    private String nombreCategoriaPadre;
    private Integer nivel;
    private String rutaJerarquica;
    private Character estado = 'A';
    private OffsetDateTime fechaCreacion;
    private long cantidadSubcategorias;
    private long cantidadProductos;
    private int profundidadDescendientes;

    public CategoriaDTO() {
    }
    // Getters y Setters
    public Integer getIdCategoria() {return this.idCategoria;}
    public void setIdCategoria(Integer idCategoria) {this.idCategoria = idCategoria;}
    public String getNombreCategoria() {return this.nombreCategoria;}
    public void setNombreCategoria(String nombreCategoria) {this.nombreCategoria = nombreCategoria;}
    public String getDescripcion() {return this.descripcion;}
    public void setDescripcion(String descripcion) {this.descripcion = descripcion;}
    public Integer getIdCategoriaPadre() {return this.idCategoriaPadre;}
    public void setIdCategoriaPadre(Integer idCategoriaPadre) {this.idCategoriaPadre = idCategoriaPadre;}
    public String getNombreCategoriaPadre() {return this.nombreCategoriaPadre;}
    public void setNombreCategoriaPadre(String nombreCategoriaPadre) {this.nombreCategoriaPadre = nombreCategoriaPadre;}
    public Integer getNivel() {return this.nivel;}
    public void setNivel(Integer nivel) {this.nivel = nivel;}
    public String getRutaJerarquica() {return this.rutaJerarquica;}
    public void setRutaJerarquica(String rutaJerarquica) {this.rutaJerarquica = rutaJerarquica;}
    public Character getEstado() {return this.estado;}
    public void setEstado(Character estado) {this.estado = estado;}
    public OffsetDateTime getFechaCreacion() {return this.fechaCreacion;}
    public void setFechaCreacion(OffsetDateTime fechaCreacion) {this.fechaCreacion = fechaCreacion;}
    public long getCantidadSubcategorias() {return this.cantidadSubcategorias;}
    public void setCantidadSubcategorias(long cantidadSubcategorias) {this.cantidadSubcategorias = cantidadSubcategorias;}
    public long getCantidadProductos() {return this.cantidadProductos;}
    public void setCantidadProductos(long cantidadProductos) {this.cantidadProductos = cantidadProductos;}
    public int getProfundidadDescendientes() {return this.profundidadDescendientes;}
    public void setProfundidadDescendientes(int profundidadDescendientes) {this.profundidadDescendientes = profundidadDescendientes;}




}

package com.sv.grupo1.ecommerce.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.OffsetDateTime;

public class RolDTO {

    private Integer idRol;

    @NotBlank(message = "El nombre del rol es obligatorio")
    @Size(max = 50, message = "El nombre del rol no puede exceder los 50 caracteres")
    private String nombre;

    @Size(max = 255, message = "La descripción no puede exceder los 255 caracteres")
    private String descripcion;

    private OffsetDateTime fechaCreacion;
    private long cantidadUsuarios;
    private boolean rolSistema;

    public RolDTO() {
    }

    public Integer getIdRol() {
        return idRol;
    }

    public void setIdRol(Integer idRol) {
        this.idRol = idRol;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public OffsetDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(OffsetDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public long getCantidadUsuarios() {
        return cantidadUsuarios;
    }

    public void setCantidadUsuarios(long cantidadUsuarios) {
        this.cantidadUsuarios = cantidadUsuarios;
    }

    public boolean isRolSistema() {
        return rolSistema;
    }

    public void setRolSistema(boolean rolSistema) {
        this.rolSistema = rolSistema;
    }
}

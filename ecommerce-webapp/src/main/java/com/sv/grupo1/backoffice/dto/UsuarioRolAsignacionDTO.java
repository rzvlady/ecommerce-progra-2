package com.sv.grupo1.backoffice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.OffsetDateTime;

public class UsuarioRolAsignacionDTO {

    @NotNull(message = "El usuario es obligatorio")
    private Integer idUsuario;

    @NotNull(message = "Debe seleccionar el nuevo rol a asignar")
    private Integer idRol;

    @NotBlank(message = "Debe ingresar el motivo o justificación del cambio de rol")
    @Size(max = 255, message = "El motivo no puede exceder los 255 caracteres")
    private String motivo;

    // Campos informativos para la tabla y vista previa en el modal
    private String correoInicioSesion;
    private String nombreCompleto;
    private boolean tienePerfil;
    private Character estadoUsuario;
    private OffsetDateTime ultimoInicioSesion;
    private String nombreRolActual;
    private OffsetDateTime fechaAsignacionRol;
    private String asignadoPor;
    private String rolAnteriorAuditoria;
    private String motivoUltimaAuditoria;

    public UsuarioRolAsignacionDTO() {
    }

    public Integer getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(Integer idUsuario) {
        this.idUsuario = idUsuario;
    }

    public Integer getIdRol() {
        return idRol;
    }

    public void setIdRol(Integer idRol) {
        this.idRol = idRol;
    }

    public String getMotivo() {
        return motivo;
    }

    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }

    public String getCorreoInicioSesion() {
        return correoInicioSesion;
    }

    public void setCorreoInicioSesion(String correoInicioSesion) {
        this.correoInicioSesion = correoInicioSesion;
    }

    public String getNombreCompleto() {
        return nombreCompleto;
    }

    public void setNombreCompleto(String nombreCompleto) {
        this.nombreCompleto = nombreCompleto;
    }

    public boolean isTienePerfil() {
        return tienePerfil;
    }

    public void setTienePerfil(boolean tienePerfil) {
        this.tienePerfil = tienePerfil;
    }

    public Character getEstadoUsuario() {
        return estadoUsuario;
    }

    public void setEstadoUsuario(Character estadoUsuario) {
        this.estadoUsuario = estadoUsuario;
    }

    public OffsetDateTime getUltimoInicioSesion() {
        return ultimoInicioSesion;
    }

    public void setUltimoInicioSesion(OffsetDateTime ultimoInicioSesion) {
        this.ultimoInicioSesion = ultimoInicioSesion;
    }

    public String getNombreRolActual() {
        return nombreRolActual;
    }

    public void setNombreRolActual(String nombreRolActual) {
        this.nombreRolActual = nombreRolActual;
    }

    public OffsetDateTime getFechaAsignacionRol() {
        return fechaAsignacionRol;
    }

    public void setFechaAsignacionRol(OffsetDateTime fechaAsignacionRol) {
        this.fechaAsignacionRol = fechaAsignacionRol;
    }

    public String getAsignadoPor() {
        return asignadoPor;
    }

    public void setAsignadoPor(String asignadoPor) {
        this.asignadoPor = asignadoPor;
    }

    public String getRolAnteriorAuditoria() {
        return rolAnteriorAuditoria;
    }

    public void setRolAnteriorAuditoria(String rolAnteriorAuditoria) {
        this.rolAnteriorAuditoria = rolAnteriorAuditoria;
    }

    public String getMotivoUltimaAuditoria() {
        return motivoUltimaAuditoria;
    }

    public void setMotivoUltimaAuditoria(String motivoUltimaAuditoria) {
        this.motivoUltimaAuditoria = motivoUltimaAuditoria;
    }
}

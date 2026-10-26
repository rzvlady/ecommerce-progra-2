package com.sv.grupo1.ecommerce.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

public class UsuarioPerfilCompletoDTO {

    private Integer idUsuario;

    // --- SECCIÓN 1: PERFIL PERSONAL (usuarios_perfil) ---
    private Integer idPerfil;

    @NotBlank(message = "Los nombres son obligatorios.")
    @Size(max = 100, message = "Los nombres no pueden exceder 100 caracteres.")
    private String nombres;

    @NotBlank(message = "Los apellidos son obligatorios.")
    @Size(max = 100, message = "Los apellidos no pueden exceder 100 caracteres.")
    private String apellidos;

    @NotBlank(message = "El número de DUI es obligatorio.")
    @Pattern(regexp = "^\\d{8}-\\d{1}$", message = "El DUI debe tener el formato válido 00000000-0.")
    private String dui;

    @NotNull(message = "Debe seleccionar un género.")
    private Character genero;

    @NotNull(message = "La fecha de nacimiento es obligatoria.")
    @Past(message = "La fecha de nacimiento debe ser una fecha pasada.")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate fechaNacimiento;

    // --- SECCIÓN 2: CONTACTO (usuarios_contacto) ---
    private Integer idContactoCorreo;
    private String correoPrincipal;
    private Boolean correoVerificado;

    private Integer idContactoMovil;

    @NotBlank(message = "El número de dispositivo móvil es obligatorio.")
    @Pattern(regexp = "^\\d{4}-?\\d{4}$", message = "El teléfono móvil debe tener 8 dígitos (ej. 7000-0000).")
    private String telefonoMovil;

    private Boolean tieneWhatsapp = true;

    // --- SECCIÓN 3: DIRECCIÓN PRINCIPAL (usuarios_direccion) ---
    private Integer idDireccion;

    @NotNull(message = "Debe seleccionar un departamento.")
    private Integer idDepartamento;

    @NotNull(message = "Debe seleccionar un municipio.")
    private Integer idMunicipio;

    @NotNull(message = "Debe seleccionar el tipo de dirección.")
    private Character tipoDireccion;

    @NotBlank(message = "La dirección exacta (Línea 1) es obligatoria.")
    @Size(max = 255, message = "La dirección no puede exceder 255 caracteres.")
    private String direccionLinea1;

    @Size(max = 10, message = "El código postal no puede exceder 10 caracteres.")
    private String codigoPostal;

    @Size(max = 255, message = "El punto de referencia no puede exceder 255 caracteres.")
    private String puntoReferencia;

    // Metadatos informativos de solo lectura para mostrar en la vista
    private String ultimaActualizacion;
    private boolean perfilCompletado;

    public UsuarioPerfilCompletoDTO() {
        /* Constructor Vacío */
    }

    public Integer getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(Integer idUsuario) {
        this.idUsuario = idUsuario;
    }

    public Integer getIdPerfil() {
        return idPerfil;
    }

    public void setIdPerfil(Integer idPerfil) {
        this.idPerfil = idPerfil;
    }

    public String getNombres() {
        return nombres;
    }

    public void setNombres(String nombres) {
        this.nombres = nombres;
    }

    public String getApellidos() {
        return apellidos;
    }

    public void setApellidos(String apellidos) {
        this.apellidos = apellidos;
    }

    public String getDui() {
        return dui;
    }

    public void setDui(String dui) {
        this.dui = dui;
    }

    public Character getGenero() {
        return genero;
    }

    public void setGenero(Character genero) {
        this.genero = genero;
    }

    public LocalDate getFechaNacimiento() {
        return fechaNacimiento;
    }

    public void setFechaNacimiento(LocalDate fechaNacimiento) {
        this.fechaNacimiento = fechaNacimiento;
    }

    public Integer getIdContactoCorreo() {
        return idContactoCorreo;
    }

    public void setIdContactoCorreo(Integer idContactoCorreo) {
        this.idContactoCorreo = idContactoCorreo;
    }

    public String getCorreoPrincipal() {
        return correoPrincipal;
    }

    public void setCorreoPrincipal(String correoPrincipal) {
        this.correoPrincipal = correoPrincipal;
    }

    public Boolean getCorreoVerificado() {
        return correoVerificado;
    }

    public void setCorreoVerificado(Boolean correoVerificado) {
        this.correoVerificado = correoVerificado;
    }

    public Integer getIdContactoMovil() {
        return idContactoMovil;
    }

    public void setIdContactoMovil(Integer idContactoMovil) {
        this.idContactoMovil = idContactoMovil;
    }

    public String getTelefonoMovil() {
        return telefonoMovil;
    }

    public void setTelefonoMovil(String telefonoMovil) {
        this.telefonoMovil = telefonoMovil;
    }

    public Boolean getTieneWhatsapp() {
        return tieneWhatsapp;
    }

    public void setTieneWhatsapp(Boolean tieneWhatsapp) {
        this.tieneWhatsapp = tieneWhatsapp;
    }

    public Integer getIdDireccion() {
        return idDireccion;
    }

    public void setIdDireccion(Integer idDireccion) {
        this.idDireccion = idDireccion;
    }

    public Integer getIdDepartamento() {
        return idDepartamento;
    }

    public void setIdDepartamento(Integer idDepartamento) {
        this.idDepartamento = idDepartamento;
    }

    public Integer getIdMunicipio() {
        return idMunicipio;
    }

    public void setIdMunicipio(Integer idMunicipio) {
        this.idMunicipio = idMunicipio;
    }

    public Character getTipoDireccion() {
        return tipoDireccion;
    }

    public void setTipoDireccion(Character tipoDireccion) {
        this.tipoDireccion = tipoDireccion;
    }

    public String getDireccionLinea1() {
        return direccionLinea1;
    }

    public void setDireccionLinea1(String direccionLinea1) {
        this.direccionLinea1 = direccionLinea1;
    }

    public String getCodigoPostal() {
        return codigoPostal;
    }

    public void setCodigoPostal(String codigoPostal) {
        this.codigoPostal = codigoPostal;
    }

    public String getPuntoReferencia() {
        return puntoReferencia;
    }

    public void setPuntoReferencia(String puntoReferencia) {
        this.puntoReferencia = puntoReferencia;
    }

    public String getUltimaActualizacion() {
        return ultimaActualizacion;
    }

    public void setUltimaActualizacion(String ultimaActualizacion) {
        this.ultimaActualizacion = ultimaActualizacion;
    }

    public boolean isPerfilCompletado() {
        return perfilCompletado;
    }

    public void setPerfilCompletado(boolean perfilCompletado) {
        this.perfilCompletado = perfilCompletado;
    }
}

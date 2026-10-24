package com.sv.grupo1.backoffice.dto;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class ClienteBackofficeDTO {

    private Integer idUsuario;
    private String correo;
    private String nombres;
    private String apellidos;
    private String nombreCompleto;
    private String dui;
    private String telefono;
    private Boolean tieneWhatsapp;
    private Character genero;
    private String fechaNacimiento;
    private Character estado;
    private String estadoDescripcion;
    private String fechaRegistro;
    private String ultimoInicioSesion;
    private String direccionPrincipal;
    private String departamento;
    private String municipio;
    private int totalPedidos;
    private BigDecimal totalGastado;
    private String notasInternas;

    private List<ClientePedidoResumenDTO> pedidos = new ArrayList<>();
    private List<ClienteDireccionResumenDTO> direcciones = new ArrayList<>();

    public ClienteBackofficeDTO() {
    }

    public Integer getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(Integer idUsuario) {
        this.idUsuario = idUsuario;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
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

    public String getNombreCompleto() {
        if (nombreCompleto != null && !nombreCompleto.isBlank()) {
            return nombreCompleto;
        }
        String n = nombres != null ? nombres.trim() : "";
        String a = apellidos != null ? apellidos.trim() : "";
        String res = (n + " " + a).trim();
        return res.isEmpty() ? (correo != null ? correo : "Sin nombre") : res;
    }

    public void setNombreCompleto(String nombreCompleto) {
        this.nombreCompleto = nombreCompleto;
    }

    public String getDui() {
        return dui;
    }

    public void setDui(String dui) {
        this.dui = dui;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public Boolean getTieneWhatsapp() {
        return tieneWhatsapp;
    }

    public void setTieneWhatsapp(Boolean tieneWhatsapp) {
        this.tieneWhatsapp = tieneWhatsapp;
    }

    public Character getGenero() {
        return genero;
    }

    public void setGenero(Character genero) {
        this.genero = genero;
    }

    public String getFechaNacimiento() {
        return fechaNacimiento;
    }

    public void setFechaNacimiento(String fechaNacimiento) {
        this.fechaNacimiento = fechaNacimiento;
    }

    public Character getEstado() {
        return estado;
    }

    public void setEstado(Character estado) {
        this.estado = estado;
        if (estado != null) {
            if (estado == 'A' || estado == 'a') {
                this.estadoDescripcion = "Activo";
            } else if (estado == 'B' || estado == 'b') {
                this.estadoDescripcion = "Suspendido";
            } else {
                this.estadoDescripcion = "Inactivo";
            }
        } else {
            this.estadoDescripcion = "Inactivo";
        }
    }

    public String getEstadoDescripcion() {
        return estadoDescripcion;
    }

    public void setEstadoDescripcion(String estadoDescripcion) {
        this.estadoDescripcion = estadoDescripcion;
    }

    public String getFechaRegistro() {
        return fechaRegistro;
    }

    public void setFechaRegistro(String fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
    }

    public String getUltimoInicioSesion() {
        return ultimoInicioSesion;
    }

    public void setUltimoInicioSesion(String ultimoInicioSesion) {
        this.ultimoInicioSesion = ultimoInicioSesion;
    }

    public String getDireccionPrincipal() {
        return direccionPrincipal;
    }

    public void setDireccionPrincipal(String direccionPrincipal) {
        this.direccionPrincipal = direccionPrincipal;
    }

    public String getDepartamento() {
        return departamento;
    }

    public void setDepartamento(String departamento) {
        this.departamento = departamento;
    }

    public String getMunicipio() {
        return municipio;
    }

    public void setMunicipio(String municipio) {
        this.municipio = municipio;
    }

    public int getTotalPedidos() {
        return totalPedidos;
    }

    public void setTotalPedidos(int totalPedidos) {
        this.totalPedidos = totalPedidos;
    }

    public BigDecimal getTotalGastado() {
        return totalGastado != null ? totalGastado : BigDecimal.ZERO;
    }

    public void setTotalGastado(BigDecimal totalGastado) {
        this.totalGastado = totalGastado;
    }

    public String getNotasInternas() {
        return notasInternas;
    }

    public void setNotasInternas(String notasInternas) {
        this.notasInternas = notasInternas;
    }

    public List<ClientePedidoResumenDTO> getPedidos() {
        return pedidos;
    }

    public void setPedidos(List<ClientePedidoResumenDTO> pedidos) {
        this.pedidos = pedidos;
    }

    public List<ClienteDireccionResumenDTO> getDirecciones() {
        return direcciones;
    }

    public void setDirecciones(List<ClienteDireccionResumenDTO> direcciones) {
        this.direcciones = direcciones;
    }

    public String getIniciales() {
        String n = nombres != null ? nombres.trim() : "";
        String a = apellidos != null ? apellidos.trim() : "";
        if (!n.isEmpty() && !a.isEmpty()) {
            return ("" + n.charAt(0) + a.charAt(0)).toUpperCase();
        } else if (!n.isEmpty()) {
            return ("" + n.charAt(0)).toUpperCase();
        } else if (correo != null && !correo.isBlank()) {
            return ("" + correo.charAt(0)).toUpperCase();
        }
        return "CL";
    }
}

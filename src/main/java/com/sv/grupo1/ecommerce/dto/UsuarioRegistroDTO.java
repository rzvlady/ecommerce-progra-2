package com.sv.grupo1.ecommerce.dto;

public class UsuarioRegistroDTO {

    private String correoInicioSesion;
    private String contrasenia;

    public UsuarioRegistroDTO() {
    }

    public String getCorreoInicioSesion() {
        return correoInicioSesion;
    }

    public void setCorreoInicioSesion(String correoInicioSesion) {
        this.correoInicioSesion = correoInicioSesion;
    }

    public String getContrasenia() {
        return contrasenia;
    }

    public void setContrasenia(String contrasenia) {
        this.contrasenia = contrasenia;
    }
}

package com.sv.grupo1.ecommerce.dto;

/*
* @author ivandiaz-007
*/
public class RegistroUsuarioDTO {

    private String correoInicioSesion;
    private String contrasenia;
    private String confirmarContrasenia;

    public RegistroUsuarioDTO() {
        /* Constructor Vacio */
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

    public String getConfirmarContrasenia() {
        return confirmarContrasenia;
    }

    public void setConfirmarContrasenia(String confirmarContrasenia) {
        this.confirmarContrasenia = confirmarContrasenia;
    }
}

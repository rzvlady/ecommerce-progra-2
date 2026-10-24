package com.sv.grupo1.ecommerce.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class CheckoutDTO {

    private Integer idDireccion;

    @Size(max = 150, message = "La dirección no puede superar los 150 caracteres")
    private String direccionLinea1;

    private Integer idMunicipio;

    @Pattern(regexp = "^$|^[0-9]{4,10}$", message = "El código postal debe tener entre 4 y 10 dígitos")
    private String codigoPostal;

    @NotBlank(message = "El nombre del titular es obligatorio")
    @Pattern(regexp = "^$|^\\p{L}[\\p{L} .'-]{2,99}$",
            message = "El nombre del titular debe tener entre 3 y 100 letras, sin números")
    private String nombreTitular;

    @NotBlank(message = "El número de tarjeta es obligatorio")
    @Pattern(regexp = "^[0-9]{16}$", message = "El número de tarjeta debe tener 16 dígitos")
    private String numeroTarjeta;

    @NotBlank(message = "La fecha de expiración es obligatoria")
    @Pattern(regexp = "^(0[1-9]|1[0-2])/[0-9]{2}$", message = "La fecha de expiración debe tener el formato MM/AA")
    private String fechaExpiracion;

    @NotBlank(message = "El CVV es obligatorio")
    @Pattern(regexp = "^[0-9]{3,4}$", message = "El CVV debe tener 3 o 4 dígitos")
    private String cvv;

    public CheckoutDTO() {
    }

    public Integer getIdDireccion() {
        return this.idDireccion;
    }

    public void setIdDireccion(Integer idDireccion) {
        this.idDireccion = idDireccion;
    }

    public String getDireccionLinea1() {
        return this.direccionLinea1;
    }

    public void setDireccionLinea1(String direccionLinea1) {
        this.direccionLinea1 = direccionLinea1;
    }

    public Integer getIdMunicipio() {
        return this.idMunicipio;
    }

    public void setIdMunicipio(Integer idMunicipio) {
        this.idMunicipio = idMunicipio;
    }

    public String getCodigoPostal() {
        return this.codigoPostal;
    }

    public void setCodigoPostal(String codigoPostal) {
        this.codigoPostal = codigoPostal;
    }

    public String getNombreTitular() {
        return this.nombreTitular;
    }

    public void setNombreTitular(String nombreTitular) {
        this.nombreTitular = nombreTitular;
    }

    public String getNumeroTarjeta() {
        return this.numeroTarjeta;
    }

    public void setNumeroTarjeta(String numeroTarjeta) {
        this.numeroTarjeta = numeroTarjeta == null ? null : numeroTarjeta.replaceAll("[\\s-]", "");
    }

    public String getFechaExpiracion() {
        return this.fechaExpiracion;
    }

    public void setFechaExpiracion(String fechaExpiracion) {
        this.fechaExpiracion = fechaExpiracion;
    }

    public String getCvv() {
        return this.cvv;
    }

    public void setCvv(String cvv) {
        this.cvv = cvv;
    }
}

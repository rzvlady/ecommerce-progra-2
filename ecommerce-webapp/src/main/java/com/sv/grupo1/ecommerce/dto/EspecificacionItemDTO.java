package com.sv.grupo1.ecommerce.dto;

public class EspecificacionItemDTO {

    private String clave;
    private String etiqueta;
    private String valor;
    private String icono;

    public EspecificacionItemDTO() {
    }

    public EspecificacionItemDTO(String clave, String etiqueta, String valor, String icono) {
        this.clave = clave;
        this.etiqueta = etiqueta;
        this.valor = valor;
        this.icono = icono;
    }

    public String getClave() {
        return clave;
    }

    public void setClave(String clave) {
        this.clave = clave;
    }

    public String getEtiqueta() {
        return etiqueta;
    }

    public void setEtiqueta(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    public String getValor() {
        return valor;
    }

    public void setValor(String valor) {
        this.valor = valor;
    }

    public String getIcono() {
        return icono;
    }

    public void setIcono(String icono) {
        this.icono = icono;
    }
}

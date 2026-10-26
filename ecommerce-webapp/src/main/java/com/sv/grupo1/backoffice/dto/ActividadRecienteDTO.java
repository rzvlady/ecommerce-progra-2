package com.sv.grupo1.backoffice.dto;

import java.time.OffsetDateTime;

public class ActividadRecienteDTO {

    private String tipo; // "PEDIDO" o "INVENTARIO"
    private String titulo;
    private String subtitulo;
    private String detalle;
    private String badgeTexto;
    private String badgeClass;
    private String icono;
    private String iconoColor;
    private String enlaceUrl;
    private String enlaceTexto;
    private OffsetDateTime timestamp;
    private String tiempoRelativo;

    public ActividadRecienteDTO() {}

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getSubtitulo() {
        return subtitulo;
    }

    public void setSubtitulo(String subtitulo) {
        this.subtitulo = subtitulo;
    }

    public String getDetalle() {
        return detalle;
    }

    public void setDetalle(String detalle) {
        this.detalle = detalle;
    }

    public String getBadgeTexto() {
        return badgeTexto;
    }

    public void setBadgeTexto(String badgeTexto) {
        this.badgeTexto = badgeTexto;
    }

    public String getBadgeClass() {
        return badgeClass;
    }

    public void setBadgeClass(String badgeClass) {
        this.badgeClass = badgeClass;
    }

    public String getIcono() {
        return icono;
    }

    public void setIcono(String icono) {
        this.icono = icono;
    }

    public String getIconoColor() {
        return iconoColor;
    }

    public void setIconoColor(String iconoColor) {
        this.iconoColor = iconoColor;
    }

    public String getEnlaceUrl() {
        return enlaceUrl;
    }

    public void setEnlaceUrl(String enlaceUrl) {
        this.enlaceUrl = enlaceUrl;
    }

    public String getEnlaceTexto() {
        return enlaceTexto;
    }

    public void setEnlaceTexto(String enlaceTexto) {
        this.enlaceTexto = enlaceTexto;
    }

    public OffsetDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(OffsetDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getTiempoRelativo() {
        return tiempoRelativo;
    }

    public void setTiempoRelativo(String tiempoRelativo) {
        this.tiempoRelativo = tiempoRelativo;
    }
}

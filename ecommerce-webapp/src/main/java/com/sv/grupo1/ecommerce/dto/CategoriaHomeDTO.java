package com.sv.grupo1.ecommerce.dto;

public class CategoriaHomeDTO {

    private Integer idCategoria;
    private String nombreCategoria;
    private String subtitulo;
    private String imagenUrl;
    private String enlace;

    public CategoriaHomeDTO() {
    }

    public CategoriaHomeDTO(Integer idCategoria, String nombreCategoria, String subtitulo, String imagenUrl, String enlace) {
        this.idCategoria = idCategoria;
        this.nombreCategoria = nombreCategoria;
        this.subtitulo = subtitulo;
        this.imagenUrl = imagenUrl;
        this.enlace = enlace;
    }

    public Integer getIdCategoria() {
        return idCategoria;
    }

    public void setIdCategoria(Integer idCategoria) {
        this.idCategoria = idCategoria;
    }

    public String getNombreCategoria() {
        return nombreCategoria;
    }

    public void setNombreCategoria(String nombreCategoria) {
        this.nombreCategoria = nombreCategoria;
    }

    public String getSubtitulo() {
        return subtitulo;
    }

    public void setSubtitulo(String subtitulo) {
        this.subtitulo = subtitulo;
    }

    public String getImagenUrl() {
        return imagenUrl;
    }

    public void setImagenUrl(String imagenUrl) {
        this.imagenUrl = imagenUrl;
    }

    public String getEnlace() {
        return enlace;
    }

    public void setEnlace(String enlace) {
        this.enlace = enlace;
    }
}

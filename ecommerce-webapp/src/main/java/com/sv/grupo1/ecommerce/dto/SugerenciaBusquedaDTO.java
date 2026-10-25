package com.sv.grupo1.ecommerce.dto;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class SugerenciaBusquedaDTO {

    private List<SugerenciaProductoDTO> productos = new ArrayList<>();
    private List<SugerenciaCategoriaDTO> categorias = new ArrayList<>();
    private List<SugerenciaMarcaDTO> marcas = new ArrayList<>();
    private int totalResultados;

    public SugerenciaBusquedaDTO() {
    }

    public List<SugerenciaProductoDTO> getProductos() {
        return productos;
    }

    public void setProductos(List<SugerenciaProductoDTO> productos) {
        this.productos = productos;
    }

    public List<SugerenciaCategoriaDTO> getCategorias() {
        return categorias;
    }

    public void setCategorias(List<SugerenciaCategoriaDTO> categorias) {
        this.categorias = categorias;
    }

    public List<SugerenciaMarcaDTO> getMarcas() {
        return marcas;
    }

    public void setMarcas(List<SugerenciaMarcaDTO> marcas) {
        this.marcas = marcas;
    }

    public int getTotalResultados() {
        return totalResultados;
    }

    public void setTotalResultados(int totalResultados) {
        this.totalResultados = totalResultados;
    }

    public static class SugerenciaProductoDTO {
        private Integer idProducto;
        private String codigoSku;
        private String nombreProducto;
        private BigDecimal precioVenta;
        private String imagenUrl;
        private String nombreCategoria;
        private String nombreMarca;
        private String url;

        public SugerenciaProductoDTO() {
        }

        public SugerenciaProductoDTO(Integer idProducto, String codigoSku, String nombreProducto,
                                     BigDecimal precioVenta, String imagenUrl, String nombreCategoria,
                                     String nombreMarca, String url) {
            this.idProducto = idProducto;
            this.codigoSku = codigoSku;
            this.nombreProducto = nombreProducto;
            this.precioVenta = precioVenta;
            this.imagenUrl = imagenUrl;
            this.nombreCategoria = nombreCategoria;
            this.nombreMarca = nombreMarca;
            this.url = url;
        }

        public Integer getIdProducto() {
            return idProducto;
        }

        public void setIdProducto(Integer idProducto) {
            this.idProducto = idProducto;
        }

        public String getCodigoSku() {
            return codigoSku;
        }

        public void setCodigoSku(String codigoSku) {
            this.codigoSku = codigoSku;
        }

        public String getNombreProducto() {
            return nombreProducto;
        }

        public void setNombreProducto(String nombreProducto) {
            this.nombreProducto = nombreProducto;
        }

        public BigDecimal getPrecioVenta() {
            return precioVenta;
        }

        public void setPrecioVenta(BigDecimal precioVenta) {
            this.precioVenta = precioVenta;
        }

        public String getImagenUrl() {
            return imagenUrl;
        }

        public void setImagenUrl(String imagenUrl) {
            this.imagenUrl = imagenUrl;
        }

        public String getNombreCategoria() {
            return nombreCategoria;
        }

        public void setNombreCategoria(String nombreCategoria) {
            this.nombreCategoria = nombreCategoria;
        }

        public String getNombreMarca() {
            return nombreMarca;
        }

        public void setNombreMarca(String nombreMarca) {
            this.nombreMarca = nombreMarca;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }
    }

    public static class SugerenciaCategoriaDTO {
        private Integer idCategoria;
        private String nombreCategoria;
        private String url;

        public SugerenciaCategoriaDTO() {
        }

        public SugerenciaCategoriaDTO(Integer idCategoria, String nombreCategoria, String url) {
            this.idCategoria = idCategoria;
            this.nombreCategoria = nombreCategoria;
            this.url = url;
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

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }
    }

    public static class SugerenciaMarcaDTO {
        private Integer idMarca;
        private String nombreMarca;
        private String url;

        public SugerenciaMarcaDTO() {
        }

        public SugerenciaMarcaDTO(Integer idMarca, String nombreMarca, String url) {
            this.idMarca = idMarca;
            this.nombreMarca = nombreMarca;
            this.url = url;
        }

        public Integer getIdMarca() {
            return idMarca;
        }

        public void setIdMarca(Integer idMarca) {
            this.idMarca = idMarca;
        }

        public String getNombreMarca() {
            return nombreMarca;
        }

        public void setNombreMarca(String nombreMarca) {
            this.nombreMarca = nombreMarca;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }
    }
}

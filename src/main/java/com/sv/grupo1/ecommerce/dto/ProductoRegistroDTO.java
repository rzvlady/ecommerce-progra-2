package com.sv.grupo1.ecommerce.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public class ProductoRegistroDTO {
    @NotBlank(message = "El SKU es obligatorio")
    private String sku;

    @NotBlank(message = "El nombre es obligatorio")
    private String nombreProducto;

    @NotNull(message = "El precio es obligatorio")
    @Min(value = 0, message = "El precio no puede ser negativo")
    private BigDecimal precioCosto;

    @NotNull(message = "El stock es obligatorio")
    @Min(value = 0, message = "El stock no puede ser negativo")
    private Integer stockDisponible;

    @NotNull(message = "El stock mínimo es obligatorio")
    @Min(value = 0, message = "El stock mínimo no puede ser negativo")
    private Integer stockMinimo;

    @NotNull(message = "Debe seleccionar una categoría")
    private Integer idCategoria;

    @NotNull(message = "Debe seleccionar una marca")
    private Integer idMarca;

    // Getters y Setters
    public String getSku() {return this.sku;}
    public void setSku(String sku) {this.sku = sku;}
    public String getNombreProducto() {return this.nombreProducto;}
    public void setNombreProducto(String nombreProducto) {this.nombreProducto = nombreProducto;}
    public BigDecimal getPrecioCosto() {return this.precioCosto;}
    public void setPrecioCosto(BigDecimal precio) {this.precioCosto = precio;}
    public Integer getStockDisponible() {return this.stockDisponible;}
    public void setStockDisponible(Integer stockDisponible) {this.stockDisponible = stockDisponible;}
    public Integer getStockMinimo() {return this.stockMinimo;}
    public void setStockMinimo(Integer stockMinimo) {this.stockMinimo = stockMinimo;}
    public Integer getIdCategoria() {return this.idCategoria;}
    public void setIdCategoria(Integer idCategoria) {this.idCategoria = idCategoria;}
    public Integer getIdMarca() {return this.idMarca;}
    public void setIdMarca(Integer idMarca) {this.idMarca = idMarca;}
}
package com.sv.grupo1.backoffice.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;

public class ProductoRegistroDTO {

    private Integer idProducto;

    // El SKU se genera automáticamente según categoría y marca
    private String sku;

    @NotBlank(message = "El nombre es obligatorio")
    private String nombreProducto;

    @NotBlank(message = "La descripción es obligatoria")
    private String descripcionProducto;

    @NotNull(message = "El precio de venta es obligatorio")
    @DecimalMin(value = "0.01", message = "El precio de venta debe ser mayor a 0")
    private BigDecimal precioVenta;

    @NotNull(message = "El precio de costo es obligatorio")
    @Min(value = 0, message = "El precio no puede ser negativo")
    private BigDecimal precioCosto;

    @NotNull(message = "El stock disponible es obligatorio")
    @Min(value = 0, message = "El stock no puede ser negativo")
    private Integer stockDisponible;

    @NotNull(message = "El stock mínimo es obligatorio")
    @Min(value = 0, message = "El stock mínimo no puede ser negativo")
    private Integer stockMinimo;

    @NotNull(message = "Debe seleccionar una categoría")
    private Integer idCategoria;

    @NotNull(message = "Debe seleccionar una marca")
    private Integer idMarca;

    private Character estado = 'A';

    // Atributos dinámicos del producto en formato JSON (columna metadata JSONB)
    private String metadata;

    // Fotografías por bloque individual (Bloque 1..4) y control de eliminaciones
    private MultipartFile imagenSlot1;
    private MultipartFile imagenSlot2;
    private MultipartFile imagenSlot3;
    private MultipartFile imagenSlot4;
    private String slotsEliminar;

    // Getters y Setters
    public Integer getIdProducto() { return this.idProducto; }
    public void setIdProducto(Integer idProducto) { this.idProducto = idProducto; }
    public String getSku() { return this.sku; }
    public void setSku(String sku) { this.sku = sku; }
    public String getNombreProducto() { return this.nombreProducto; }
    public void setNombreProducto(String nombreProducto) { this.nombreProducto = nombreProducto; }
    public String getDescripcionProducto() { return this.descripcionProducto; }
    public void setDescripcionProducto(String descripcionProducto) { this.descripcionProducto = descripcionProducto; }
    public BigDecimal getPrecioVenta() { return this.precioVenta; }
    public void setPrecioVenta(BigDecimal precioVenta) { this.precioVenta = precioVenta; }
    public BigDecimal getPrecioCosto() { return this.precioCosto; }
    public void setPrecioCosto(BigDecimal precio) { this.precioCosto = precio; }
    public Integer getStockDisponible() { return this.stockDisponible; }
    public void setStockDisponible(Integer stockDisponible) { this.stockDisponible = stockDisponible; }
    public Integer getStockMinimo() { return this.stockMinimo; }
    public void setStockMinimo(Integer stockMinimo) { this.stockMinimo = stockMinimo; }
    public Integer getIdCategoria() { return this.idCategoria; }
    public void setIdCategoria(Integer idCategoria) { this.idCategoria = idCategoria; }
    public Integer getIdMarca() { return this.idMarca; }
    public void setIdMarca(Integer idMarca) { this.idMarca = idMarca; }
    public Character getEstado() { return this.estado; }
    public void setEstado(Character estado) { this.estado = estado; }
    public String getMetadata() { return this.metadata; }
    public void setMetadata(String metadata) { this.metadata = metadata; }
    public MultipartFile getImagenSlot1() { return this.imagenSlot1; }
    public void setImagenSlot1(MultipartFile imagenSlot1) { this.imagenSlot1 = imagenSlot1; }
    public MultipartFile getImagenSlot2() { return this.imagenSlot2; }
    public void setImagenSlot2(MultipartFile imagenSlot2) { this.imagenSlot2 = imagenSlot2; }
    public MultipartFile getImagenSlot3() { return this.imagenSlot3; }
    public void setImagenSlot3(MultipartFile imagenSlot3) { this.imagenSlot3 = imagenSlot3; }
    public MultipartFile getImagenSlot4() { return this.imagenSlot4; }
    public void setImagenSlot4(MultipartFile imagenSlot4) { this.imagenSlot4 = imagenSlot4; }
    public String getSlotsEliminar() { return this.slotsEliminar; }
    public void setSlotsEliminar(String slotsEliminar) { this.slotsEliminar = slotsEliminar; }
}

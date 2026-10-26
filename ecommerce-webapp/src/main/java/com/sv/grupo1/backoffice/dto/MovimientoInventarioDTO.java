package com.sv.grupo1.backoffice.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class MovimientoInventarioDTO {

    @NotNull(message = "Debe seleccionar un producto")
    private Integer idProducto;

    @NotBlank(message = "Debe seleccionar el tipo de movimiento")
    private String tipoMovimiento = "ENTRADA";

    @NotNull(message = "La cantidad es obligatoria")
    @Min(value = 1, message = "La cantidad debe ser al menos 1 unidad")
    private Integer cantidad;

    @NotBlank(message = "Debe seleccionar el motivo del movimiento")
    private String motivo = "COMPRA_PROVEEDOR";

    private String observacion;

    public MovimientoInventarioDTO() {}

    public Integer getIdProducto() {
        return idProducto;
    }

    public void setIdProducto(Integer idProducto) {
        this.idProducto = idProducto;
    }

    public String getTipoMovimiento() {
        return tipoMovimiento;
    }

    public void setTipoMovimiento(String tipoMovimiento) {
        this.tipoMovimiento = tipoMovimiento;
    }

    public Integer getCantidad() {
        return cantidad;
    }

    public void setCantidad(Integer cantidad) {
        this.cantidad = cantidad;
    }

    public String getMotivo() {
        return motivo;
    }

    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }

    public String getObservacion() {
        return observacion;
    }

    public void setObservacion(String observacion) {
        this.observacion = observacion;
    }
}

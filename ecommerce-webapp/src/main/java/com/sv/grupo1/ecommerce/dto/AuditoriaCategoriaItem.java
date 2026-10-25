package com.sv.grupo1.ecommerce.dto;

import java.io.Serializable;

/**
 * DTO que representa el resultado de la auditoría de inventario
 * procesada de forma concurrente para una categoría específica.
 */
public class AuditoriaCategoriaItem implements Serializable {

    private Integer idCategoria;
    private String nombreCategoria;
    private int totalProductos;
    private int stockTotal;
    private int productosAlerta;
    private int discrepanciasDetectadas;
    private String hiloEjecutor;
    private long tiempoMilisegundos;
    private String estado;

    public AuditoriaCategoriaItem() {}

    public AuditoriaCategoriaItem(Integer idCategoria, String nombreCategoria, int totalProductos,
                                  int stockTotal, int productosAlerta, int discrepanciasDetectadas,
                                  String hiloEjecutor, long tiempoMilisegundos, String estado) {
        this.idCategoria = idCategoria;
        this.nombreCategoria = nombreCategoria;
        this.totalProductos = totalProductos;
        this.stockTotal = stockTotal;
        this.productosAlerta = productosAlerta;
        this.discrepanciasDetectadas = discrepanciasDetectadas;
        this.hiloEjecutor = hiloEjecutor;
        this.tiempoMilisegundos = tiempoMilisegundos;
        this.estado = estado;
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

    public int getTotalProductos() {
        return totalProductos;
    }

    public void setTotalProductos(int totalProductos) {
        this.totalProductos = totalProductos;
    }

    public int getStockTotal() {
        return stockTotal;
    }

    public void setStockTotal(int stockTotal) {
        this.stockTotal = stockTotal;
    }

    public int getProductosAlerta() {
        return productosAlerta;
    }

    public void setProductosAlerta(int productosAlerta) {
        this.productosAlerta = productosAlerta;
    }

    public int getDiscrepanciasDetectadas() {
        return discrepanciasDetectadas;
    }

    public void setDiscrepanciasDetectadas(int discrepanciasDetectadas) {
        this.discrepanciasDetectadas = discrepanciasDetectadas;
    }

    public String getHiloEjecutor() {
        return hiloEjecutor;
    }

    public void setHiloEjecutor(String hiloEjecutor) {
        this.hiloEjecutor = hiloEjecutor;
    }

    public long getTiempoMilisegundos() {
        return tiempoMilisegundos;
    }

    public void setTiempoMilisegundos(long tiempoMilisegundos) {
        this.tiempoMilisegundos = tiempoMilisegundos;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }
}

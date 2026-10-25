package com.sv.grupo1.ecommerce.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * DTO que consolida los resultados de la auditoría de inventario multihilo.
 * Incluye métricas de rendimiento, hilos ejecutores concurrentes del pool
 * y detalle de integridad por categoría.
 */
public class ResultadoAuditoriaConcurrente implements Serializable {

    private int totalCategoriasAuditadas;
    private int totalProductosVerificados;
    private int totalAlertas;
    private int totalDiscrepancias;
    private long tiempoTotalMs;
    private Set<String> hilosUtilizados = new HashSet<>();
    private List<AuditoriaCategoriaItem> detallesPorCategoria = new ArrayList<>();
    private String mensaje;
    private boolean exito;

    public ResultadoAuditoriaConcurrente() {}

    public int getTotalCategoriasAuditadas() {
        return totalCategoriasAuditadas;
    }

    public void setTotalCategoriasAuditadas(int totalCategoriasAuditadas) {
        this.totalCategoriasAuditadas = totalCategoriasAuditadas;
    }

    public int getTotalProductosVerificados() {
        return totalProductosVerificados;
    }

    public void setTotalProductosVerificados(int totalProductosVerificados) {
        this.totalProductosVerificados = totalProductosVerificados;
    }

    public int getTotalAlertas() {
        return totalAlertas;
    }

    public void setTotalAlertas(int totalAlertas) {
        this.totalAlertas = totalAlertas;
    }

    public int getTotalDiscrepancias() {
        return totalDiscrepancias;
    }

    public void setTotalDiscrepancias(int totalDiscrepancias) {
        this.totalDiscrepancias = totalDiscrepancias;
    }

    public long getTiempoTotalMs() {
        return tiempoTotalMs;
    }

    public void setTiempoTotalMs(long tiempoTotalMs) {
        this.tiempoTotalMs = tiempoTotalMs;
    }

    public Set<String> getHilosUtilizados() {
        return hilosUtilizados;
    }

    public void setHilosUtilizados(Set<String> hilosUtilizados) {
        this.hilosUtilizados = hilosUtilizados;
    }

    public List<AuditoriaCategoriaItem> getDetallesPorCategoria() {
        return detallesPorCategoria;
    }

    public void setDetallesPorCategoria(List<AuditoriaCategoriaItem> detallesPorCategoria) {
        this.detallesPorCategoria = detallesPorCategoria;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }

    public boolean isExito() {
        return exito;
    }

    public void setExito(boolean exito) {
        this.exito = exito;
    }
}

package com.sv.grupo1.ecommerce.services;

import com.sv.grupo1.ecommerce.dto.ResultadoAuditoriaConcurrente;

/**
 * Servicio de auditoría concurrente de inventario y stock.
 * Ejecuta validaciones de integridad en paralelo utilizando un pool de hilos
 * (ExecutorService) y conexiones directas al DataSource.
 */
public interface AuditoriaInventarioConcurrenteService {

    /**
     * Ejecuta una auditoría concurrente completa de inventario sobre todas las categorías registradas.
     *
     * @param hilosConcurrencia Cantidad de hilos trabajadores en paralelo.
     * @return DTO consolidado con métricas, hilos involucrados y alertas detectadas.
     */
    ResultadoAuditoriaConcurrente ejecutarAuditoriaConcurrente(int hilosConcurrencia);

    /**
     * Sobrecarga conveniente que utiliza 4 hilos por defecto.
     *
     * @return DTO consolidado de la auditoría.
     */
    default ResultadoAuditoriaConcurrente ejecutarAuditoriaConcurrente() {
        return ejecutarAuditoriaConcurrente(4);
    }
}

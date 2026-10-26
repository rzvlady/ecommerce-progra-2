package com.sv.grupo1.ecommerce.services;

import com.sv.grupo1.ecommerce.dto.ResultadoAuditoriaConcurrente;

/**
 * Servicio de auditoría concurrente de inventario y stock.
 */
public interface AuditoriaInventarioConcurrenteService {

    /**
     * Ejecuta una auditoría concurrente de inventario sobre las categorías registradas.
     *
     * @param hilosConcurrencia Cantidad de hilos trabajadores en paralelo.
     * @return Resultado consolidado de la auditoría.
     */
    ResultadoAuditoriaConcurrente ejecutarAuditoriaConcurrente(int hilosConcurrencia);

    /**
     * Ejecuta la auditoría concurrente utilizando 4 hilos por defecto.
     *
     * @return Resultado consolidado de la auditoría.
     */
    default ResultadoAuditoriaConcurrente ejecutarAuditoriaConcurrente() {
        return ejecutarAuditoriaConcurrente(4);
    }
}

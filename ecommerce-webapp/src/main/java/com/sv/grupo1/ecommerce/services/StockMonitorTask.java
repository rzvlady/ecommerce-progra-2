package com.sv.grupo1.ecommerce.services;

import com.sv.grupo1.ecommerce.dao.ProductoRepository;
import com.sv.grupo1.ecommerce.entities.core.Producto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

// Tarea ejecutada en segundo plano para monitorear stock sin bloquear peticiones HTTP
// Justificación Punto 5 (Uso de programación concurrente / Hilos)
@Component
public class StockMonitorTask {

    private static final DateTimeFormatter FORMATO_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
    private static final Logger log = LoggerFactory.getLogger(StockMonitorTask.class);

    private final ProductoRepository productoRepository;

    // Recurso compartido en memoria: visible de forma segura para todos los hilos
    private volatile List<Producto> productosBajoStockAlertados = List.of();

    public StockMonitorTask(ProductoRepository productoRepository) {
        this.productoRepository = productoRepository;
    }

    // Proceso en segundo plano ejecutado en un hilo separado cada 60 segundos
    @Scheduled(fixedRate = 60000)
    public void escanearInventario() {
        String horaEjecucion = LocalDateTime.now().format(FORMATO_HORA);
        log.info("[Monitor Hilo: {}] [{}] Iniciando escaneo de inventario...",
                Thread.currentThread().getName(), horaEjecucion);

        List<Producto> productosCriticos = productoRepository.findProductosConStockBajo();

        // Actualización atómica en memoria (evita ventanas inconsistentes de lectura)
        this.productosBajoStockAlertados = List.copyOf(productosCriticos);

        if (!productosBajoStockAlertados.isEmpty()) {
            log.warn("[{}] [ALERTA STOCK] Se detectaron {} productos con nivel crítico.",
                    horaEjecucion, productosBajoStockAlertados.size());
            for (Producto p : productosBajoStockAlertados) {
                log.warn("-> Producto: '{}' | SKU: {} | Stock Actual: {} | Stock Mínimo: {}",
                        p.getNombreProducto(),
                        p.getCodigoSku(),
                        p.getStockDisponible(),
                        p.getStockMinimo());
            }
        } else {
            log.info("[{}] [Stock OK] Niveles de inventario óptimos.", horaEjecucion);
        }
    }

    // Acceso de lectura para el Controller (hilos web de Tomcat)
    public List<Producto> getProductosBajoStockAlertados() {
        return productosBajoStockAlertados;
    }
}
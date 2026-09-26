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
public class StockMonitorTask implements Runnable {
    private static final DateTimeFormatter FORMATO_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
    private static final Logger log = LoggerFactory.getLogger(StockMonitorTask.class);

    private final ProductoRepository productoRepository;
    // Colección en memoria segura ante hilos (thread-safe) para retener las alertas
    private final List<Producto> productosBajoStockAlertados = new CopyOnWriteArrayList<>();

    public StockMonitorTask(ProductoRepository productoRepository) {
        this.productoRepository = productoRepository;
    }
    // Hilo programado que escanea periódicamente el inventario cada 60 segundos
    @Override
    @Scheduled(fixedRate = 60000)
    public void run() {
        String horaEjecucion = LocalDateTime.now().format(FORMATO_HORA);
        log.info("[Monitor Hilo: {}] [{}] Iniciando escaneo de inventario...", Thread.currentThread().getName(), horaEjecucion);

        // Consulta en base de datos de productos que alcanzaron o rebasaron su stock mínimo
        List<Producto> productosCriticos = productoRepository.findProductosConStockBajo();

        // Actualización concurrente de la colección en memoria
        productosBajoStockAlertados.clear();
        productosBajoStockAlertados.addAll(productosCriticos);

        // Notificación en logs y detalle de productos críticos detectados por el hilo
        if (!productosBajoStockAlertados.isEmpty()) {
            log.warn("[{}] [ALERTA STOCK] Se detectaron {} productos con nivel crítico.", horaEjecucion, productosBajoStockAlertados.size());
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

    // Acceso de solo lectura a la colección en memoria para desacoplar el controlador de la base de datos
    public List<Producto> getProductosBajoStockAlertados() {
        return Collections.unmodifiableList(productosBajoStockAlertados);
    }
}
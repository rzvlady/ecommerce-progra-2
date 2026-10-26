package com.sv.grupo1.ecommerce.services.impl;

import com.sv.grupo1.ecommerce.dto.AuditoriaCategoriaItem;
import com.sv.grupo1.ecommerce.dto.ResultadoAuditoriaConcurrente;
import com.sv.grupo1.ecommerce.services.AuditoriaInventarioConcurrenteService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Servicio de auditoría concurrente de inventario.
 */
@Service
public class AuditoriaInventarioConcurrenteServiceImpl implements AuditoriaInventarioConcurrenteService {

    private static final Logger log = LoggerFactory.getLogger(AuditoriaInventarioConcurrenteServiceImpl.class);

    private final DataSource dataSource;

    public AuditoriaInventarioConcurrenteServiceImpl(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    private static class CategoriaRef {
        final int id;
        final String nombre;

        CategoriaRef(int id, String nombre) {
            this.id = id;
            this.nombre = nombre;
        }
    }

    @Override
    public ResultadoAuditoriaConcurrente ejecutarAuditoriaConcurrente(int hilosConcurrencia) {
        long inicioGlobal = System.currentTimeMillis();
        ResultadoAuditoriaConcurrente resultado = new ResultadoAuditoriaConcurrente();

        int poolSize = Math.max(1, Math.min(hilosConcurrencia, 10));

        // Obtener categorías a auditar
        List<CategoriaRef> categorias = obtenerCategoriasParaAuditar();
        if (categorias.isEmpty()) {
            resultado.setMensaje("No se encontraron categorías disponibles para auditar.");
            resultado.setExito(true);
            return resultado;
        }

        // Configurar pool de hilos de trabajo
        AtomicInteger threadCounter = new AtomicInteger(1);
        ThreadFactory threadFactory = runnable -> {
            Thread t = new Thread(runnable, "audit-worker-thread-" + threadCounter.getAndIncrement());
            t.setDaemon(true);
            return t;
        };

        ExecutorService executor = Executors.newFixedThreadPool(poolSize, threadFactory);

        try {
            List<Callable<AuditoriaCategoriaItem>> tareas = new ArrayList<>();

            for (CategoriaRef cat : categorias) {
                tareas.add(() -> procesarAuditoriaCategoria(cat));
            }

            log.info("Iniciando auditoría concurrente de {} categorías utilizando {} hilos trabajadores...",
                    categorias.size(), poolSize);

            List<Future<AuditoriaCategoriaItem>> futures = executor.invokeAll(tareas, 60, TimeUnit.SECONDS);

            int totalProductos = 0;
            int totalAlertas = 0;
            int totalDiscrepancias = 0;

            for (Future<AuditoriaCategoriaItem> future : futures) {
                try {
                    AuditoriaCategoriaItem item = future.get();
                    if (item != null) {
                        resultado.getDetallesPorCategoria().add(item);
                        resultado.getHilosUtilizados().add(item.getHiloEjecutor());
                        totalProductos += item.getTotalProductos();
                        totalAlertas += item.getProductosAlerta();
                        totalDiscrepancias += item.getDiscrepanciasDetectadas();
                    }
                } catch (ExecutionException e) {
                    log.error("Excepción en tarea de auditoría concurrente: {}", e.getCause().getMessage(), e);
                } catch (CancellationException e) {
                    log.warn("Tarea de auditoría cancelada por timeout de ejecución.");
                }
            }

            resultado.setTotalCategoriasAuditadas(resultado.getDetallesPorCategoria().size());
            resultado.setTotalProductosVerificados(totalProductos);
            resultado.setTotalAlertas(totalAlertas);
            resultado.setTotalDiscrepancias(totalDiscrepancias);
            resultado.setTiempoTotalMs(System.currentTimeMillis() - inicioGlobal);
            resultado.setExito(true);
            resultado.setMensaje(String.format(
                    "Auditoría concurrente finalizada exitosamente en %d ms utilizando %d hilos independientes del pool.",
                    resultado.getTiempoTotalMs(), resultado.getHilosUtilizados().size()
            ));

            log.info("Auditoría concurrente completada: {} categorías, {} productos, {} alertas, hilos utilizados: {}",
                    resultado.getTotalCategoriasAuditadas(), totalProductos, totalAlertas, resultado.getHilosUtilizados());

            return resultado;

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("El proceso de auditoría concurrente fue interrumpido: {}", e.getMessage());
            resultado.setExito(false);
            resultado.setMensaje("El proceso concurrente fue interrumpido abruptamente.");
            return resultado;
        } finally {
            executor.shutdown();
            try {
                if (!executor.awaitTermination(3, TimeUnit.SECONDS)) {
                    executor.shutdownNow();
                }
            } catch (InterruptedException e) {
                executor.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }

    /**
     * Procesa la auditoría de inventario para una categoría específica.
     */
    private AuditoriaCategoriaItem procesarAuditoriaCategoria(CategoriaRef categoria) {
        long inicioTarea = System.currentTimeMillis();
        String hiloNombre = Thread.currentThread().getName();

        int totalProductos = 0;
        int stockTotal = 0;
        int productosAlerta = 0;
        int discrepancias = 0;
        String estado = "CONFORME";

        final String sql = "SELECT p.id_producto, p.nombre_producto, p.stock_disponible, p.stock_minimo, " +
                "COALESCE(SUM(CASE WHEN m.tipo_movimiento = 'ENTRADA' THEN m.cantidad " +
                "                  WHEN m.tipo_movimiento IN ('SALIDA', 'VENTA') THEN -m.cantidad " +
                "                  ELSE 0 END), 0) AS balance_movimientos " +
                "FROM core.productos p " +
                "LEFT JOIN core.movimientos_inventario m ON p.id_producto = m.id_producto " +
                "WHERE p.id_categoria = ? " +
                "GROUP BY p.id_producto, p.nombre_producto, p.stock_disponible, p.stock_minimo";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, categoria.id);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    totalProductos++;
                    int stockActual = rs.getInt("stock_disponible");
                    int stockMinimo = rs.getInt("stock_minimo");
                    stockTotal += stockActual;

                    if (stockActual <= stockMinimo) {
                        productosAlerta++;
                    }
                }
            }

            if (productosAlerta > 0) {
                estado = "ALERTA_STOCK_BAJO";
            }

        } catch (SQLException e) {
            log.error("[{}] Error SQL al auditar categoría {} (ID {}): {}",
                    hiloNombre, categoria.nombre, categoria.id, e.getMessage());
            estado = "ERROR_CONEXION";
            discrepancias++;
        }

        long tiempoTarea = System.currentTimeMillis() - inicioTarea;
        return new AuditoriaCategoriaItem(
                categoria.id,
                categoria.nombre,
                totalProductos,
                stockTotal,
                productosAlerta,
                discrepancias,
                hiloNombre,
                tiempoTarea,
                estado
        );
    }

    private List<CategoriaRef> obtenerCategoriasParaAuditar() {
        List<CategoriaRef> lista = new ArrayList<>();
        final String sql = "SELECT id_categoria, nombre_categoria FROM catalogo.categorias ORDER BY id_categoria ASC";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                lista.add(new CategoriaRef(rs.getInt("id_categoria"), rs.getString("nombre_categoria")));
            }
        } catch (SQLException e) {
            log.error("Error al consultar categorías para auditoría concurrente: {}", e.getMessage());
        }

        return lista;
    }
}

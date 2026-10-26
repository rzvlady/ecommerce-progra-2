package com.sv.grupo1.backoffice.controller;

import com.sv.grupo1.backoffice.dto.ActividadRecienteDTO;
import com.sv.grupo1.backoffice.service.BackofficeClienteService;
import com.sv.grupo1.backoffice.service.BackofficeProductoService;
import com.sv.grupo1.ecommerce.dao.MovimientoInventarioRepository;
import com.sv.grupo1.ecommerce.dao.PagoRepository;
import com.sv.grupo1.ecommerce.dao.PedidoRepository;
import com.sv.grupo1.ecommerce.entities.core.MovimientoInventario;
import com.sv.grupo1.ecommerce.entities.core.Pago;
import com.sv.grupo1.ecommerce.entities.core.Pedido;
import com.sv.grupo1.ecommerce.enums.EstadoPedido;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/*
 * @author alexemestica & Vladimir Ruiz
 */
@Controller
@RequestMapping(path = "/backoffice")
@PreAuthorize("hasRole('ADMIN')")
public class DashboardController {

    private static final Logger LOG = LoggerFactory.getLogger(DashboardController.class);

    private final BackofficeProductoService productoService;
    private final BackofficeClienteService clienteService;
    private final PedidoRepository pedidoRepository;
    private final MovimientoInventarioRepository movimientoInventarioRepository;
    private final PagoRepository pagoRepository;

    public DashboardController(
            BackofficeProductoService productoService,
            BackofficeClienteService clienteService,
            PedidoRepository pedidoRepository,
            MovimientoInventarioRepository movimientoInventarioRepository,
            PagoRepository pagoRepository) {
        this.productoService = productoService;
        this.clienteService = clienteService;
        this.pedidoRepository = pedidoRepository;
        this.movimientoInventarioRepository = movimientoInventarioRepository;
        this.pagoRepository = pagoRepository;
    }

    @RequestMapping(path = "/dashboard")
    public String dashboard(Model model) {
        LOG.info("Dashboard Controller cargando indicadores y actividad real desde BD");

        // 1. Catálogo e Inventario
        long totalProductos = productoService.contarTotalProductos();
        model.addAttribute("totalProductos", totalProductos);

        // 2. Clientes
        Map<String, Long> metricasClientes = clienteService.obtenerMetricasDashboard();
        model.addAttribute("totalClientes", metricasClientes.getOrDefault("total", 0L));
        model.addAttribute("clientesActivos", metricasClientes.getOrDefault("activos", 0L));
        model.addAttribute("clientesInactivos", metricasClientes.getOrDefault("inactivos", 0L));
        model.addAttribute("clientesNuevosMes", metricasClientes.getOrDefault("nuevosMes", 0L));

        // 3. Ventas y Pedidos en tiempo real (Base de Datos Supabase)
        OffsetDateTime inicioHoy = LocalDate.now().atStartOfDay().atOffset(ZoneOffset.UTC);
        BigDecimal ventasDelDia = BigDecimal.ZERO;
        long pedidosPendientes = 0;
        long pedidosConfirmados = 0;
        long totalPedidos = 0;
        List<Pedido> pedidosRecientes = new ArrayList<>();

        try {
            ventasDelDia = pedidoRepository.calcularVentasDesde(inicioHoy);
            if (ventasDelDia == null) {
                ventasDelDia = BigDecimal.ZERO;
            }
            pedidosPendientes = pedidoRepository.countByEstadoPedido(EstadoPedido.PENDIENTE.getCodigoEstado());
            pedidosConfirmados = pedidoRepository.countByEstadoPedido(EstadoPedido.CONFIRMADO.getCodigoEstado());
            pedidosRecientes = pedidoRepository.findAllConRelaciones();
            totalPedidos = pedidosRecientes.size();
        } catch (Exception e) {
            LOG.warn("No se pudieron cargar métricas agregadas de pedidos: {}", e.getMessage());
        }

        model.addAttribute("ventasDelDia", ventasDelDia);
        model.addAttribute("pedidosPendientes", pedidosPendientes);
        model.addAttribute("pedidosConfirmados", pedidosConfirmados);
        model.addAttribute("totalPedidos", totalPedidos);

        // 4. Actividad Reciente Unificada (Últimos pedidos + movimientos de inventario)
        List<ActividadRecienteDTO> actividades = construirActividadReciente(pedidosRecientes);
        model.addAttribute("actividadesRecientes", actividades);

        return "backoffice/dashboard";
    }

    private List<ActividadRecienteDTO> construirActividadReciente(List<Pedido> pedidosRecientes) {
        List<ActividadRecienteDTO> lista = new ArrayList<>();
        OffsetDateTime ahora = OffsetDateTime.now();

        // A. Agregar pedidos reales
        int limitePedidos = Math.min(pedidosRecientes.size(), 8);
        for (int i = 0; i < limitePedidos; i++) {
            Pedido p = pedidosRecientes.get(i);
            ActividadRecienteDTO item = new ActividadRecienteDTO();
            item.setTipo("PEDIDO");
            item.setTitulo("Pedido #" + p.getIdPedido());
            item.setSubtitulo("Cliente: " + (p.getUsuario() != null ? p.getUsuario().getCorreoInicioSesion() : "Anónimo"));
            item.setDetalle("Monto: $" + (p.getTotal() != null ? p.getTotal() : "0.00"));
            item.setIcono("bi bi-bag-check-fill");
            item.setIconoColor("text-primary");
            item.setEnlaceUrl("/backoffice/pedidos");
            item.setEnlaceTexto("Ver orden");
            item.setTimestamp(p.getFechaCreacion() != null ? p.getFechaCreacion() : ahora);
            item.setTiempoRelativo(calcularTiempoRelativo(item.getTimestamp(), ahora));

            char est = p.getEstadoPedido() != null ? p.getEstadoPedido() : 'P';
            if (est == 'C' || est == EstadoPedido.CONFIRMADO.getCodigoEstado()) {
                item.setBadgeTexto("Pagado");
                item.setBadgeClass("bg-primary-subtle text-primary border border-primary-subtle");
            } else if (est == 'E' || est == EstadoPedido.ENVIADO.getCodigoEstado()) {
                item.setBadgeTexto("En Tránsito");
                item.setBadgeClass("bg-info-subtle text-info border border-info-subtle");
            } else if (est == 'F' || est == EstadoPedido.FINALIZADO.getCodigoEstado()) {
                item.setBadgeTexto("Entregado");
                item.setBadgeClass("bg-success-subtle text-success border border-success-subtle");
            } else if (est == 'A' || est == EstadoPedido.ANULADO.getCodigoEstado()) {
                item.setBadgeTexto("Anulado");
                item.setBadgeClass("bg-danger-subtle text-danger border border-danger-subtle");
            } else {
                item.setBadgeTexto("Pendiente");
                item.setBadgeClass("bg-warning-subtle text-dark border border-warning-subtle");
            }
            lista.add(item);
        }

        // B. Agregar movimientos de inventario recientes (VENTAS y AJUSTES)
        try {
            List<MovimientoInventario> movimientos = movimientoInventarioRepository.findAllConProducto();
            int limiteMov = Math.min(movimientos.size(), 8);
            for (int i = 0; i < limiteMov; i++) {
                MovimientoInventario m = movimientos.get(i);
                ActividadRecienteDTO item = new ActividadRecienteDTO();
                item.setTipo("INVENTARIO");
                item.setTitulo("Movimiento: " + m.getTipoMovimiento());
                item.setSubtitulo(m.getProducto() != null ? m.getProducto().getNombreProducto() : "Producto");
                item.setDetalle("Cantidad: " + (m.getCantidad() != null ? m.getCantidad() : 1) + " unidad(es) • " + (m.getMotivo() != null ? m.getMotivo() : ""));
                item.setEnlaceUrl("/backoffice/inventario");
                item.setEnlaceTexto("Ver inventario");
                item.setTimestamp(m.getFechaMovimiento() != null ? m.getFechaMovimiento() : ahora);
                item.setTiempoRelativo(calcularTiempoRelativo(item.getTimestamp(), ahora));

                if ("SALIDA".equalsIgnoreCase(m.getTipoMovimiento())) {
                    item.setIcono("bi bi-box-arrow-right");
                    item.setIconoColor("text-warning");
                    item.setBadgeTexto("Salida stock");
                    item.setBadgeClass("bg-warning-subtle text-dark border border-warning-subtle");
                } else {
                    item.setIcono("bi bi-box-arrow-in-left");
                    item.setIconoColor("text-success");
                    item.setBadgeTexto("Entrada stock");
                    item.setBadgeClass("bg-success-subtle text-success border border-success-subtle");
                }
                lista.add(item);
            }
        } catch (Exception e) {
            LOG.warn("No se pudieron cargar movimientos de inventario recientes: {}", e.getMessage());
        }

        // Ordenar cronológicamente descendente y tomar los 10 más recientes
        lista.sort(Comparator.comparing(ActividadRecienteDTO::getTimestamp, Comparator.nullsLast(Comparator.reverseOrder())));
        if (lista.size() > 10) {
            return lista.subList(0, 10);
        }
        return lista;
    }

    private String calcularTiempoRelativo(OffsetDateTime timestamp, OffsetDateTime ahora) {
        if (timestamp == null) return "Reciente";
        long minutos = ChronoUnit.MINUTES.between(timestamp, ahora);
        if (minutos < 1) return "Hace un momento";
        if (minutos < 60) return "Hace " + minutos + " min";
        long horas = ChronoUnit.HOURS.between(timestamp, ahora);
        if (horas < 24) return "Hace " + horas + " h";
        long dias = ChronoUnit.DAYS.between(timestamp, ahora);
        return "Hace " + dias + " d";
    }

    @GetMapping(path = "/test/v1/dashboard")
    public String testDashboard() {
        return "backoffice/test/dashboard";
    }

    @GetMapping(path = "/test/v2/dashboard")
    public String testDashboardV2() {
        return "backoffice/test/dashboardV2";
    }
}

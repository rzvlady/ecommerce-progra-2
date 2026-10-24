package com.sv.grupo1.backoffice.controller;

import com.sv.grupo1.backoffice.service.BackofficeClienteService;
import com.sv.grupo1.backoffice.service.BackofficeProductoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Map;

/*
 * @author alexemestica
 */
@Controller
@RequestMapping(path = "/backoffice")
@PreAuthorize("hasRole('ADMIN')")
public class DashboardController {

    private static final Logger LOG = LoggerFactory.getLogger(DashboardController.class);

    private final BackofficeProductoService productoService;
    private final BackofficeClienteService clienteService;

    public DashboardController(
            BackofficeProductoService productoService,
            BackofficeClienteService clienteService) {
        this.productoService = productoService;
        this.clienteService = clienteService;
    }

    @RequestMapping(path = "/dashboard")
    public String dashboard(Model model) {
        LOG.info("Dashboard Controller cargando indicadores");
        model.addAttribute("totalProductos", productoService.contarTotalProductos());

        Map<String, Long> metricasClientes = clienteService.obtenerMetricasDashboard();
        model.addAttribute("totalClientes", metricasClientes.getOrDefault("total", 0L));
        model.addAttribute("clientesActivos", metricasClientes.getOrDefault("activos", 0L));
        model.addAttribute("clientesInactivos", metricasClientes.getOrDefault("inactivos", 0L));
        model.addAttribute("clientesNuevosMes", metricasClientes.getOrDefault("nuevosMes", 0L));

        return "backoffice/dashboard";
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

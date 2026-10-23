package com.sv.grupo1.backoffice.controller;

import com.sv.grupo1.backoffice.dto.MovimientoInventarioDTO;
import com.sv.grupo1.backoffice.service.BackofficeInventarioService;
import com.sv.grupo1.ecommerce.entities.core.MovimientoInventario;
import com.sv.grupo1.ecommerce.entities.core.Producto;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.List;

@Controller
@RequestMapping(path = "/backoffice/inventario")
public class BackofficeInventarioController {

    private static final Logger LOG = LoggerFactory.getLogger(BackofficeInventarioController.class);

    private final BackofficeInventarioService inventarioService;

    public BackofficeInventarioController(BackofficeInventarioService inventarioService) {
        this.inventarioService = inventarioService;
    }

    @GetMapping
    public String vistaPrincipalInventario(
            @ModelAttribute("movimientoDTO") MovimientoInventarioDTO movimientoDTO,
            @RequestParam(name = "tab", required = false, defaultValue = "existencias") String tab,
            Model model) {
        LOG.info("Cargando vista principal de inventario en backoffice");
        cargarModeloVistaPrincipal(model);
        if (!model.containsAttribute("abrirModalMovimiento")) {
            model.addAttribute("abrirModalMovimiento", false);
        }
        model.addAttribute("tabActiva", tab);
        return "backoffice/inventario/lista";
    }

    @PostMapping("/movimiento")
    public String registrarMovimiento(
            @Valid @ModelAttribute("movimientoDTO") MovimientoInventarioDTO dto,
            BindingResult result,
            Model model,
            RedirectAttributes redirectAttributes) {

        if (result.hasErrors()) {
            cargarModeloVistaPrincipal(model);
            model.addAttribute("abrirModalMovimiento", true);
            model.addAttribute("tabActiva", "existencias");
            return "backoffice/inventario/lista";
        }

        try {
            Producto actualizado = inventarioService.registrarMovimiento(dto);
            String accion = "ENTRADA".equalsIgnoreCase(dto.getTipoMovimiento()) ? "Entrada" : "Salida";
            String signo = "ENTRADA".equalsIgnoreCase(dto.getTipoMovimiento()) ? "+" : "-";
            redirectAttributes.addFlashAttribute("mensajeExito",
                    accion + " registrada (" + signo + dto.getCantidad() + " uds) para '"
                            + actualizado.getNombreProducto() + "' [" + actualizado.getCodigoSku()
                            + "]. Stock actual: " + actualizado.getStockDisponible() + " uds.");
            return "redirect:/backoffice/inventario";
        } catch (IllegalArgumentException e) {
            cargarModeloVistaPrincipal(model);
            model.addAttribute("mensajeErrorModal", e.getMessage());
            model.addAttribute("abrirModalMovimiento", true);
            model.addAttribute("tabActiva", "existencias");
            return "backoffice/inventario/lista";
        }
    }

    @PostMapping("/{id}/stock-minimo")
    public String actualizarStockMinimo(
            @PathVariable("id") Integer idProducto,
            @RequestParam("stockMinimo") Integer stockMinimo,
            RedirectAttributes redirectAttributes) {

        try {
            Producto actualizado = inventarioService.actualizarStockMinimo(idProducto, stockMinimo);
            redirectAttributes.addFlashAttribute("mensajeExito",
                    "Umbral de stock mínimo actualizado a " + actualizado.getStockMinimo()
                            + " uds para '" + actualizado.getNombreProducto() + "' (" + actualizado.getCodigoSku() + ").");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("mensajeError", e.getMessage());
        }
        return "redirect:/backoffice/inventario";
    }

    @PostMapping("/escanear-alertas")
    public String ejecutarEscaneoManual(RedirectAttributes redirectAttributes) {
        inventarioService.ejecutarEscaneoMonitor();
        List<Producto> alertas = inventarioService.obtenerAlertasMonitor();
        if (alertas.isEmpty()) {
            redirectAttributes.addFlashAttribute("mensajeExito",
                    "Escaneo completado: Todos los productos tienen niveles óptimos de stock.");
        } else {
            redirectAttributes.addFlashAttribute("mensajeExito",
                    "Escaneo completado: Se detectaron " + alertas.size() + " producto(s) en nivel de alerta o agotados.");
        }
        return "redirect:/backoffice/inventario";
    }

    private void cargarModeloVistaPrincipal(Model model) {
        List<Producto> productos = inventarioService.listarProductos();
        List<MovimientoInventario> movimientos = inventarioService.listarMovimientos();
        List<Producto> alertasMonitor = inventarioService.obtenerAlertasMonitor();

        BigDecimal valorTotalInventario = productos.stream()
                .filter(p -> p.getStockDisponible() != null && p.getStockDisponible() > 0 && p.getPrecioCosto() != null)
                .map(p -> p.getPrecioCosto().multiply(BigDecimal.valueOf(p.getStockDisponible())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        long unidadesTotalesBodega = productos.stream()
                .filter(p -> p.getStockDisponible() != null && p.getStockDisponible() > 0)
                .mapToLong(Producto::getStockDisponible)
                .sum();

        long productosStockBajo = productos.stream()
                .filter(p -> p.getStockDisponible() != null && p.getStockMinimo() != null
                        && p.getStockDisponible() > 0 && p.getStockDisponible() <= p.getStockMinimo())
                .count();

        long productosAgotados = productos.stream()
                .filter(p -> p.getStockDisponible() == null || p.getStockDisponible() <= 0)
                .count();

        long totalMovimientos = movimientos.size();
        long totalEntradas = movimientos.stream()
                .filter(m -> "ENTRADA".equalsIgnoreCase(m.getTipoMovimiento()))
                .count();
        long totalSalidas = movimientos.stream()
                .filter(m -> "SALIDA".equalsIgnoreCase(m.getTipoMovimiento()))
                .count();

        model.addAttribute("productos", productos);
        model.addAttribute("movimientos", movimientos);
        model.addAttribute("alertasMonitor", alertasMonitor);
        model.addAttribute("categorias", inventarioService.listarCategorias());
        model.addAttribute("marcas", inventarioService.listarMarcas());

        model.addAttribute("valorTotalInventario", valorTotalInventario);
        model.addAttribute("unidadesTotalesBodega", unidadesTotalesBodega);
        model.addAttribute("productosStockBajo", productosStockBajo);
        model.addAttribute("productosAgotados", productosAgotados);
        model.addAttribute("totalAlertasCriticas", productosStockBajo + productosAgotados);
        model.addAttribute("totalMovimientos", totalMovimientos);
        model.addAttribute("totalEntradas", totalEntradas);
        model.addAttribute("totalSalidas", totalSalidas);
    }
}

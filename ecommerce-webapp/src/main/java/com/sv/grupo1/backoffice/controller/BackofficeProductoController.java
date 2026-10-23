package com.sv.grupo1.backoffice.controller;

import com.sv.grupo1.backoffice.dto.ProductoRegistroDTO;
import com.sv.grupo1.backoffice.service.BackofficeProductoService;
import com.sv.grupo1.ecommerce.entities.core.Producto;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping(path = "/backoffice/productos")
public class BackofficeProductoController {

    private static final Logger LOG = LoggerFactory.getLogger(BackofficeProductoController.class);

    private final BackofficeProductoService productoService;

    public BackofficeProductoController(BackofficeProductoService productoService) {
        this.productoService = productoService;
    }

    @GetMapping
    public String listarProductos(Model model) {
        LOG.info("Cargando vista principal de productos en backoffice");
        cargarModeloVistaPrincipal(model);
        if (!model.containsAttribute("productoDTO")) {
            model.addAttribute("productoDTO", new ProductoRegistroDTO());
        }
        if (!model.containsAttribute("modoEdicion")) {
            model.addAttribute("modoEdicion", false);
        }
        if (!model.containsAttribute("abrirModalFormulario")) {
            model.addAttribute("abrirModalFormulario", false);
        }
        return "backoffice/productos/lista";
    }

    @GetMapping("/nuevo")
    public String mostrarModalNuevo(Model model) {
        cargarModeloVistaPrincipal(model);
        model.addAttribute("productoDTO", new ProductoRegistroDTO());
        model.addAttribute("modoEdicion", false);
        model.addAttribute("abrirModalFormulario", true);
        return "backoffice/productos/lista";
    }

    @PostMapping("/guardar")
    public String guardarProducto(
            @Valid @ModelAttribute("productoDTO") ProductoRegistroDTO dto,
            BindingResult result,
            Model model,
            RedirectAttributes redirectAttributes) {

        if (result.hasErrors()) {
            cargarModeloVistaPrincipal(model);
            model.addAttribute("modoEdicion", false);
            model.addAttribute("abrirModalFormulario", true);
            return "backoffice/productos/lista";
        }

        try {
            productoService.registrarProducto(dto);
            redirectAttributes.addFlashAttribute("mensajeExito",
                    "Producto '" + dto.getNombreProducto().toUpperCase() + "' registrado con SKU: " + dto.getSku());
            return "redirect:/backoffice/productos";
        } catch (IllegalArgumentException e) {
            cargarModeloVistaPrincipal(model);
            model.addAttribute("mensajeErrorModal", e.getMessage());
            model.addAttribute("modoEdicion", false);
            model.addAttribute("abrirModalFormulario", true);
            return "backoffice/productos/lista";
        }
    }

    @GetMapping("/{id}/editar")
    public String mostrarModalEditar(
            @PathVariable("id") Integer idProducto,
            Model model,
            RedirectAttributes redirectAttributes) {

        try {
            ProductoRegistroDTO dto = productoService.obtenerDtoPorId(idProducto);
            cargarModeloVistaPrincipal(model);
            model.addAttribute("productoDTO", dto);
            model.addAttribute("modoEdicion", true);
            model.addAttribute("abrirModalFormulario", true);
            return "backoffice/productos/lista";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("mensajeError", e.getMessage());
            return "redirect:/backoffice/productos";
        }
    }

    @PostMapping("/{id}/actualizar")
    public String actualizarProducto(
            @PathVariable("id") Integer idProducto,
            @Valid @ModelAttribute("productoDTO") ProductoRegistroDTO dto,
            BindingResult result,
            Model model,
            RedirectAttributes redirectAttributes) {

        dto.setIdProducto(idProducto);

        if (result.hasErrors()) {
            cargarModeloVistaPrincipal(model);
            model.addAttribute("modoEdicion", true);
            model.addAttribute("abrirModalFormulario", true);
            return "backoffice/productos/lista";
        }

        try {
            productoService.actualizarProducto(idProducto, dto);
            redirectAttributes.addFlashAttribute("mensajeExito",
                    "Producto '" + dto.getNombreProducto().toUpperCase() + "' (" + dto.getSku() + ") actualizado correctamente.");
            return "redirect:/backoffice/productos";
        } catch (IllegalArgumentException e) {
            cargarModeloVistaPrincipal(model);
            model.addAttribute("mensajeErrorModal", e.getMessage());
            model.addAttribute("modoEdicion", true);
            model.addAttribute("abrirModalFormulario", true);
            return "backoffice/productos/lista";
        }
    }

    @PostMapping("/{id}/estado")
    public String cambiarEstadoProducto(
            @PathVariable("id") Integer idProducto,
            RedirectAttributes redirectAttributes) {

        try {
            productoService.cambiarEstado(idProducto);
            redirectAttributes.addFlashAttribute("mensajeExito",
                    "El estado del producto se actualizó correctamente.");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("mensajeError", e.getMessage());
        }
        return "redirect:/backoffice/productos";
    }

    @PostMapping("/{id}/eliminar")
    public String eliminarProducto(
            @PathVariable("id") Integer idProducto,
            RedirectAttributes redirectAttributes) {

        try {
            productoService.eliminarProducto(idProducto);
            redirectAttributes.addFlashAttribute("mensajeExito",
                    "El producto fue eliminado correctamente.");
        } catch (IllegalArgumentException | IllegalStateException e) {
            redirectAttributes.addFlashAttribute("mensajeError", e.getMessage());
        }
        return "redirect:/backoffice/productos";
    }

    private void cargarModeloVistaPrincipal(Model model) {
        List<Producto> todosProductos = productoService.listarProductos();

        long totalProductos = todosProductos.size();
        long productosActivos = todosProductos.stream()
                .filter(p -> Character.valueOf('A').equals(p.getEstado()))
                .count();
        long productosStockBajo = todosProductos.stream()
                .filter(p -> p.getStockDisponible() != null && p.getStockMinimo() != null
                        && p.getStockDisponible() > 0 && p.getStockDisponible() <= p.getStockMinimo())
                .count();
        long productosAgotados = todosProductos.stream()
                .filter(p -> p.getStockDisponible() != null && p.getStockDisponible() <= 0)
                .count();

        model.addAttribute("productos", todosProductos);
        model.addAttribute("categorias", productoService.listarCategorias());
        model.addAttribute("marcas", productoService.listarMarcas());

        model.addAttribute("totalProductos", totalProductos);
        model.addAttribute("productosActivos", productosActivos);
        model.addAttribute("productosStockBajo", productosStockBajo);
        model.addAttribute("productosAgotados", productosAgotados);
    }
}

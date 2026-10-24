package com.sv.grupo1.backoffice.controller;

import com.sv.grupo1.ecommerce.dto.MarcaDTO;
import com.sv.grupo1.ecommerce.services.MarcaService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping(path = "/backoffice/marcas")
@PreAuthorize("hasRole('ADMIN')")
public class BackofficeMarcaController {

    private static final Logger LOG = LoggerFactory.getLogger(BackofficeMarcaController.class);

    private final MarcaService marcaService;

    public BackofficeMarcaController(MarcaService marcaService) {
        this.marcaService = marcaService;
    }

    @GetMapping
    public String listarMarcas(Model model) {
        LOG.info("Cargando vista principal de marcas en backoffice");
        cargarModeloVistaPrincipal(model);

        if (!model.containsAttribute("marcaDTO")) {
            model.addAttribute("marcaDTO", new MarcaDTO());
        }
        if (!model.containsAttribute("modoEdicion")) {
            model.addAttribute("modoEdicion", false);
        }
        if (!model.containsAttribute("abrirModalFormulario")) {
            model.addAttribute("abrirModalFormulario", false);
        }

        return "backoffice/marcas/lista";
    }

    @GetMapping("/{id}/editar")
    public String mostrarModalEditar(
            @PathVariable("id") Integer idMarca,
            Model model,
            RedirectAttributes redirectAttributes) {

        try {
            MarcaDTO dto = marcaService.obtenerDtoPorId(idMarca);
            cargarModeloVistaPrincipal(model);
            model.addAttribute("marcaDTO", dto);
            model.addAttribute("modoEdicion", true);
            model.addAttribute("abrirModalFormulario", true);
            return "backoffice/marcas/lista";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("mensajeError", e.getMessage());
            return "redirect:/backoffice/marcas";
        }
    }

    @PostMapping("/guardar")
    public String guardarMarca(
            @Valid @ModelAttribute("marcaDTO") MarcaDTO dto,
            BindingResult result,
            Model model,
            RedirectAttributes redirectAttributes) {

        boolean esEdicion = dto.getIdMarca() != null;

        if (result.hasErrors()) {
            cargarModeloVistaPrincipal(model);
            model.addAttribute("modoEdicion", esEdicion);
            model.addAttribute("abrirModalFormulario", true);
            return "backoffice/marcas/lista";
        }

        try {
            marcaService.guardarMarca(dto);
            String accion = esEdicion ? "actualizada" : "registrada";
            redirectAttributes.addFlashAttribute(
                    "mensajeExito",
                    "Marca '" + dto.getNombreMarca().trim() + "' " + accion + " correctamente."
            );
            return "redirect:/backoffice/marcas";
        } catch (IllegalArgumentException | IllegalStateException e) {
            LOG.warn("Error de negocio al guardar marca: {}", e.getMessage());
            cargarModeloVistaPrincipal(model);
            model.addAttribute("mensajeErrorModal", e.getMessage());
            model.addAttribute("modoEdicion", esEdicion);
            model.addAttribute("abrirModalFormulario", true);
            return "backoffice/marcas/lista";
        }
    }

    @PostMapping("/{id}/estado")
    public String cambiarEstadoMarca(
            @PathVariable("id") Integer idMarca,
            RedirectAttributes redirectAttributes) {

        try {
            marcaService.cambiarEstado(idMarca);
            redirectAttributes.addFlashAttribute(
                    "mensajeExito",
                    "El estado de la marca se actualizó correctamente."
            );
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("mensajeError", e.getMessage());
        }
        return "redirect:/backoffice/marcas";
    }

    @PostMapping("/eliminar/{id}")
    public String eliminarMarca(
            @PathVariable("id") Integer idMarca,
            RedirectAttributes redirectAttributes) {

        try {
            marcaService.eliminarMarca(idMarca);
            redirectAttributes.addFlashAttribute(
                    "mensajeExito",
                    "La marca fue eliminada correctamente."
            );
        } catch (IllegalArgumentException | IllegalStateException e) {
            LOG.warn("No se pudo eliminar la marca ID {}: {}", idMarca, e.getMessage());
            redirectAttributes.addFlashAttribute("mensajeError", e.getMessage());
        }
        return "redirect:/backoffice/marcas";
    }

    private void cargarModeloVistaPrincipal(Model model) {
        List<MarcaDTO> marcas = marcaService.listarTodasConConteo();

        long totalMarcas = marcas.size();
        long marcasActivas = marcas.stream()
                .filter(m -> Character.valueOf('A').equals(m.getEstado()))
                .count();
        long marcasInactivas = totalMarcas - marcasActivas;
        long marcasConProductos = marcas.stream()
                .filter(m -> m.getCantidadProductos() > 0)
                .count();

        model.addAttribute("marcas", marcas);
        model.addAttribute("totalMarcas", totalMarcas);
        model.addAttribute("marcasActivas", marcasActivas);
        model.addAttribute("marcasInactivas", marcasInactivas);
        model.addAttribute("marcasConProductos", marcasConProductos);
    }
}

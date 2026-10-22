package com.sv.grupo1.backoffice.controller;

import com.sv.grupo1.ecommerce.dto.CategoriaDTO;
import com.sv.grupo1.ecommerce.services.CategoriaService;
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
@RequestMapping(path = "/backoffice/categorias")
public class BackofficeCategoriaController {

    private static final Logger LOG = LoggerFactory.getLogger(BackofficeCategoriaController.class);

    private final CategoriaService categoriaService;

    public BackofficeCategoriaController(CategoriaService categoriaService) {
        this.categoriaService = categoriaService;
    }

    @GetMapping
    public String listarCategorias(Model model) {
        LOG.info("Cargando vista principal de categorías en backoffice");
        cargarModeloVistaPrincipal(model);

        if (!model.containsAttribute("categoriaDTO")) {
            model.addAttribute("categoriaDTO", new CategoriaDTO());
        }
        if (!model.containsAttribute("modoEdicion")) {
            model.addAttribute("modoEdicion", false);
        }
        if (!model.containsAttribute("abrirModalFormulario")) {
            model.addAttribute("abrirModalFormulario", false);
        }

        return "backoffice/categorias/lista";
    }

    @GetMapping("/{id}/editar")
    public String mostrarModalEditar(
            @PathVariable("id") Integer idCategoria,
            Model model,
            RedirectAttributes redirectAttributes) {

        try {
            CategoriaDTO dto = categoriaService.obtenerDtoPorId(idCategoria);
            cargarModeloVistaPrincipal(model);
            model.addAttribute("categoriaDTO", dto);
            model.addAttribute("modoEdicion", true);
            model.addAttribute("abrirModalFormulario", true);
            return "backoffice/categorias/lista";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("mensajeError", e.getMessage());
            return "redirect:/backoffice/categorias";
        }
    }

    @PostMapping("/guardar")
    public String guardarCategoria(
            @Valid @ModelAttribute("categoriaDTO") CategoriaDTO dto,
            BindingResult result,
            Model model,
            RedirectAttributes redirectAttributes) {

        boolean esEdicion = dto.getIdCategoria() != null;

        if (result.hasErrors()) {
            cargarModeloVistaPrincipal(model);
            model.addAttribute("modoEdicion", esEdicion);
            model.addAttribute("abrirModalFormulario", true);
            return "backoffice/categorias/lista";
        }

        try {
            categoriaService.guardarCategoria(dto);
            String accion = esEdicion ? "actualizada" : "registrada";
            redirectAttributes.addFlashAttribute(
                    "mensajeExito",
                    "Categoría '" + dto.getNombreCategoria().trim() + "' " + accion + " correctamente."
            );
            return "redirect:/backoffice/categorias";
        } catch (IllegalArgumentException | IllegalStateException e) {
            LOG.warn("Error de negocio al guardar categoría: {}", e.getMessage());
            cargarModeloVistaPrincipal(model);
            model.addAttribute("mensajeErrorModal", e.getMessage());
            model.addAttribute("modoEdicion", esEdicion);
            model.addAttribute("abrirModalFormulario", true);
            return "backoffice/categorias/lista";
        }
    }

    @PostMapping("/eliminar/{id}")
    public String eliminarCategoria(
            @PathVariable("id") Integer idCategoria,
            RedirectAttributes redirectAttributes) {

        try {
            categoriaService.eliminarCategoria(idCategoria);
            redirectAttributes.addFlashAttribute(
                    "mensajeExito",
                    "La categoría fue eliminada correctamente."
            );
        } catch (IllegalArgumentException | IllegalStateException e) {
            LOG.warn("No se pudo eliminar la categoría ID {}: {}", idCategoria, e.getMessage());
            redirectAttributes.addFlashAttribute("mensajeError", e.getMessage());
        }
        return "redirect:/backoffice/categorias";
    }

    private void cargarModeloVistaPrincipal(Model model) {
        List<CategoriaDTO> categorias = categoriaService.listarTodasConNivel();
        List<CategoriaDTO> candidatasPadre = categoriaService.listarCandidatasPadre();

        long totalCategorias = categorias.size();
        long totalNivel1 = categorias.stream().filter(c -> Integer.valueOf(1).equals(c.getNivel())).count();
        long totalNivel2 = categorias.stream().filter(c -> Integer.valueOf(2).equals(c.getNivel())).count();
        long totalNivel3 = categorias.stream().filter(c -> Integer.valueOf(3).equals(c.getNivel())).count();

        model.addAttribute("categorias", categorias);
        model.addAttribute("candidatasPadre", candidatasPadre);

        model.addAttribute("totalCategorias", totalCategorias);
        model.addAttribute("totalNivel1", totalNivel1);
        model.addAttribute("totalNivel2", totalNivel2);
        model.addAttribute("totalNivel3", totalNivel3);
    }
}

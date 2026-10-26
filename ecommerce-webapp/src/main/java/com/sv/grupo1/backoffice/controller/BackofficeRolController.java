package com.sv.grupo1.backoffice.controller;

import com.sv.grupo1.ecommerce.dto.RolDTO;
import com.sv.grupo1.ecommerce.services.RolService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping(path = "/backoffice/roles")
@PreAuthorize("hasRole('ADMIN')")
public class BackofficeRolController {

    private static final Logger LOG = LoggerFactory.getLogger(BackofficeRolController.class);

    private final RolService rolService;

    public BackofficeRolController(RolService rolService) {
        this.rolService = rolService;
    }

    @GetMapping
    public String listarRoles(Model model) {
        LOG.info("Cargando vista principal de roles en backoffice");
        cargarModeloVistaPrincipal(model);

        if (!model.containsAttribute("rolDTO")) {
            model.addAttribute("rolDTO", new RolDTO());
        }
        if (!model.containsAttribute("modoEdicion")) {
            model.addAttribute("modoEdicion", false);
        }
        if (!model.containsAttribute("abrirModalFormulario")) {
            model.addAttribute("abrirModalFormulario", false);
        }

        return "backoffice/roles/lista";
    }

    @GetMapping("/{id}/editar")
    public String mostrarModalEditar(
            @PathVariable("id") Integer idRol,
            Model model,
            RedirectAttributes redirectAttributes) {

        try {
            RolDTO dto = rolService.obtenerDtoPorId(idRol);
            cargarModeloVistaPrincipal(model);
            model.addAttribute("rolDTO", dto);
            model.addAttribute("modoEdicion", true);
            model.addAttribute("abrirModalFormulario", true);
            return "backoffice/roles/lista";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("mensajeError", e.getMessage());
            return "redirect:/backoffice/roles";
        }
    }

    @PostMapping("/guardar")
    public String guardarRol(
            @Valid @ModelAttribute("rolDTO") RolDTO dto,
            BindingResult result,
            Model model,
            RedirectAttributes redirectAttributes) {

        boolean esEdicion = dto.getIdRol() != null;

        if (result.hasErrors()) {
            cargarModeloVistaPrincipal(model);
            model.addAttribute("modoEdicion", esEdicion);
            model.addAttribute("abrirModalFormulario", true);
            return "backoffice/roles/lista";
        }

        try {
            String nombreFinal = rolService.guardarRol(dto);
            String accion = esEdicion ? "actualizado" : "registrado";
            redirectAttributes.addFlashAttribute(
                    "mensajeExito",
                    "Rol '" + nombreFinal + "' " + accion + " correctamente."
            );
            return "redirect:/backoffice/roles";
        } catch (IllegalArgumentException | IllegalStateException e) {
            LOG.warn("Error de negocio al guardar rol: {}", e.getMessage());
            cargarModeloVistaPrincipal(model);
            model.addAttribute("mensajeErrorModal", e.getMessage());
            model.addAttribute("modoEdicion", esEdicion);
            model.addAttribute("abrirModalFormulario", true);
            return "backoffice/roles/lista";
        }
    }

    @PostMapping("/eliminar/{id}")
    public String eliminarRol(
            @PathVariable("id") Integer idRol,
            RedirectAttributes redirectAttributes) {

        try {
            rolService.eliminarRol(idRol);
            redirectAttributes.addFlashAttribute(
                    "mensajeExito",
                    "El rol fue eliminado correctamente."
            );
        } catch (IllegalArgumentException | IllegalStateException e) {
            LOG.warn("No se pudo eliminar el rol ID {}: {}", idRol, e.getMessage());
            redirectAttributes.addFlashAttribute("mensajeError", e.getMessage());
        }
        return "redirect:/backoffice/roles";
    }

    private void cargarModeloVistaPrincipal(Model model) {
        List<RolDTO> roles = rolService.listarTodos();

        long totalRoles = roles.size();
        long totalRolesSistema = roles.stream().filter(RolDTO::isRolSistema).count();
        long totalRolesPersonalizados = totalRoles - totalRolesSistema;
        long totalRolesConUsuarios = roles.stream().filter(r -> r.getCantidadUsuarios() > 0).count();

        model.addAttribute("roles", roles);
        model.addAttribute("totalRoles", totalRoles);
        model.addAttribute("totalRolesSistema", totalRolesSistema);
        model.addAttribute("totalRolesPersonalizados", totalRolesPersonalizados);
        model.addAttribute("totalRolesConUsuarios", totalRolesConUsuarios);
    }
}

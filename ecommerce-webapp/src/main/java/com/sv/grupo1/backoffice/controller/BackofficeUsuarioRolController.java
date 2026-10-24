package com.sv.grupo1.backoffice.controller;

import com.sv.grupo1.backoffice.dto.UsuarioRolAsignacionDTO;
import com.sv.grupo1.backoffice.service.BackofficeUsuarioRolService;
import com.sv.grupo1.ecommerce.dto.RolDTO;
import com.sv.grupo1.ecommerce.services.RolService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
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
@RequestMapping(path = "/backoffice/usuarios-roles")
@PreAuthorize("hasRole('ADMIN')")
public class BackofficeUsuarioRolController {

    private static final Logger LOG = LoggerFactory.getLogger(BackofficeUsuarioRolController.class);

    private final BackofficeUsuarioRolService usuarioRolService;
    private final RolService rolService;

    public BackofficeUsuarioRolController(
            BackofficeUsuarioRolService usuarioRolService,
            RolService rolService) {
        this.usuarioRolService = usuarioRolService;
        this.rolService = rolService;
    }

    @GetMapping
    public String listarUsuariosRoles(Model model, Authentication authentication) {
        LOG.info("Cargando vista de asignación de roles a usuarios en backoffice");
        cargarModeloVistaPrincipal(model, authentication);

        if (!model.containsAttribute("asignacionDTO")) {
            model.addAttribute("asignacionDTO", new UsuarioRolAsignacionDTO());
        }
        if (!model.containsAttribute("abrirModalFormulario")) {
            model.addAttribute("abrirModalFormulario", false);
        }

        return "backoffice/usuarios-roles/lista";
    }

    @GetMapping("/{id}/cambiar-rol")
    public String mostrarModalCambiarRol(
            @PathVariable("id") Integer idUsuario,
            Model model,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {

        try {
            UsuarioRolAsignacionDTO dto = usuarioRolService.obtenerPorIdUsuario(idUsuario);
            dto.setMotivo("");
            cargarModeloVistaPrincipal(model, authentication);
            model.addAttribute("asignacionDTO", dto);
            model.addAttribute("abrirModalFormulario", true);
            return "backoffice/usuarios-roles/lista";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("mensajeError", e.getMessage());
            return "redirect:/backoffice/usuarios-roles";
        }
    }

    @PostMapping("/cambiar-rol")
    public String cambiarRolUsuario(
            @Valid @ModelAttribute("asignacionDTO") UsuarioRolAsignacionDTO dto,
            BindingResult result,
            Authentication authentication,
            Model model,
            RedirectAttributes redirectAttributes) {

        if (result.hasErrors()) {
            enriquecerDatosVisualesUsuario(dto);
            cargarModeloVistaPrincipal(model, authentication);
            model.addAttribute("abrirModalFormulario", true);
            return "backoffice/usuarios-roles/lista";
        }

        String correoAdmin = authentication != null ? authentication.getName() : "ADMIN_BACKOFFICE";

        try {
            usuarioRolService.cambiarRolUsuario(dto, correoAdmin);
            redirectAttributes.addFlashAttribute(
                    "mensajeExito",
                    "El rol del usuario '" + dto.getCorreoInicioSesion() + "' fue actualizado a '"
                            + dto.getNombreRolActual() + "' correctamente."
            );
            return "redirect:/backoffice/usuarios-roles";
        } catch (IllegalArgumentException | IllegalStateException e) {
            LOG.warn("Error de negocio al cambiar rol de usuario: {}", e.getMessage());
            enriquecerDatosVisualesUsuario(dto);
            cargarModeloVistaPrincipal(model, authentication);
            model.addAttribute("mensajeErrorModal", e.getMessage());
            model.addAttribute("abrirModalFormulario", true);
            return "backoffice/usuarios-roles/lista";
        }
    }

    private void enriquecerDatosVisualesUsuario(UsuarioRolAsignacionDTO dto) {
        if (dto.getIdUsuario() != null) {
            try {
                UsuarioRolAsignacionDTO actual = usuarioRolService.obtenerPorIdUsuario(dto.getIdUsuario());
                dto.setCorreoInicioSesion(actual.getCorreoInicioSesion());
                dto.setNombreCompleto(actual.getNombreCompleto());
                dto.setNombreRolActual(actual.getNombreRolActual());
            } catch (IllegalArgumentException ignored) {
                // Si el usuario no existe, se mantiene el DTO tal cual
            }
        }
    }

    private void cargarModeloVistaPrincipal(Model model, Authentication authentication) {
        List<UsuarioRolAsignacionDTO> usuarios = usuarioRolService.listarUsuariosConRol();
        List<RolDTO> rolesDisponibles = rolService.listarTodos();

        long totalUsuarios = usuarios.size();
        long totalAdmins = usuarios.stream()
                .filter(u -> "ROLE_ADMIN".equalsIgnoreCase(u.getNombreRolActual()))
                .count();
        long totalClientes = usuarios.stream()
                .filter(u -> "ROLE_CLIENTE".equalsIgnoreCase(u.getNombreRolActual()))
                .count();
        long totalOtrosRoles = totalUsuarios - totalAdmins - totalClientes;

        String correoAdminEnSesion = authentication != null ? authentication.getName() : "";

        model.addAttribute("usuarios", usuarios);
        model.addAttribute("rolesDisponibles", rolesDisponibles);
        model.addAttribute("correoAdminEnSesion", correoAdminEnSesion);
        model.addAttribute("totalUsuarios", totalUsuarios);
        model.addAttribute("totalAdmins", totalAdmins);
        model.addAttribute("totalClientes", totalClientes);
        model.addAttribute("totalOtrosRoles", totalOtrosRoles);
    }
}

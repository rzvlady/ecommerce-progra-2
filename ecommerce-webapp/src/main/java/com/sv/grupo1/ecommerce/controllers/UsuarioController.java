package com.sv.grupo1.ecommerce.controllers;

import com.sv.grupo1.ecommerce.dto.UsuarioRegistroDTO;
import com.sv.grupo1.ecommerce.services.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping("/registro")
    public String mostrarFormularioRegistro(Model model) {
        model.addAttribute("usuarioDTO", new UsuarioRegistroDTO());
        return "usuarios/registro";
    }

    @PostMapping("/registrar")
    public String registrarUsuario(
            @Valid @ModelAttribute("usuarioDTO") UsuarioRegistroDTO dto,
            BindingResult result,
            Model model) {

        if (result.hasErrors()) {
            return "usuarios/registro";
        }

        try {
            usuarioService.registrarUsuario(
                    dto.getCorreoInicioSesion(),
                    dto.getContrasenia()
            );

            model.addAttribute(
                    "mensajeExito",
                    "Cuenta registrada correctamente."
            );

            model.addAttribute(
                    "usuarioDTO",
                    new UsuarioRegistroDTO()
            );

        } catch (IllegalArgumentException e) {
            model.addAttribute(
                    "mensajeError",
                    e.getMessage()
            );
        }

        return "usuarios/registro";
    }
}
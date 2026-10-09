package com.sv.grupo1.ecommerce.controllers;

import com.sv.grupo1.ecommerce.dto.RegistroUsuarioDTO;
import com.sv.grupo1.ecommerce.services.impl.UsuarioServiceImpl;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

//@Controller
//@RequestMapping("/usuarios")
public class UsuarioController {

    private final UsuarioServiceImpl usuarioService;

    public UsuarioController(UsuarioServiceImpl usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping("/registro")
    public String mostrarFormularioRegistro(Model model) {
        model.addAttribute("usuarioDTO", new RegistroUsuarioDTO());
        return "usuarios/registro";
    }

    /*@PostMapping("/registrar")
    public String registrarUsuario(
            @Valid @ModelAttribute("usuarioDTO") RegistroUsuarioDTO dto,
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
                    new RegistroUsuarioDTO()
            );

        } catch (IllegalArgumentException e) {
            model.addAttribute(
                    "mensajeError",
                    e.getMessage()
            );
        }

        return "usuarios/registro";
    }*/
}
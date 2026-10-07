package com.sv.grupo1.ecommerce.controllers;

import com.sv.grupo1.ecommerce.dto.RegistroUsuarioDTO;
import com.sv.grupo1.ecommerce.services.UsuarioService;
import com.sv.grupo1.ecommerce.services.impl.UsuarioServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

/*
 * @author alexemestica
 * */
@Controller
public class RegistroController {

    private static final Logger LOG = LoggerFactory.getLogger(RegistroController.class);
    private final UsuarioService usuarioService;

    public  RegistroController(UsuarioServiceImpl usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping("/registro")
    public String mostrarFormularioRegistro(Model model) {
        LOG.info("mostrarFormularioRegistro() => Cargando vista");

        model.addAttribute("registroDTO", new RegistroUsuarioDTO());
        return "pages/register/registro";
    }

    @PostMapping("/registro")
    public String registrarUsuario(@ModelAttribute("registroDTO")RegistroUsuarioDTO dto){
        LOG.info("registrarUsuario() => Iniciando");

        if (!dto.getContrasenia().equals(dto.getConfirmarContrasenia())) {
            return "redirect:/registro?error=contrasenas_no_coinciden";
        }
        this.usuarioService.registrarUsuario(dto);
        LOG.info("registrarUsuario() => Completado");
        return "redirect:/inicio-sesion?exito=true";
    }

}

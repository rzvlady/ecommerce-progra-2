package com.sv.grupo1.ecommerce.controllers;

import com.sv.grupo1.ecommerce.dao.UsuarioRepository;
import com.sv.grupo1.ecommerce.entities.catalogo.Categoria;
import com.sv.grupo1.ecommerce.entities.seguridad.Usuario;
import com.sv.grupo1.ecommerce.entities.seguridad.UsuarioPerfil;
import com.sv.grupo1.ecommerce.services.ProductoService;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.List;

@ControllerAdvice(basePackages = {"com.sv.grupo1.ecommerce.controllers", "com.sv.grupo1.backoffice.controller"})
public class GlobalNavbarControllerAdvice {

    private final ProductoService productoService;
    private final UsuarioRepository usuarioRepository;

    public GlobalNavbarControllerAdvice(
            ProductoService productoService,
            UsuarioRepository usuarioRepository) {
        this.productoService = productoService;
        this.usuarioRepository = usuarioRepository;
    }

    @ModelAttribute("categoriasNavbar")
    public List<Categoria> populateCategoriasNavbar() {
        return productoService.listarCategorias();
    }

    @ModelAttribute("correoUsuarioAutenticado")
    public String populateCorreoUsuarioAutenticado(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated() || authentication instanceof AnonymousAuthenticationToken) {
            return null;
        }
        return authentication.getName();
    }

    @ModelAttribute("nombreMostradoUsuario")
    public String populateNombreMostradoUsuario(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated() || authentication instanceof AnonymousAuthenticationToken) {
            return "Invitado";
        }

        String correo = authentication.getName();
        return usuarioRepository.findByCorreoInicioSesionIgnoreCase(correo)
                .map(usuario -> formatearPrimerNombreYApellido(usuario, correo))
                .orElse(correo);
    }

    private String formatearPrimerNombreYApellido(Usuario usuario, String correoFallback) {
        UsuarioPerfil perfil = usuario.getPerfil();
        if (perfil == null) {
            return correoFallback;
        }

        String nombres = perfil.getNombres() != null ? perfil.getNombres().trim() : "";
        String apellidos = perfil.getApellidos() != null ? perfil.getApellidos().trim() : "";

        if (nombres.isEmpty() || apellidos.isEmpty()) {
            return correoFallback;
        }

        String primerNombre = nombres.split("\\s+")[0];
        String primerApellido = apellidos.split("\\s+")[0];
        return primerNombre + " " + primerApellido;
    }
}

package com.sv.grupo1.ecommerce.controllers;

import com.sv.grupo1.ecommerce.entities.catalogo.Categoria;
import com.sv.grupo1.ecommerce.services.ProductoService;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.List;

@ControllerAdvice(basePackages = "com.sv.grupo1.ecommerce.controllers")
public class GlobalNavbarControllerAdvice {

    private final ProductoService productoService;

    public GlobalNavbarControllerAdvice(ProductoService productoService) {
        this.productoService = productoService;
    }

    @ModelAttribute("categoriasNavbar")
    public List<Categoria> populateCategoriasNavbar() {
        return productoService.listarCategorias();
    }
}

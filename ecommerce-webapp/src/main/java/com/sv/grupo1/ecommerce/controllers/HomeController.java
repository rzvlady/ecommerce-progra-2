package com.sv.grupo1.ecommerce.controllers;

import com.sv.grupo1.ecommerce.services.ProductoService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/*
 * @author alexemestica
 */
@Controller
public class HomeController {

    private final ProductoService productoService;

    public HomeController(ProductoService productoService) {
        this.productoService = productoService;
    }

    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("productosDestacados", productoService.obtenerProductosDestacadosHome());
        model.addAttribute("categoriasCarousel", productoService.obtenerCategoriasHome());
        model.addAttribute("categoriasNavbar", productoService.listarCategorias());
        return "ecommerce/pages/index";
    }

    /** Vista del carrito de compras */
    @GetMapping("/carrito")
    public String carrito() {
        return "ecommerce/pages/carrito";
    }
}

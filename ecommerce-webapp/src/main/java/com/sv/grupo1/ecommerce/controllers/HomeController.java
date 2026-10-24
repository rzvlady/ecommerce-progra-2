package com.sv.grupo1.ecommerce.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/*
* @author alexemestica
* */
@Controller
public class HomeController {

    @GetMapping("/")
    public String index() {
        return "ecommerce/pages/index";
    }

    @GetMapping("/old/inicio")
    public String indexOld() {
        return "index";
    }

    /** Vista provisional del carrito de compras */
    @GetMapping("/carrito")
    public String carrito() {
        return "ecommerce/pages/carrito";
    }

    /** Vista provisional: todas las categorías */
    @GetMapping("/categorias")
    public String categorias() {
        return "ecommerce/pages/categorias";
    }

    /** Vista provisional: productos de una categoría específica */
    @GetMapping("/categorias/{nombre}")
    public String categoria(@PathVariable("nombre") String nombre, Model model) {
        model.addAttribute("nombreCategoria", nombre);
        return "ecommerce/pages/categoria-detalle";
    }

    /** Vista provisional: detalle de un producto */
    @GetMapping("/producto/{nombre}")
    public String productoDetalle(@PathVariable("nombre") String nombre, Model model) {
        model.addAttribute("nombreProducto", nombre);
        return "ecommerce/pages/producto-detalle";
    }
}

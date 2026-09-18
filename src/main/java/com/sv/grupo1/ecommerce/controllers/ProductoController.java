package com.sv.grupo1.ecommerce.controllers;

import com.sv.grupo1.ecommerce.dto.ProductoRegistroDTO;
import com.sv.grupo1.ecommerce.services.ProductoService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin/productos")
public class ProductoController {

    private final ProductoService productoService;

    public ProductoController(ProductoService productoService) {
        this.productoService = productoService;
    }

    @GetMapping("/nuevo")
    public String mostrarFormulario(Model model) {
        // La clave debe coincidir exactamente con th:object="${productoDTO}"
        model.addAttribute("productoDTO", new ProductoRegistroDTO());
        return "admin/registro-producto";
    }

    @PostMapping("/guardar")
    public String guardarProducto(
            @Valid @ModelAttribute("productoDTO") ProductoRegistroDTO dto,
            BindingResult result,
            Model model) {

        if (result.hasErrors()) {
            return "admin/registro-producto";
        }

        try {
            productoService.registrarProducto(dto);
            model.addAttribute("mensajeExito", "¡Producto registrado exitosamente con el SKU: " + dto.getSku() + "!");
            // Limpia el formulario para un nuevo registro tras el éxito
            model.addAttribute("productoDTO", new ProductoRegistroDTO());
        } catch (IllegalArgumentException e) {
            model.addAttribute("mensajeError", e.getMessage());
        }

        return "admin/registro-producto";
    }
}
package com.sv.grupo1.ecommerce.controllers;

import com.sv.grupo1.ecommerce.services.ProductoService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/*
 * @author Miguel-A-Serrano
 */
@Controller
@RequestMapping("/productos")
public class ProductoController {

    private final ProductoService productoService;

    public ProductoController(ProductoService productoService) {
        this.productoService = productoService;
    }

    @GetMapping
    public String catalogo(
            @RequestParam(required = false) String busqueda,
            @RequestParam(required = false) Integer idCategoria,
            @RequestParam(required = false) Integer idMarca,
            Model model) {

        model.addAttribute("productos", productoService.filtrarProductos(busqueda, idCategoria, idMarca));
        model.addAttribute("categorias", productoService.listarCategorias());
        model.addAttribute("marcas", productoService.listarMarcas());
        model.addAttribute("busqueda", busqueda);
        model.addAttribute("idCategoriaSeleccionada", idCategoria);
        model.addAttribute("idMarcaSeleccionada", idMarca);

        return "ecommerce/pages/productos";
    }

    @GetMapping("/{id}")
    public String detalleProducto(@PathVariable("id") Integer id, Model model) {
        var producto = productoService.obtenerPorId(id)
                .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado: " + id));
        model.addAttribute("producto", producto);
        return "ecommerce/pages/producto-detalle";
    }
}

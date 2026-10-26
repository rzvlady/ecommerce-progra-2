package com.sv.grupo1.ecommerce.carrito;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.NoSuchElementException;

@Controller
@RequestMapping("/carrito")
public class CarritoWebController {

    private final CarritoSesionService carritoService;

    public CarritoWebController(CarritoSesionService carritoService) {
        this.carritoService = carritoService;
    }

    @GetMapping
    public String ver(Model model) {
        model.addAttribute("items", carritoService.items());
        model.addAttribute("resumen", carritoService.resumen());
        return "ecommerce/pages/carrito";
    }

    @PostMapping("/agregar")
    public String agregar(@RequestParam Integer idProducto,
                          @RequestParam(defaultValue = "1") int cantidad,
                          RedirectAttributes ra) {
        return ejecutar(ra, () -> carritoService.agregar(idProducto, cantidad), "Producto agregado al carrito.");
    }

    @PostMapping("/actualizar")
    public String actualizar(@RequestParam Integer idProducto,
                             @RequestParam int cantidad,
                             RedirectAttributes ra) {
        return ejecutar(ra, () -> carritoService.actualizar(idProducto, cantidad), null);
    }

    @PostMapping("/eliminar")
    public String eliminar(@RequestParam Integer idProducto, RedirectAttributes ra) {
        return ejecutar(ra, () -> carritoService.eliminar(idProducto), "Artículo eliminado.");
    }

    @PostMapping("/vaciar")
    public String vaciar(RedirectAttributes ra) {
        return ejecutar(ra, carritoService::vaciar, "Carrito vaciado.");
    }

    private String ejecutar(RedirectAttributes ra, Runnable accion, String exito) {
        try {
            accion.run();
            if (exito != null) {
                ra.addFlashAttribute("ok", exito);
            }
        } catch (IllegalArgumentException | IllegalStateException | NoSuchElementException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/carrito";
    }
}

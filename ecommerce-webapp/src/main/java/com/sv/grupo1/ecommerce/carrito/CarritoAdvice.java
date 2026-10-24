package com.sv.grupo1.ecommerce.carrito;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/**
 * Pone la cantidad de articulos del carrito en el modelo de TODAS las vistas web,
 * para que el encabezado pueda mostrarla con ${carritoCantidad}.
 */
@ControllerAdvice(annotations = Controller.class)
public class CarritoAdvice {

    private final CarritoSesionService carritoService;

    public CarritoAdvice(CarritoSesionService carritoService) {
        this.carritoService = carritoService;
    }

    @ModelAttribute("carritoCantidad")
    public int carritoCantidad() {
        return carritoService.resumen().getCantidadTotal();
    }
}

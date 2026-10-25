package com.sv.grupo1.ecommerce.controllers;

import com.sv.grupo1.ecommerce.carrito.CarritoItem;
import com.sv.grupo1.ecommerce.carrito.CarritoResumen;
import com.sv.grupo1.ecommerce.carrito.CarritoSesionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.Map;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/carrito")
public class CarritoApiController {

    private final CarritoSesionService carritoService;

    public CarritoApiController(CarritoSesionService carritoService) {
        this.carritoService = carritoService;
    }

    @PostMapping("/agregar")
    public ResponseEntity<?> agregar(
            @RequestParam("idProducto") Integer idProducto,
            @RequestParam(value = "cantidad", defaultValue = "1") int cantidad) {
        try {
            carritoService.agregar(idProducto, cantidad);
            CarritoResumen resumen = carritoService.resumen();
            return ResponseEntity.ok(Map.of(
                    "ok", true,
                    "mensaje", "Producto agregado al carrito exitosamente.",
                    "carritoCantidad", resumen.getCantidadTotal(),
                    "subtotal", resumen.getSubtotal(),
                    "total", resumen.getTotal()
            ));
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "ok", false,
                    "mensaje", e.getMessage()
            ));
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "ok", false,
                    "mensaje", e.getMessage()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "ok", false,
                    "mensaje", "No se pudo agregar el producto al carrito."
            ));
        }
    }

    @GetMapping("/resumen")
    public ResponseEntity<?> resumen() {
        CarritoResumen resumen = carritoService.resumen();
        return ResponseEntity.ok(Map.of(
                "ok", true,
                "carritoCantidad", resumen.getCantidadTotal(),
                "subtotal", resumen.getSubtotal(),
                "envio", resumen.getEnvio(),
                "total", resumen.getTotal()
        ));
    }

    @GetMapping("/items")
    public ResponseEntity<Collection<CarritoItem>> items() {
        return ResponseEntity.ok(carritoService.items());
    }

    @PostMapping("/actualizar")
    public ResponseEntity<?> actualizar(
            @RequestParam("idProducto") Integer idProducto,
            @RequestParam("cantidad") int cantidad) {
        try {
            carritoService.actualizar(idProducto, cantidad);
            CarritoResumen resumen = carritoService.resumen();
            return ResponseEntity.ok(Map.of(
                    "ok", true,
                    "carritoCantidad", resumen.getCantidadTotal(),
                    "subtotal", resumen.getSubtotal(),
                    "envio", resumen.getEnvio(),
                    "total", resumen.getTotal()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "ok", false,
                    "mensaje", e.getMessage()
            ));
        }
    }

    @PostMapping("/eliminar")
    public ResponseEntity<?> eliminar(@RequestParam("idProducto") Integer idProducto) {
        try {
            carritoService.eliminar(idProducto);
            CarritoResumen resumen = carritoService.resumen();
            return ResponseEntity.ok(Map.of(
                    "ok", true,
                    "carritoCantidad", resumen.getCantidadTotal(),
                    "subtotal", resumen.getSubtotal(),
                    "envio", resumen.getEnvio(),
                    "total", resumen.getTotal()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "ok", false,
                    "mensaje", e.getMessage()
            ));
        }
    }

    @PostMapping("/vaciar")
    public ResponseEntity<?> vaciar() {
        carritoService.vaciar();
        return ResponseEntity.ok(Map.of(
                "ok", true,
                "carritoCantidad", 0
        ));
    }
}

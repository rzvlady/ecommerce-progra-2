package com.sv.grupo1.ecommerce.controllers;

import com.sv.grupo1.ecommerce.entities.core.DetallePedido;
import com.sv.grupo1.ecommerce.services.CarritoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/carrito")
public class CarritoController {

    @Autowired
    private CarritoService carritoService;

    // En un caso real idCliente vendría del token de seguridad (JWT) o Sesión, aquí lo simulamos por Header
    @PostMapping("/agregar")
    public ResponseEntity<String> agregarProducto(
            @RequestHeader("X-Cliente-Id") Integer idCliente,
            @RequestParam Integer idProducto,
            @RequestParam Integer cantidad) {
        try {
            carritoService.agregarProductoAlCarrito(idCliente, idProducto, cantidad);
            return ResponseEntity.ok("Producto agregado al carrito exitosamente.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/")
    public ResponseEntity<List<DetallePedido>> verCarrito(@RequestHeader("X-Cliente-Id") Integer idCliente) {
        List<DetallePedido> carrito = carritoService.obtenerCarrito(idCliente);
        return ResponseEntity.ok(carrito);
    }

    @DeleteMapping("/eliminar")
    public ResponseEntity<String> eliminarProducto(
            @RequestHeader("X-Cliente-Id") Integer idCliente,
            @RequestParam Integer idProducto) {
        carritoService.eliminarProducto(idCliente, idProducto);
        return ResponseEntity.ok("Producto eliminado del carrito.");
    }

    @DeleteMapping("/vaciar")
    public ResponseEntity<String> vaciarCarrito(@RequestHeader("X-Cliente-Id") Integer idCliente) {
        carritoService.vaciarCarrito(idCliente);
        return ResponseEntity.ok("Carrito vaciado.");
    }

    @GetMapping("/total")
    public ResponseEntity<BigDecimal> obtenerTotal(@RequestHeader("X-Cliente-Id") Integer idCliente) {
        return ResponseEntity.ok(carritoService.calcularTotal(idCliente));
    }
}

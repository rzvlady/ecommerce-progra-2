package com.sv.grupo1.ecommerce.controllers;

import com.sv.grupo1.ecommerce.entities.core.Envio;
import com.sv.grupo1.ecommerce.services.PedidoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

//@RestController
//@RequestMapping("/api/pedidos")
public class PedidoController {

    //@Autowired
    private PedidoService pedidoService;

    @PostMapping("/{idPedido}/despachar")
    public ResponseEntity<?> despacharPedido(
            @PathVariable Integer idPedido,
            @RequestParam String empresaTransporte,
            @RequestParam String numeroSeguimiento) {
        try {
            Envio envio = pedidoService.despacharPedido(idPedido, empresaTransporte, numeroSeguimiento);
            return ResponseEntity.ok(envio);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
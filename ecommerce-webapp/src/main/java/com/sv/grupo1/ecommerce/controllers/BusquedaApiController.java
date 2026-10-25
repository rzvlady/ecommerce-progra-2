package com.sv.grupo1.ecommerce.controllers;

import com.sv.grupo1.ecommerce.dto.SugerenciaBusquedaDTO;
import com.sv.grupo1.ecommerce.services.ProductoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/busqueda")
public class BusquedaApiController {

    private final ProductoService productoService;

    public BusquedaApiController(ProductoService productoService) {
        this.productoService = productoService;
    }

    @GetMapping("/sugerencias")
    public ResponseEntity<SugerenciaBusquedaDTO> sugerencias(
            @RequestParam(name = "q", defaultValue = "") String q) {
        SugerenciaBusquedaDTO sugerencias = productoService.buscarSugerencias(q);
        return ResponseEntity.ok(sugerencias);
    }
}

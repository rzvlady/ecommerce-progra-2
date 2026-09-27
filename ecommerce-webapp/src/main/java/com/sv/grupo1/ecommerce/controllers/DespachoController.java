package com.sv.grupo1.ecommerce.controllers;

import java.util.List;
import java.util.ArrayList;
import org.springframework.beans.factory.annotation.Autowired;
import com.sv.grupo1.ecommerce.services.DespachoService;
import org.springframework.ui.Model;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import com.sv.grupo1.ecommerce.entities.core.Envio;

@Controller
public class DespachoController{
    @Autowired
    private DespachoService despachoService;

    @GetMapping("/despachos")
    public String verDespachos(Model model){
        List<Envio> listaEnvios = new ArrayList<>();
        despachoService.persistirDespachosEnBinario(listaEnvios);
        return"admin/despachos/lista";
    }
}
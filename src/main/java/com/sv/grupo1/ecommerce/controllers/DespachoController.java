package com.sv.grupo1.ecommerce.controllers;

import org.springframework.ui.Model;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DespachoController{

    @GetMapping("/despachos")
    public String verDespachos(Model model){
        return"admin/despachos/lista";
    }
}
package com.sv.grupo1.ecommerce.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation;

@Controller
public class DespachoController{

    @GetMapping("/despachos")
    public String verDespachos(){
        return"despachoslista";
    }
}
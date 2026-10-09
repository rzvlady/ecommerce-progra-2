package com.sv.grupo1.ecommerce.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/*
* @author alexemestica
* */
@Controller
public class HomeController {

    @GetMapping("/")
    public String index() {
        return "index";
    }
}

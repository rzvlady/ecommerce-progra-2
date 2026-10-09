package com.sv.grupo1.ecommerce.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/*
 * @author alexemestica
 * */
@Controller
public class LoginController {

    private static final Logger LOG = LoggerFactory.getLogger(LoginController.class);

    public LoginController() {

    }

    @GetMapping("/inicio-sesion")
    public String mostrarLogin() {
        LOG.info("mostrarLogin() => Cargando vista de inicio de sesion");
        return "pages/security/login";
    }

}

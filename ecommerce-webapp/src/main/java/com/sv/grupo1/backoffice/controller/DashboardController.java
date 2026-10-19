package com.sv.grupo1.backoffice.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/*
 * @author alexemestica
 */
@Controller
@RequestMapping(path = "/backoffice")
public class DashboardController {

    private static final Logger LOG = LoggerFactory.getLogger(DashboardController.class);

    @RequestMapping(path = "/dashboard")
    public String dashboard() {
        LOG.info("Dashboard Controller");
        return  "backoffice/dashboard";
    }

    @GetMapping(path = "/test/v1/dashboard")
    public String testDashboard() {
        return  "backoffice/test/dashboard";
    }

    @GetMapping(path = "/test/v2/dashboard")
    public String testDashboardV2() {
        return  "backoffice/test/dashboardV2";
    }
}

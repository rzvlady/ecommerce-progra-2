package com.sv.grupo1.ecommerce.controllers;

import com.sv.grupo1.ecommerce.dto.CheckoutDTO;
import com.sv.grupo1.ecommerce.dto.ResultadoCompraDTO;
import com.sv.grupo1.ecommerce.dto.ResumenCompraDTO;
import com.sv.grupo1.ecommerce.services.CheckoutService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;

@Controller
@RequestMapping("/checkout")
public class CheckoutController {

    private static final Logger LOG = LoggerFactory.getLogger(CheckoutController.class);

    private final CheckoutService checkoutService;

    public CheckoutController(CheckoutService checkoutService) {
        this.checkoutService = checkoutService;
    }

    @GetMapping
    public String mostrarCheckout(Model model, Principal principal, RedirectAttributes redirectAttributes) {
        LOG.info("mostrarCheckout() => Cargando vista");

        ResumenCompraDTO resumen = checkoutService.obtenerResumenCarrito(principal.getName());
        if (resumen.getItems().isEmpty()) {
            redirectAttributes.addFlashAttribute("mensajeError", "Tu carrito está vacío.");
            return "redirect:/carrito";
        }

        cargarModeloCheckout(model, principal.getName(), resumen);
        model.addAttribute("checkoutDTO", new CheckoutDTO());
        return "ecommerce/pages/checkout/checkout";
    }

    @PostMapping("/pagar")
    public String pagar(
            @Valid @ModelAttribute("checkoutDTO") CheckoutDTO dto,
            BindingResult result,
            Model model,
            Principal principal,
            RedirectAttributes redirectAttributes) {
        LOG.info("pagar() => Iniciando");

        if (result.hasErrors()) {
            return volverAlCheckout(model, principal.getName(), dto);
        }

        try {
            ResultadoCompraDTO resultado = checkoutService.finalizarCompra(principal.getName(), dto);
            if (resultado.isAprobado()) {
                redirectAttributes.addFlashAttribute("mensajeExito", resultado.getMensaje());
                LOG.info("pagar() => Completado");
                return "redirect:/checkout/confirmacion/" + resultado.getIdPedido();
            }
            model.addAttribute("mensajeError", resultado.getMensaje());
        } catch (IllegalArgumentException | IllegalStateException e) {
            model.addAttribute("mensajeError", e.getMessage());
        }
        return volverAlCheckout(model, principal.getName(), dto);
    }

    @GetMapping("/confirmacion/{idPedido}")
    public String mostrarConfirmacion(
            @PathVariable("idPedido") Integer idPedido,
            Model model,
            Principal principal,
            RedirectAttributes redirectAttributes) {
        LOG.info("mostrarConfirmacion() => Cargando vista del pedido {}", idPedido);

        try {
            model.addAttribute("resumen", checkoutService.obtenerConfirmacion(principal.getName(), idPedido));
            return "ecommerce/pages/checkout/confirmacion";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("mensajeError", e.getMessage());
            return "redirect:/";
        }
    }

    private String volverAlCheckout(Model model, String correo, CheckoutDTO dto) {
        dto.setNumeroTarjeta(null);
        dto.setCvv(null);
        cargarModeloCheckout(model, correo, checkoutService.obtenerResumenCarrito(correo));
        return "ecommerce/pages/checkout/checkout";
    }

    private void cargarModeloCheckout(Model model, String correo, ResumenCompraDTO resumen) {
        model.addAttribute("resumen", resumen);
        model.addAttribute("direcciones", checkoutService.listarDirecciones(correo));
        model.addAttribute("municipios", checkoutService.listarMunicipios());
    }
}

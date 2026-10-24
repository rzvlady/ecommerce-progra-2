package com.sv.grupo1.ecommerce.controllers;

import com.sv.grupo1.ecommerce.dto.CheckoutDTO;
import com.sv.grupo1.ecommerce.dto.ResultadoCompraDTO;
import com.sv.grupo1.ecommerce.dto.ResumenCompraDTO;
import com.sv.grupo1.ecommerce.services.CheckoutService;
import jakarta.servlet.http.HttpSession;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.time.YearMonth;
import java.util.UUID;

@Controller
@RequestMapping("/checkout")
public class CheckoutController {

    private static final Logger LOG = LoggerFactory.getLogger(CheckoutController.class);
    private static final String ATRIBUTO_TOKEN_PAGO = "tokenPago";
    private static final int ANIOS_MAXIMOS_VIGENCIA_TARJETA = 15;
    private static final String MENSAJE_CARRITO_VACIO = "Tu carrito está vacío.";
    private static final String MENSAJE_PAGO_VENCIDO =
            "Esta pantalla de pago ya no está vigente: tu compra ya fue procesada o tu carrito está vacío.";
    private static final String MENSAJE_FORMULARIO_VENCIDO =
            "Esta pantalla de pago ya no está vigente. Revisa tu pedido e ingresa nuevamente los datos de tu tarjeta.";

    private final CheckoutService checkoutService;

    public CheckoutController(CheckoutService checkoutService) {
        this.checkoutService = checkoutService;
    }

    @GetMapping
    public String mostrarCheckout(
            Model model,
            Principal principal,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        LOG.info("mostrarCheckout() => Cargando vista");

        ResumenCompraDTO resumen = checkoutService.obtenerResumenCarrito(principal.getName());
        if (resumen.getItems().isEmpty()) {
            redirectAttributes.addFlashAttribute("error", MENSAJE_CARRITO_VACIO);
            return "redirect:/carrito";
        }

        cargarModeloCheckout(model, principal.getName(), resumen, session);
        model.addAttribute("checkoutDTO", new CheckoutDTO());
        return "ecommerce/pages/checkout/checkout";
    }

    @PostMapping("/pagar")
    public String pagar(
            @Valid @ModelAttribute("checkoutDTO") CheckoutDTO dto,
            BindingResult result,
            @RequestParam(value = ATRIBUTO_TOKEN_PAGO, required = false) String tokenPago,
            Model model,
            Principal principal,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        LOG.info("pagar() => Iniciando");

        ResumenCompraDTO resumen = checkoutService.obtenerResumenCarrito(principal.getName());
        if (resumen.getItems().isEmpty()) {
            LOG.info("pagar() => Pantalla de pago vencida, el carrito esta vacio");
            redirectAttributes.addFlashAttribute("error", MENSAJE_PAGO_VENCIDO);
            return "redirect:/carrito";
        }

        if (!consumirTokenPago(session, tokenPago)) {
            LOG.info("pagar() => Token de pago invalido o ya utilizado");
            redirectAttributes.addFlashAttribute("mensajeError", MENSAJE_FORMULARIO_VENCIDO);
            return "redirect:/checkout";
        }

        validarFechaExpiracion(dto, result);
        if (result.hasErrors()) {
            return volverAlCheckout(model, principal.getName(), dto, resumen, session);
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
        return volverAlCheckout(model, principal.getName(), dto,
                checkoutService.obtenerResumenCarrito(principal.getName()), session);
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

    private boolean consumirTokenPago(HttpSession session, String tokenPago) {
        Object tokenEsperado = session.getAttribute(ATRIBUTO_TOKEN_PAGO);
        session.removeAttribute(ATRIBUTO_TOKEN_PAGO);
        return tokenPago != null && tokenPago.equals(tokenEsperado);
    }

    private void validarFechaExpiracion(CheckoutDTO dto, BindingResult result) {
        String fecha = dto.getFechaExpiracion();
        if (fecha == null || result.hasFieldErrors("fechaExpiracion")) {
            return;
        }
        YearMonth vencimiento = YearMonth.of(
                2000 + Integer.parseInt(fecha.substring(3)), Integer.parseInt(fecha.substring(0, 2)));
        YearMonth actual = YearMonth.now();
        if (vencimiento.isBefore(actual)) {
            result.rejectValue("fechaExpiracion", "tarjetaVencida", "La tarjeta está vencida");
        } else if (vencimiento.isAfter(actual.plusYears(ANIOS_MAXIMOS_VIGENCIA_TARJETA))) {
            result.rejectValue("fechaExpiracion", "fechaNoValida", "La fecha de expiración no es válida");
        }
    }

    private String volverAlCheckout(
            Model model,
            String correo,
            CheckoutDTO dto,
            ResumenCompraDTO resumen,
            HttpSession session) {
        dto.setNumeroTarjeta(null);
        dto.setCvv(null);
        cargarModeloCheckout(model, correo, resumen, session);
        return "ecommerce/pages/checkout/checkout";
    }

    private void cargarModeloCheckout(Model model, String correo, ResumenCompraDTO resumen, HttpSession session) {
        String tokenPago = UUID.randomUUID().toString();
        session.setAttribute(ATRIBUTO_TOKEN_PAGO, tokenPago);

        model.addAttribute(ATRIBUTO_TOKEN_PAGO, tokenPago);
        model.addAttribute("resumen", resumen);
        model.addAttribute("direcciones", checkoutService.listarDirecciones(correo));
        model.addAttribute("municipios", checkoutService.listarMunicipios());
    }
}

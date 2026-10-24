package com.sv.grupo1.ecommerce.carrito;

import com.sv.grupo1.ecommerce.carrito.PagoSimuladoService.Resultado;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.UUID;

@Controller
@RequestMapping("/checkout")
public class CheckoutController {

    private final CarritoSesionService carritoService;
    private final PagoSimuladoService pagoService;

    public CheckoutController(CarritoSesionService carritoService, PagoSimuladoService pagoService) {
        this.carritoService = carritoService;
        this.pagoService = pagoService;
    }

    @GetMapping
    public String formulario(Model model) {
        if (carritoService.items().isEmpty()) {
            return "redirect:/carrito";
        }
        model.addAttribute("items", carritoService.items());
        model.addAttribute("resumen", carritoService.resumen());
        return "ecommerce/pages/checkout";
    }

    /** Los datos de la tarjeta se usan solo aqui: no se guardan ni se escriben en logs. */
    @PostMapping("/pagar")
    public String pagar(@RequestParam String titular,
                        @RequestParam String tipo,
                        @RequestParam String numero,
                        @RequestParam String vencimiento,
                        @RequestParam String cvv,
                        RedirectAttributes ra) {
        if (carritoService.items().isEmpty()) {
            return "redirect:/carrito";
        }
        CarritoResumen resumen = carritoService.resumen();
        Resultado r = pagoService.procesar(numero, vencimiento, cvv);

        if (r == Resultado.TARJETA_INVALIDA || titular.trim().length() < 3) {
            ra.addFlashAttribute("error", "Revisa los datos de la tarjeta: alguno no es valido.");
            return "redirect:/checkout";
        }

        ra.addFlashAttribute("total", resumen.getTotal());
        ra.addFlashAttribute("tipo", tipo);
        ra.addFlashAttribute("ultimos4", pagoService.ultimosCuatro(numero));

        if (r == Resultado.APROBADO) {
            ra.addFlashAttribute("exito", true);
            ra.addFlashAttribute("referencia", "PED-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
            carritoService.vaciar();   // aqui tambien podrias guardar el pedido en core.pedidos
        } else {
            ra.addFlashAttribute("exito", false);
        }
        return "redirect:/checkout/resultado";
    }

    @GetMapping("/resultado")
    public String resultado(Model model) {
        if (!model.containsAttribute("exito")) {   // entrada directa sin haber pagado
            return "redirect:/carrito";
        }
        return "ecommerce/pages/pago-resultado";
    }
}

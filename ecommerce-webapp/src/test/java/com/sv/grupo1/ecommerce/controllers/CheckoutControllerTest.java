package com.sv.grupo1.ecommerce.controllers;

import com.sv.grupo1.ecommerce.dto.CheckoutDTO;
import com.sv.grupo1.ecommerce.dto.ItemCompraDTO;
import com.sv.grupo1.ecommerce.dto.ResultadoCompraDTO;
import com.sv.grupo1.ecommerce.dto.ResumenCompraDTO;
import com.sv.grupo1.ecommerce.services.CheckoutService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

import java.security.Principal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class CheckoutControllerTest {

    private static final String CORREO = "cliente@example.com";

    private final CheckoutService checkoutService = mock(CheckoutService.class);
    private final Principal principal = mock(Principal.class);

    private final CheckoutController controller = new CheckoutController(checkoutService);

    private final Model model = new ExtendedModelMap();
    private final RedirectAttributesModelMap redirectAttributes = new RedirectAttributesModelMap();

    @BeforeEach
    void prepararUsuarioAutenticado() {
        when(principal.getName()).thenReturn(CORREO);
    }

    @Test
    void debeRedirigirAlCarritoCuandoElCarritoEstaVacio() {
        when(checkoutService.obtenerResumenCarrito(CORREO)).thenReturn(new ResumenCompraDTO());

        String vista = controller.mostrarCheckout(model, principal, redirectAttributes);

        assertEquals("redirect:/carrito", vista);
        assertEquals("Tu carrito está vacío.", redirectAttributes.getFlashAttributes().get("mensajeError"));
        verify(checkoutService, never()).listarDirecciones(any());
    }

    @Test
    void debeCargarResumenDireccionesYMunicipiosAlMostrarElCheckout() {
        ResumenCompraDTO resumen = crearResumenConUnItem();
        when(checkoutService.obtenerResumenCarrito(CORREO)).thenReturn(resumen);

        String vista = controller.mostrarCheckout(model, principal, redirectAttributes);

        assertEquals("ecommerce/pages/checkout/checkout", vista);
        assertSame(resumen, model.getAttribute("resumen"));
        assertNotNull(model.getAttribute("direcciones"));
        assertNotNull(model.getAttribute("municipios"));
        assertInstanceOf(CheckoutDTO.class, model.getAttribute("checkoutDTO"));
    }

    @Test
    void noDebePagarCuandoHayErroresDeValidacion() {
        when(checkoutService.obtenerResumenCarrito(CORREO)).thenReturn(crearResumenConUnItem());
        CheckoutDTO dto = crearCheckoutDTO();
        BindingResult result = crearResultadoConErrores(dto);

        String vista = controller.pagar(dto, result, model, principal, redirectAttributes);

        assertEquals("ecommerce/pages/checkout/checkout", vista);
        assertNull(dto.getNumeroTarjeta());
        assertNull(dto.getCvv());
        verify(checkoutService, never()).finalizarCompra(any(), any());
    }

    @Test
    void noDebePagarConUnaTarjetaVencida() {
        when(checkoutService.obtenerResumenCarrito(CORREO)).thenReturn(crearResumenConUnItem());
        CheckoutDTO dto = crearCheckoutDTO();
        dto.setFechaExpiracion("01/20");
        BindingResult result = crearResultadoSinErrores(dto);

        String vista = controller.pagar(dto, result, model, principal, redirectAttributes);

        assertEquals("ecommerce/pages/checkout/checkout", vista);
        assertEquals("La tarjeta está vencida", result.getFieldError("fechaExpiracion").getDefaultMessage());
        verify(checkoutService, never()).finalizarCompra(any(), any());
    }

    @Test
    void debeRedirigirALaConfirmacionCuandoElPagoEsAprobado() {
        CheckoutDTO dto = crearCheckoutDTO();
        when(checkoutService.finalizarCompra(CORREO, dto))
                .thenReturn(crearResultado(true, "¡Pago aprobado! Tu pedido #7 fue confirmado."));

        String vista = controller.pagar(dto, crearResultadoSinErrores(dto), model, principal, redirectAttributes);

        assertEquals("redirect:/checkout/confirmacion/7", vista);
        assertEquals("¡Pago aprobado! Tu pedido #7 fue confirmado.",
                redirectAttributes.getFlashAttributes().get("mensajeExito"));
    }

    @Test
    void debeVolverAlCheckoutConMensajeYSinDatosDeTarjetaCuandoElPagoEsRechazado() {
        CheckoutDTO dto = crearCheckoutDTO();
        when(checkoutService.obtenerResumenCarrito(CORREO)).thenReturn(crearResumenConUnItem());
        when(checkoutService.finalizarCompra(CORREO, dto))
                .thenReturn(crearResultado(false, "Fondos insuficientes"));

        String vista = controller.pagar(dto, crearResultadoSinErrores(dto), model, principal, redirectAttributes);

        assertEquals("ecommerce/pages/checkout/checkout", vista);
        assertEquals("Fondos insuficientes", model.getAttribute("mensajeError"));
        assertNull(dto.getNumeroTarjeta());
        assertNull(dto.getCvv());
        assertEquals("Cliente Prueba", dto.getNombreTitular());
    }

    @Test
    void debeMostrarElErrorDeNegocioYVolverAlCheckout() {
        CheckoutDTO dto = crearCheckoutDTO();
        when(checkoutService.obtenerResumenCarrito(CORREO)).thenReturn(crearResumenConUnItem());
        when(checkoutService.finalizarCompra(CORREO, dto))
                .thenThrow(new IllegalArgumentException("Stock insuficiente para 'LAPTOP HP'. Disponible: 1 unidades."));

        String vista = controller.pagar(dto, crearResultadoSinErrores(dto), model, principal, redirectAttributes);

        assertEquals("ecommerce/pages/checkout/checkout", vista);
        assertEquals("Stock insuficiente para 'LAPTOP HP'. Disponible: 1 unidades.", model.getAttribute("mensajeError"));
        assertNull(dto.getCvv());
    }

    @Test
    void debeMostrarLaConfirmacionDelPedido() {
        ResumenCompraDTO resumen = crearResumenConUnItem();
        when(checkoutService.obtenerConfirmacion(CORREO, 7)).thenReturn(resumen);

        String vista = controller.mostrarConfirmacion(7, model, principal, redirectAttributes);

        assertEquals("ecommerce/pages/checkout/confirmacion", vista);
        assertSame(resumen, model.getAttribute("resumen"));
    }

    @Test
    void debeRedirigirAlInicioCuandoElPedidoNoEsDelUsuario() {
        when(checkoutService.obtenerConfirmacion(CORREO, 9))
                .thenThrow(new IllegalArgumentException("El pedido no pertenece a tu cuenta."));

        String vista = controller.mostrarConfirmacion(9, model, principal, redirectAttributes);

        assertEquals("redirect:/", vista);
        assertEquals("El pedido no pertenece a tu cuenta.", redirectAttributes.getFlashAttributes().get("mensajeError"));
    }

    private ResumenCompraDTO crearResumenConUnItem() {
        ResumenCompraDTO resumen = new ResumenCompraDTO();
        resumen.setItems(List.of(new ItemCompraDTO()));
        return resumen;
    }

    private ResultadoCompraDTO crearResultado(boolean aprobado, String mensaje) {
        ResultadoCompraDTO resultado = new ResultadoCompraDTO();
        resultado.setAprobado(aprobado);
        resultado.setIdPedido(7);
        resultado.setMensaje(mensaje);
        return resultado;
    }

    private BindingResult crearResultadoSinErrores(CheckoutDTO dto) {
        return new BeanPropertyBindingResult(dto, "checkoutDTO");
    }

    private BindingResult crearResultadoConErrores(CheckoutDTO dto) {
        BindingResult result = new BeanPropertyBindingResult(dto, "checkoutDTO");
        result.addError(new ObjectError("checkoutDTO", "El CVV debe tener 3 o 4 dígitos"));
        return result;
    }

    private CheckoutDTO crearCheckoutDTO() {
        CheckoutDTO dto = new CheckoutDTO();
        dto.setIdDireccion(1);
        dto.setNombreTitular("Cliente Prueba");
        dto.setNumeroTarjeta("4111111111111234");
        dto.setFechaExpiracion("12/35");
        dto.setCvv("123");
        return dto;
    }
}

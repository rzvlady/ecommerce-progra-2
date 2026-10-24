package com.sv.grupo1.ecommerce.controllers;

import com.sv.grupo1.ecommerce.dto.CheckoutDTO;
import com.sv.grupo1.ecommerce.dto.ItemCompraDTO;
import com.sv.grupo1.ecommerce.dto.ResultadoCompraDTO;
import com.sv.grupo1.ecommerce.dto.ResumenCompraDTO;
import com.sv.grupo1.ecommerce.services.CheckoutService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

import java.security.Principal;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class CheckoutControllerTest {

    private static final String CORREO = "cliente@example.com";
    private static final String TOKEN = "token-de-prueba";
    private static final String VISTA_CHECKOUT = "ecommerce/pages/checkout/checkout";
    private static final DateTimeFormatter FORMATO_EXPIRACION = DateTimeFormatter.ofPattern("MM/yy");

    private final CheckoutService checkoutService = mock(CheckoutService.class);
    private final Principal principal = mock(Principal.class);

    private final CheckoutController controller = new CheckoutController(checkoutService);

    private final Model model = new ExtendedModelMap();
    private final RedirectAttributesModelMap redirectAttributes = new RedirectAttributesModelMap();
    private final MockHttpSession session = new MockHttpSession();

    @BeforeEach
    void prepararUsuarioAutenticado() {
        when(principal.getName()).thenReturn(CORREO);
    }

    @Test
    void debeRedirigirAlCarritoCuandoElCarritoEstaVacio() {
        when(checkoutService.obtenerResumenCarrito(CORREO)).thenReturn(new ResumenCompraDTO());

        String vista = controller.mostrarCheckout(model, principal, session, redirectAttributes);

        assertEquals("redirect:/carrito", vista);
        assertEquals("Tu carrito está vacío.", redirectAttributes.getFlashAttributes().get("error"));
        verify(checkoutService, never()).listarDirecciones(any());
    }

    @Test
    void debeCargarResumenDireccionesMunicipiosYTokenAlMostrarElCheckout() {
        ResumenCompraDTO resumen = crearResumenConUnItem();
        when(checkoutService.obtenerResumenCarrito(CORREO)).thenReturn(resumen);

        String vista = controller.mostrarCheckout(model, principal, session, redirectAttributes);

        assertEquals(VISTA_CHECKOUT, vista);
        assertSame(resumen, model.getAttribute("resumen"));
        assertNotNull(model.getAttribute("direcciones"));
        assertNotNull(model.getAttribute("municipios"));
        assertInstanceOf(CheckoutDTO.class, model.getAttribute("checkoutDTO"));
        assertNotNull(model.getAttribute("tokenPago"));
        assertEquals(model.getAttribute("tokenPago"), session.getAttribute("tokenPago"));
    }

    @Test
    void noDebePagarCuandoLaPantallaDePagoYaNoEstaVigente() {
        when(checkoutService.obtenerResumenCarrito(CORREO)).thenReturn(new ResumenCompraDTO());
        CheckoutDTO dto = crearCheckoutDTO();

        String vista = pagarConTokenValido(dto, crearResultadoSinErrores(dto));

        assertEquals("redirect:/carrito", vista);
        assertEquals("Esta pantalla de pago ya no está vigente: tu compra ya fue procesada o tu carrito está vacío.",
                redirectAttributes.getFlashAttributes().get("error"));
        verify(checkoutService, never()).finalizarCompra(any(), any());
    }

    @Test
    void noDebePagarSinTokenDePago() {
        when(checkoutService.obtenerResumenCarrito(CORREO)).thenReturn(crearResumenConUnItem());
        CheckoutDTO dto = crearCheckoutDTO();

        String vista = controller.pagar(dto, crearResultadoSinErrores(dto), null,
                model, principal, session, redirectAttributes);

        assertEquals("redirect:/checkout", vista);
        assertEquals("Esta pantalla de pago ya no está vigente. Revisa tu pedido e ingresa nuevamente los datos de tu tarjeta.",
                redirectAttributes.getFlashAttributes().get("mensajeError"));
        verify(checkoutService, never()).finalizarCompra(any(), any());
    }

    @Test
    void noDebePagarConUnTokenDistintoAlDeLaSesion() {
        when(checkoutService.obtenerResumenCarrito(CORREO)).thenReturn(crearResumenConUnItem());
        session.setAttribute("tokenPago", TOKEN);
        CheckoutDTO dto = crearCheckoutDTO();

        String vista = controller.pagar(dto, crearResultadoSinErrores(dto), "token-de-otra-pestana",
                model, principal, session, redirectAttributes);

        assertEquals("redirect:/checkout", vista);
        assertNull(session.getAttribute("tokenPago"));
        verify(checkoutService, never()).finalizarCompra(any(), any());
    }

    @Test
    void noDebeProcesarDosVecesElMismoFormularioDePago() {
        when(checkoutService.obtenerResumenCarrito(CORREO)).thenReturn(crearResumenConUnItem());
        CheckoutDTO dto = crearCheckoutDTO();
        when(checkoutService.finalizarCompra(CORREO, dto))
                .thenReturn(crearResultado(false, "Fondos insuficientes"));

        pagarConTokenValido(dto, crearResultadoSinErrores(dto));
        String vista = controller.pagar(dto, crearResultadoSinErrores(dto), TOKEN,
                new ExtendedModelMap(), principal, session, redirectAttributes);

        assertEquals("redirect:/checkout", vista);
        verify(checkoutService, times(1)).finalizarCompra(any(), any());
    }

    @Test
    void noDebePagarCuandoHayErroresDeValidacionYDebeGenerarUnTokenNuevo() {
        when(checkoutService.obtenerResumenCarrito(CORREO)).thenReturn(crearResumenConUnItem());
        CheckoutDTO dto = crearCheckoutDTO();

        String vista = pagarConTokenValido(dto, crearResultadoConErrores(dto));

        assertEquals(VISTA_CHECKOUT, vista);
        assertNull(dto.getNumeroTarjeta());
        assertNull(dto.getCvv());
        assertNotEquals(TOKEN, session.getAttribute("tokenPago"));
        assertEquals(session.getAttribute("tokenPago"), model.getAttribute("tokenPago"));
        verify(checkoutService, never()).finalizarCompra(any(), any());
    }

    @Test
    void noDebePagarConUnaTarjetaVencida() {
        when(checkoutService.obtenerResumenCarrito(CORREO)).thenReturn(crearResumenConUnItem());
        CheckoutDTO dto = crearCheckoutDTO();
        dto.setFechaExpiracion("01/20");
        BindingResult result = crearResultadoSinErrores(dto);

        String vista = pagarConTokenValido(dto, result);

        assertEquals(VISTA_CHECKOUT, vista);
        assertEquals("La tarjeta está vencida", result.getFieldError("fechaExpiracion").getDefaultMessage());
        verify(checkoutService, never()).finalizarCompra(any(), any());
    }

    @Test
    void noDebeAceptarUnaFechaDeExpiracionDemasiadoLejana() {
        when(checkoutService.obtenerResumenCarrito(CORREO)).thenReturn(crearResumenConUnItem());
        CheckoutDTO dto = crearCheckoutDTO();
        dto.setFechaExpiracion(YearMonth.now().plusYears(16).format(FORMATO_EXPIRACION));
        BindingResult result = crearResultadoSinErrores(dto);

        String vista = pagarConTokenValido(dto, result);

        assertEquals(VISTA_CHECKOUT, vista);
        assertEquals("La fecha de expiración no es válida",
                result.getFieldError("fechaExpiracion").getDefaultMessage());
        verify(checkoutService, never()).finalizarCompra(any(), any());
    }

    @Test
    void debeAceptarUnaTarjetaQueVenceEsteMes() {
        when(checkoutService.obtenerResumenCarrito(CORREO)).thenReturn(crearResumenConUnItem());
        CheckoutDTO dto = crearCheckoutDTO();
        dto.setFechaExpiracion(YearMonth.now().format(FORMATO_EXPIRACION));
        when(checkoutService.finalizarCompra(CORREO, dto))
                .thenReturn(crearResultado(true, "¡Pago aprobado! Tu pedido #7 fue confirmado."));

        String vista = pagarConTokenValido(dto, crearResultadoSinErrores(dto));

        assertEquals("redirect:/checkout/confirmacion/7", vista);
    }

    @Test
    void debeRedirigirALaConfirmacionCuandoElPagoEsAprobado() {
        when(checkoutService.obtenerResumenCarrito(CORREO)).thenReturn(crearResumenConUnItem());
        CheckoutDTO dto = crearCheckoutDTO();
        when(checkoutService.finalizarCompra(CORREO, dto))
                .thenReturn(crearResultado(true, "¡Pago aprobado! Tu pedido #7 fue confirmado."));

        String vista = pagarConTokenValido(dto, crearResultadoSinErrores(dto));

        assertEquals("redirect:/checkout/confirmacion/7", vista);
        assertEquals("¡Pago aprobado! Tu pedido #7 fue confirmado.",
                redirectAttributes.getFlashAttributes().get("mensajeExito"));
        assertNull(session.getAttribute("tokenPago"));
    }

    @Test
    void debeVolverAlCheckoutConMensajeYSinDatosDeTarjetaCuandoElPagoEsRechazado() {
        when(checkoutService.obtenerResumenCarrito(CORREO)).thenReturn(crearResumenConUnItem());
        CheckoutDTO dto = crearCheckoutDTO();
        when(checkoutService.finalizarCompra(CORREO, dto))
                .thenReturn(crearResultado(false, "Fondos insuficientes"));

        String vista = pagarConTokenValido(dto, crearResultadoSinErrores(dto));

        assertEquals(VISTA_CHECKOUT, vista);
        assertEquals("Fondos insuficientes", model.getAttribute("mensajeError"));
        assertNull(dto.getNumeroTarjeta());
        assertNull(dto.getCvv());
        assertEquals("Cliente Prueba", dto.getNombreTitular());
        assertNotNull(session.getAttribute("tokenPago"));
    }

    @Test
    void debeMostrarElErrorDeNegocioYVolverAlCheckout() {
        when(checkoutService.obtenerResumenCarrito(CORREO)).thenReturn(crearResumenConUnItem());
        CheckoutDTO dto = crearCheckoutDTO();
        when(checkoutService.finalizarCompra(CORREO, dto))
                .thenThrow(new IllegalArgumentException("Stock insuficiente para 'LAPTOP HP'. Disponible: 1 unidades."));

        String vista = pagarConTokenValido(dto, crearResultadoSinErrores(dto));

        assertEquals(VISTA_CHECKOUT, vista);
        assertEquals("Stock insuficiente para 'LAPTOP HP'. Disponible: 1 unidades.", model.getAttribute("mensajeError"));
        assertNull(dto.getCvv());
    }

    @Test
    void debeMostrarElResumenActualizadoCuandoElTotalCambio() {
        ResumenCompraDTO resumenAnterior = crearResumenConUnItem();
        ResumenCompraDTO resumenActual = crearResumenConUnItem();
        when(checkoutService.obtenerResumenCarrito(CORREO)).thenReturn(resumenAnterior, resumenActual);
        CheckoutDTO dto = crearCheckoutDTO();
        when(checkoutService.finalizarCompra(CORREO, dto))
                .thenThrow(new IllegalStateException("El total de tu compra cambió de $100.00 a $120.00. Revisa el resumen antes de pagar."));

        String vista = pagarConTokenValido(dto, crearResultadoSinErrores(dto));

        assertEquals(VISTA_CHECKOUT, vista);
        assertSame(resumenActual, model.getAttribute("resumen"));
        assertEquals("El total de tu compra cambió de $100.00 a $120.00. Revisa el resumen antes de pagar.",
                model.getAttribute("mensajeError"));
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

    private String pagarConTokenValido(CheckoutDTO dto, BindingResult result) {
        session.setAttribute("tokenPago", TOKEN);
        return controller.pagar(dto, result, TOKEN, model, principal, session, redirectAttributes);
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

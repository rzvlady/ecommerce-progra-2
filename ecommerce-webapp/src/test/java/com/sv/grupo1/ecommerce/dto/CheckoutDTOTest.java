package com.sv.grupo1.ecommerce.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class CheckoutDTOTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void debeAceptarLosDatosDeUnPagoValido() {
        assertTrue(validator.validate(crearCheckoutDTO()).isEmpty());
    }

    @Test
    void debeAceptarElNombreConTildesYEspacios() {
        CheckoutDTO dto = crearCheckoutDTO();
        dto.setNombreTitular("José Ángel Peña-Núñez");

        assertTrue(validator.validate(dto).isEmpty());
    }

    @Test
    void noDebeAceptarElNombreDelTitularConNumeros() {
        CheckoutDTO dto = crearCheckoutDTO();
        dto.setNombreTitular("Cliente 123");

        assertEquals(1, validarCampo(dto, "nombreTitular").size());
    }

    @Test
    void noDebeAceptarElNombreDelTitularMuyCorto() {
        CheckoutDTO dto = crearCheckoutDTO();
        dto.setNombreTitular("Al");

        assertEquals(1, validarCampo(dto, "nombreTitular").size());
    }

    @Test
    void soloDebeMostrarUnMensajeCuandoElNombreDelTitularVieneVacio() {
        CheckoutDTO dto = crearCheckoutDTO();
        dto.setNombreTitular("");

        assertEquals(1, validarCampo(dto, "nombreTitular").size());
    }

    @Test
    void noDebeAceptarUnNumeroDeTarjetaDeQuinceDigitos() {
        CheckoutDTO dto = crearCheckoutDTO();
        dto.setNumeroTarjeta("411111111111123");

        assertEquals(1, validarCampo(dto, "numeroTarjeta").size());
    }

    @Test
    void debeQuitarEspaciosYGuionesDelNumeroDeTarjeta() {
        CheckoutDTO dto = crearCheckoutDTO();
        dto.setNumeroTarjeta("4111 1111-1111 1234");

        assertEquals("4111111111111234", dto.getNumeroTarjeta());
        assertTrue(validarCampo(dto, "numeroTarjeta").isEmpty());
    }

    @Test
    void debePermitirLimpiarElNumeroDeTarjeta() {
        CheckoutDTO dto = crearCheckoutDTO();
        dto.setNumeroTarjeta(null);

        assertNull(dto.getNumeroTarjeta());
    }

    @Test
    void noDebeAceptarUnCodigoPostalConLetras() {
        CheckoutDTO dto = crearCheckoutDTO();
        dto.setCodigoPostal("15A1");

        assertEquals(1, validarCampo(dto, "codigoPostal").size());
    }

    @Test
    void debeAceptarElCodigoPostalVacio() {
        CheckoutDTO dto = crearCheckoutDTO();
        dto.setCodigoPostal("");

        assertTrue(validarCampo(dto, "codigoPostal").isEmpty());
    }

    @Test
    void noDebeAceptarUnaDireccionConMasDeCientoCincuentaCaracteres() {
        CheckoutDTO dto = crearCheckoutDTO();
        dto.setDireccionLinea1("a".repeat(151));

        assertEquals(1, validarCampo(dto, "direccionLinea1").size());
    }

    @Test
    void noDebeAceptarUnaDireccionDeMenosDeDiezCaracteres() {
        CheckoutDTO dto = crearCheckoutDTO();
        dto.setDireccionLinea1("Calle 1");

        assertEquals(1, validarCampo(dto, "direccionLinea1").size());
    }

    @Test
    void debeAceptarLaDireccionVaciaCuandoSeEligeUnaExistente() {
        CheckoutDTO dto = crearCheckoutDTO();
        dto.setDireccionLinea1("");

        assertTrue(validarCampo(dto, "direccionLinea1").isEmpty());
    }

    private Set<ConstraintViolation<CheckoutDTO>> validarCampo(CheckoutDTO dto, String campo) {
        return validator.validateProperty(dto, campo);
    }

    private CheckoutDTO crearCheckoutDTO() {
        CheckoutDTO dto = new CheckoutDTO();
        dto.setIdDireccion(1);
        dto.setNombreTitular("Cliente Prueba");
        dto.setNumeroTarjeta("4111111111111234");
        dto.setFechaExpiracion("12/30");
        dto.setCvv("123");
        return dto;
    }
}

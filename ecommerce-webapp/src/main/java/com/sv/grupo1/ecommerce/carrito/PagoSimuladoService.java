package com.sv.grupo1.ecommerce.carrito;

import org.springframework.stereotype.Service;

import java.time.YearMonth;

/**
 * Simula el cobro de una tarjeta. NO se conecta a ningun banco y NO guarda
 * ni registra datos de la tarjeta. Para produccion usar una pasarela de pagos.
 *
 * Regla de la simulacion: una tarjeta valida que termina en 9995 se rechaza
 * por fondos insuficientes; cualquier otra tarjeta valida se aprueba.
 * Tarjetas de prueba: 4242 4242 4242 4242 (aprobada) y 4000 0000 0000 9995 (sin fondos).
 */
@Service
public class PagoSimuladoService {

    public enum Resultado { APROBADO, FONDOS_INSUFICIENTES, TARJETA_INVALIDA }

    public Resultado procesar(String numero, String vencimiento, String cvv) {
        String n = numero == null ? "" : numero.replaceAll("\\D", "");
        if (n.length() < 13 || n.length() > 19 || !luhn(n)) {
            return Resultado.TARJETA_INVALIDA;
        }
        if (!vencimientoValido(vencimiento)) {
            return Resultado.TARJETA_INVALIDA;
        }
        if (cvv == null || !cvv.matches("\\d{3,4}")) {
            return Resultado.TARJETA_INVALIDA;
        }
        return n.endsWith("9995") ? Resultado.FONDOS_INSUFICIENTES : Resultado.APROBADO;
    }

    public String ultimosCuatro(String numero) {
        String n = numero == null ? "" : numero.replaceAll("\\D", "");
        return n.length() <= 4 ? n : n.substring(n.length() - 4);
    }

    private boolean vencimientoValido(String v) {
        if (v == null || !v.matches("\\d{2}/\\d{2}")) {
            return false;
        }
        int mes = Integer.parseInt(v.substring(0, 2));
        int anio = 2000 + Integer.parseInt(v.substring(3));
        if (mes < 1 || mes > 12) {
            return false;
        }
        return !YearMonth.of(anio, mes).isBefore(YearMonth.now());
    }

    private boolean luhn(String n) {
        int suma = 0;
        boolean doble = false;
        for (int i = n.length() - 1; i >= 0; i--) {
            int d = n.charAt(i) - '0';
            if (doble) {
                d *= 2;
                if (d > 9) {
                    d -= 9;
                }
            }
            suma += d;
            doble = !doble;
        }
        return suma % 10 == 0;
    }
}

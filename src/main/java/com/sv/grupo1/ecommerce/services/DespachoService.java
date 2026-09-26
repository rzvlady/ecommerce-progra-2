package com.sv.grupo1.ecommerce.services;

import org.springframework.stereotype.Service;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.List;

@Service
public class DespachoService {

    private static final String RUTA_ARCHIVO = "despachos_temporal.dat";
  
    public void persistirDespachosEnBinario(List<Envio> listaEnvios) {

        try (FileOutputStream fileOut = new FileOutputStream(RUTA_ARCHIVO);
             ObjectOutputStream objectOut = new ObjectOutputStream(fileOut)) {

            objectOut.writeObject(listaEnvios);
            System.out.println(">> [ÉXITO] Archivo .dat generado correctamente como buffer temporal.");

        } catch (IOException e) {
            System.err.println(">> [ERROR] Ocurrió un fallo de E/S al escribir el archivo binario: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
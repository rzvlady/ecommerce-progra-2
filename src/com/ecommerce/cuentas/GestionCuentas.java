package com.ecommerce.cuentas;

import java.util.ArrayList;

public class GestionCuentas {

    private ArrayList<Usuario> usuarios;

    public GestionCuentas() {
        usuarios = new ArrayList<>();
    }

    public void registrarUsuario(String nombre, String correo, String contrasena) {

        if (nombre.isBlank() || correo.isBlank() || contrasena.isBlank()) {
            System.out.println("Todos los campos son obligatorios.");
            return;
        }

        if (!correo.contains("@")) {
            System.out.println("El correo ingresado no es válido.");
            return;
        }

        if (contrasena.length() < 4) {
            System.out.println("La contraseña debe tener al menos 4 caracteres.");
            return;
        }

        if (buscarUsuario(correo) != null) {
            System.out.println("Ya existe una cuenta con ese correo.");
            return;
        }

        Usuario nuevoUsuario = new Usuario(nombre, correo, contrasena);
        usuarios.add(nuevoUsuario);

        System.out.println("Cuenta registrada correctamente.");
    }

    public Usuario buscarUsuario(String correo) {

        for (Usuario usuario : usuarios) {
            if (usuario.getCorreo().equalsIgnoreCase(correo)) {
                return usuario;
            }
        }

        return null;
    }

    public void mostrarUsuarios() {

        if (usuarios.isEmpty()) {
            System.out.println("No hay cuentas registradas.");
            return;
        }

        System.out.println("Cuentas registradas:");

        for (Usuario usuario : usuarios) {
            System.out.println(usuario);
        }
    }
}

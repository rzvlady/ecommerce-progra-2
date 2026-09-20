package com.ecommerce.cuentas;

import java.util.Scanner;

public class Main {

    public static void main(String[] args) {

        Scanner scanner = new Scanner(System.in);
        GestionCuentas gestion = new GestionCuentas();

        int opcion;

        do {
            System.out.println("\n===== GESTIÓN DE CUENTAS =====");
            System.out.println("1. Registrar cuenta");
            System.out.println("2. Buscar cuenta");
            System.out.println("3. Mostrar cuentas");
            System.out.println("4. Salir");
            System.out.print("Seleccione una opción: ");

            opcion = scanner.nextInt();
            scanner.nextLine();

            switch (opcion) {

                case 1:
                    System.out.println("\n--- REGISTRAR CUENTA ---");

                    System.out.print("Nombre: ");
                    String nombre = scanner.nextLine();

                    while (nombre.isBlank() || !nombre.matches("[a-zA-ZáéíóúÁÉÍÓÚñÑ]+")) {
                        System.out.println("El nombre solo puede contener letras y no debe llevar espacios.");
                        System.out.print("Ingrese nuevamente el nombre: ");
                        nombre = scanner.nextLine();
                    }

                    System.out.print("Correo: ");
                    String correo = scanner.nextLine();

                    while (!correo.contains("@") || !correo.endsWith(".com")) {
                        System.out.println("El correo no es válido. Debe incluir @ y terminar en .com.");
                        System.out.print("Ingrese nuevamente el correo: ");
                        correo = scanner.nextLine();
                    }

                    System.out.print("Contraseña: ");
                    String contrasena = scanner.nextLine();

                    while (contrasena.length() < 5) {
                        System.out.println("La contraseña debe tener al menos 5 caracteres.");
                        System.out.print("Ingrese nuevamente la contraseña: ");
                        contrasena = scanner.nextLine();
                    }

                    gestion.registrarUsuario(nombre, correo, contrasena);
                    break;

                case 2:
                    System.out.println("\n--- BUSCAR CUENTA ---");

                    System.out.print("Ingrese el correo: ");
                    String correoBuscar = scanner.nextLine();

                    Usuario usuarioEncontrado = gestion.buscarUsuario(correoBuscar);

                    if (usuarioEncontrado != null) {
                        System.out.println("Cuenta encontrada:");
                        System.out.println(usuarioEncontrado);
                    } else {
                        System.out.println("No se encontró una cuenta con ese correo.");
                    }
                    break;

                case 3:
                    System.out.println();
                    gestion.mostrarUsuarios();
                    break;

                case 4:
                    System.out.println("Saliendo de gestión de cuentas...");
                    break;

                default:
                    System.out.println("Opción no válida.");
            }

        } while (opcion != 4);

        scanner.close();
    }
}

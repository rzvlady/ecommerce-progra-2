package com.sv.grupo1.ecommerce;

import com.sv.grupo1.ecommerce.dto.RegistroUsuarioDTO;
import com.sv.grupo1.ecommerce.services.impl.UsuarioServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ECommerceApplication {

	private static final Logger LOG = LoggerFactory.getLogger(ECommerceApplication.class);

	public static void main(String[] args) {
		SpringApplication.run(ECommerceApplication.class, args);
	}

	/*@Bean*/
	public CommandLineRunner probarRegistro(UsuarioServiceImpl usuarioService) {
		return args -> {
			LOG.info("=== INICIANDO PRUEBA DE REGISTRO DESDE COMMAND LINE RUNNER ===");

			try {
				// 1. Preparamos el DTO simulando los datos que vendrían del formulario web
				RegistroUsuarioDTO dto = new RegistroUsuarioDTO();
				dto.setCorreoInicioSesion("cliente.prueba-001@ecommerce.sv");
				dto.setContrasenia("MiPasswordSecreto123");
				dto.setConfirmarContrasenia("MiPasswordSecreto123");

				// 2. Ejecutamos el servicio
				usuarioService.registrarUsuario(dto);

				LOG.info("¡ÉXITO! Usuario y contacto registrados correctamente en la base de datos.");

			} catch (IllegalArgumentException e) {
				// Esto capturará la validación si intentas registrar el mismo correo dos veces
				LOG.warn("Validación esperada: {}", e.getMessage());
			} catch (Exception e) {
				LOG.error("Ocurrió un error inesperado al guardar: ", e);
			}

			LOG.info("=== FIN DE LA PRUEBA ===");
		};
	}

}

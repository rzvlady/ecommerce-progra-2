package com.sv.grupo1.ecommerce.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/*
 * @author stvipeerxz01
 * */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
            // Desactivamos CSRF temporalmente si tu colega lo necesita así,
            // aunque para formularios Thymeleaf es mejor dejarlo activo después.
            .csrf(csrf -> csrf.disable())

            // Unificamos TODO en un solo bloque authorizeHttpRequests
            .authorizeHttpRequests(auth -> auth
                // 1. Rutas de la API (lo de tu colega)
                .requestMatchers("/api/auth/**").permitAll()

                // 2. Rutas web públicas (las nuestras)
                .requestMatchers("/", "/registro", "/inicio-sesion", "/css/**", "/js/**").permitAll()

                // 3. LA REGLA FINAL SIEMPRE DEBE SER anyRequest()
                // Aquí decidimos: o todo es público por ahora, o lo protegemos.
                // Usaremos tu regla de proteger el resto de la app web.
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/inicio-sesion")
                .loginProcessingUrl("/inicio-sesion")
                .defaultSuccessUrl("/", true)
                .failureUrl("/inicio-sesion?error=true")
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/cerrar-sesion")
                .logoutSuccessUrl("/")
                .permitAll()
            );

        return http.build();
    }
}

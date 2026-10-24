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

    private final CustomAuthenticationSuccessHandler customAuthenticationSuccessHandler;

    public SecurityConfig(CustomAuthenticationSuccessHandler customAuthenticationSuccessHandler) {
        this.customAuthenticationSuccessHandler = customAuthenticationSuccessHandler;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
        .csrf(csrf -> csrf.disable())
        .authorizeHttpRequests(auth -> auth
            // 1. Rutas de la API (lo de tu colega)
            .requestMatchers("/api/auth/**").permitAll()

            // 2. PROTEGER EL BACKOFFICE (Nueva regla añadida aquí)
            // Exige que el usuario tenga el rol de ADMIN.
            // Si aún no usas roles, cámbialo por .authenticated() temporalmente.
            .requestMatchers("/backoffice/**").hasRole("ADMIN")

            // 3. Rutas web públicas (las nuestras sin el backoffice)
            .requestMatchers(
                    "/",
                    "/registro",
                    "/inicio-sesion",
                    "/css/**", "/js/**", "/ecommerce/**",
                    "/uploads/**",
                    "/carrito", "/carrito/**", "/categorias", "/categorias/**",
                    "/producto/**", "/productos", "/productos/**"
            ).permitAll()
            // 4. LA REGLA FINAL SIEMPRE DEBE SER anyRequest()
            .anyRequest().authenticated()
        )
        .formLogin(form -> form
            .loginPage("/inicio-sesion")
            .loginProcessingUrl("/inicio-sesion")
            .successHandler(customAuthenticationSuccessHandler)
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

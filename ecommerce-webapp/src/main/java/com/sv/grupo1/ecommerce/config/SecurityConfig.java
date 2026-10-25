package com.sv.grupo1.ecommerce.config;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final CustomAuthenticationSuccessHandler customAuthenticationSuccessHandler;

    public SecurityConfig(@Lazy CustomAuthenticationSuccessHandler customAuthenticationSuccessHandler) {
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
            .requestMatchers("/api/auth/**", "/api/busqueda/**", "/api/carrito/**").permitAll()

            .requestMatchers("/backoffice/**", "/admin/**").hasRole("ADMIN")

            // 3. Rutas web públicas
            .requestMatchers(
                    "/",
                    "/registro",
                    "/inicio-sesion",
                    "/login",
                    "/error",
                    "/css/**", "/js/**", "/ecommerce/**",
                    "/uploads/**",
                    "/carrito", "/carrito/**", "/categorias", "/categorias/**",
                    "/producto/**", "/productos", "/productos/**"
            ).permitAll()
            .anyRequest().authenticated()
        )
        .formLogin(form -> form
            .loginPage("/inicio-sesion")
            .loginProcessingUrl("/inicio-sesion")
            .successHandler(customAuthenticationSuccessHandler)
            .failureHandler((req, res, ex) -> {
                String requestedWith = req.getHeader("X-Requested-With");
                String accept = req.getHeader("Accept");
                if ("XMLHttpRequest".equalsIgnoreCase(requestedWith) || (accept != null && accept.contains("application/json"))) {
                    res.setContentType("application/json;charset=UTF-8");
                    res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    res.getWriter().write("{\"ok\":false,\"mensaje\":\"Correo o contraseña incorrectos.\"}");
                } else {
                    res.sendRedirect("/inicio-sesion?error=true");
                }
            })
            .permitAll()
        )
        .logout(logout -> logout
            .logoutUrl("/cerrar-sesion")
            .logoutSuccessUrl("/")
            .permitAll()
        )
        .exceptionHandling(ex -> ex
            .accessDeniedPage("/?error=acceso_denegado")
        );

        return http.build();
    }

}

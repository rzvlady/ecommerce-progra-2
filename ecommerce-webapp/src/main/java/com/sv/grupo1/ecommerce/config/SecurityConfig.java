package com.sv.grupo1.ecommerce.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
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
@EnableMethodSecurity(prePostEnabled = true, securedEnabled = true, jsr250Enabled = true)
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
            .requestMatchers("/api/auth/**").permitAll()

            .requestMatchers("/backoffice/**", "/admin/**").hasRole("ADMIN")

            // 3. Rutas web públicas
            .requestMatchers(
                    "/",
                    "/registro",
                    "/inicio-sesion",
                    "/css/**", "/js/**", "/ecommerce/**",
                    "/uploads/**",
                    "/carrito", "/categorias", "/categorias/**",
                    "/producto/**", "/productos", "/productos/**"
            ).permitAll()
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
        )
        .exceptionHandling(ex -> ex
            .accessDeniedPage("/?error=acceso_denegado")
        );

        return http.build();
    }
}

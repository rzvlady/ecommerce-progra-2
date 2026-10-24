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

            .csrf(csrf -> csrf.disable())

            .authorizeHttpRequests(auth -> auth

                .requestMatchers("/api/auth/**","/productos/**", "/api/carrito/**").permitAll()


                .requestMatchers(
                    "/", "/registro", "/inicio-sesion",
                    "/css/**", "/js/**", "/ecommerce/**",
                    "/backoffice/**", "/uploads/**",
                    // Nuevas rutas del frontend cliente
                    "/carrito", "/categorias", "/categorias/**", "/producto/**"
                ).permitAll()
      

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

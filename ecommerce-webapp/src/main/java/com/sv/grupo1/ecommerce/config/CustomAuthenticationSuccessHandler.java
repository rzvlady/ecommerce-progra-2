package com.sv.grupo1.ecommerce.config;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Collection;

@Component
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private static final Logger LOG = LoggerFactory.getLogger(CustomAuthenticationSuccessHandler.class);
    private final RedirectStrategy redirectStrategy = new DefaultRedirectStrategy();

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException, ServletException {

        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();

        boolean tieneRolBackoffice = authorities.stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(rol -> rol.startsWith("ROLE_") && !"ROLE_CLIENTE".equals(rol));

        boolean esCliente = authorities.stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch("ROLE_CLIENTE"::equals);

        String targetUrl = "/";
        if (tieneRolBackoffice) {
            targetUrl = "/backoffice/dashboard";
        } else if (esCliente) {
            targetUrl = "/";
        }

        LOG.info("Usuario '{}' autenticado con roles {}. Redirigiendo a: {}",
                authentication.getName(), authorities, targetUrl);

        if (response.isCommitted()) {
            LOG.debug("La respuesta ya fue confirmada. No es posible redirigir a {}", targetUrl);
            return;
        }

        redirectStrategy.sendRedirect(request, response, targetUrl);
    }
}

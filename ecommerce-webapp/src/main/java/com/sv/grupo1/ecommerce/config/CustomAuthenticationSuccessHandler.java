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

        boolean esAdmin = authorities.stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(rol -> "ROLE_ADMIN".equalsIgnoreCase(rol) || "ADMIN".equalsIgnoreCase(rol));

        String targetUrl = esAdmin ? "/backoffice/dashboard" : "/";

        LOG.info("Usuario '{}' autenticado con roles {}. Es admin: {}. Redirigiendo a: {}",
                authentication.getName(), authorities, esAdmin, targetUrl);

        if (response.isCommitted()) {
            LOG.debug("La respuesta ya fue confirmada. No es posible redirigir a {}", targetUrl);
            return;
        }

        redirectStrategy.sendRedirect(request, response, targetUrl);
    }
}

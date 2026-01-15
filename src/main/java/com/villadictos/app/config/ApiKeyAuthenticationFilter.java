package com.villadictos.app.config;

import com.villadictos.app.service.ApiClientService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * Filtro que valida API Keys en el header X-API-Key
 * Solo aplica a rutas /api/**
 */
@Component
public class ApiKeyAuthenticationFilter extends OncePerRequestFilter {

    private static final String API_KEY_HEADER = "X-API-Key";

    private final ApiClientService apiClientService;

    public ApiKeyAuthenticationFilter(ApiClientService apiClientService) {
        this.apiClientService = apiClientService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        String apiKey = request.getHeader(API_KEY_HEADER);

        // Si hay API Key y la ruta es /api/**, validamos
        if (apiKey != null && request.getRequestURI().startsWith("/api/")) {
            if (apiClientService.validateAndRegisterUse(apiKey)) {
                // Autenticación exitosa con API Key
                UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                        "api-client",
                        null,
                        List.of(new SimpleGrantedAuthority("ROLE_API_CLIENT")));
                SecurityContextHolder.getContext().setAuthentication(auth);
            }
        }

        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        // Solo filtramos rutas /api/**
        return !request.getRequestURI().startsWith("/api/");
    }
}

package com.example.Inventario.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

public class ApiKeyAuthFilter extends OncePerRequestFilter {

    private final String authenticationHeaderName;
    private final AuthenticationManager authenticationManager;

    public ApiKeyAuthFilter(String authenticationHeaderName, AuthenticationManager authenticationManager) {
        this.authenticationHeaderName = authenticationHeaderName;
        this.authenticationManager = authenticationManager;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String apiKey = request.getHeader(authenticationHeaderName);

        if (apiKey == null || apiKey.isEmpty()) {
            // No hay API Key, permitir que Spring Security la maneje (Denegar acceso)
            filterChain.doFilter(request, response);
            return;
        }

        try {
            Authentication authentication = new ApiKeyAuthToken(apiKey, null);
            Authentication result = authenticationManager.authenticate(authentication);
            SecurityContextHolder.getContext().setAuthentication(result);
        } catch (Exception e) {
            // Si la autenticación falla, limpiar el contexto y enviar error 401
            SecurityContextHolder.clearContext();
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Acceso no autorizado: " + e.getMessage());
            return;
        }

        filterChain.doFilter(request, response);
    }
}

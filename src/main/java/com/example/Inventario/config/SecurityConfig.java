package com.example.Inventario.config;

import com.example.Inventario.security.ApiKeyAuthFilter;
import com.example.Inventario.security.ApiKeyAuthManager;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * SecurityConfig configura la seguridad de la aplicación usando Spring Security.
 * <p>
 * Protege los endpoints del servicio de inventario mediante autenticación por API Key,
 * deshabilita CSRF, establece la política de sesiones como stateless y permite el acceso
 * público a la documentación Swagger.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Value("${inventario-service.api-key}")
    private String inventoriesApiKey;

    private static final String API_KEY_AUTH_HEADER = "X-API-KEY";


    /**
     * Configura la cadena de filtros de seguridad para la aplicación.
     *
     * @param http objeto HttpSecurity para configurar la seguridad HTTP.
     * @return la cadena de filtros de seguridad configurada.
     * @throws Exception si ocurre un error durante la configuración.
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable())
                .exceptionHandling(eh -> eh
                        .authenticationEntryPoint((request, response, authException) -> {
                            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                            response.getWriter().write("Acceso no autorizado: Autenticación requerida.");
                        })
                )
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(new ApiKeyAuthFilter(API_KEY_AUTH_HEADER, new ApiKeyAuthManager(inventoriesApiKey)),
                        UsernamePasswordAuthenticationFilter.class)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**", "/webjars/**").permitAll()
                        .requestMatchers("/api/inventario/**").authenticated() // Proteger endpoints de Inventarios
                        .anyRequest().denyAll()
                );

        return http.build();
    }
}

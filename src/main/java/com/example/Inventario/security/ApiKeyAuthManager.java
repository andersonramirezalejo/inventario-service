package com.example.Inventario.security;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.AuthorityUtils;

public class ApiKeyAuthManager implements AuthenticationManager {

    private final String principalRequestHeader;

    public ApiKeyAuthManager(String principalRequestHeader) {
        this.principalRequestHeader = principalRequestHeader;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String apiKey = authentication.getPrincipal().toString();
        if (!apiKey.equals(principalRequestHeader)) {
            throw new BadCredentialsException("API Key inválida");
        }
        // Si la API Key es válida, se autentica el token
        return new ApiKeyAuthToken(apiKey, AuthorityUtils.NO_AUTHORITIES);
    }
}

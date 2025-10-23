package com.meli.inventory_service.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {

    /**
     * Clave secreta para firmar tokens JWT.
     * DEBE tener al menos 512 bits (64 caracteres) para el algoritmo HS512.
     */
    private String secret = "zxRIlfOPUK2b1FyilWEamtCPwM1MTJZUwTU0QhMWu4XTqkxhEgodmVOBs07NbJTGFXVtHPi9DVFg4VhPj8hOHA";

    /**
     * Tiempo de expiración del token en milisegundos.
     */
    private long expirationMs = 86400000; // 24 horas

    /**
     * Tiempo de expiración del token de refresco en milisegundos.
     */
    private long refreshExpirationMs = 604800000; // 7 días

    /**
     * Emisor del token.
     */
    private String issuer = "meli-inventory-service";

    /**
     * Prefijo para el header Authorization.
     */
    private String tokenPrefix = "Bearer ";

    /**
     * Nombre del header que contiene el token.
     */
    private String headerString = "Authorization";

    // Getters y Setters
    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public long getExpirationMs() {
        return expirationMs;
    }

    public void setExpirationMs(long expirationMs) {
        this.expirationMs = expirationMs;
    }

    public long getRefreshExpirationMs() {
        return refreshExpirationMs;
    }

    public void setRefreshExpirationMs(long refreshExpirationMs) {
        this.refreshExpirationMs = refreshExpirationMs;
    }

    public String getIssuer() {
        return issuer;
    }

    public void setIssuer(String issuer) {
        this.issuer = issuer;
    }

    public String getTokenPrefix() {
        return tokenPrefix;
    }

    public void setTokenPrefix(String tokenPrefix) {
        this.tokenPrefix = tokenPrefix;
    }

    public String getHeaderString() {
        return headerString;
    }

    public void setHeaderString(String headerString) {
        this.headerString = headerString;
    }
}

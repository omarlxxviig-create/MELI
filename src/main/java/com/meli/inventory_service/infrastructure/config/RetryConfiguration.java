package com.meli.inventory_service.infrastructure.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.retry.annotation.EnableRetry;

/**
 * Configuración para habilitar reintentos automáticos en caso de fallos
 * transitorios.
 * Especialmente útil para manejar conflictos de concurrencia optimista.
 */
@Configuration
@EnableRetry
public class RetryConfiguration {
    // Spring Retry se configura mediante anotaciones en los métodos
}

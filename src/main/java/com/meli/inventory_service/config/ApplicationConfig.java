package com.meli.inventory_service.config;

import com.fasterxml.jackson.databind.ObjectMapper;
// Commented out: Old inventory system
// import com.meli.inventory_service.application.service.InventoryUseCase;
// import com.meli.inventory_service.domain.ports.out.InventoryPort;
import com.meli.inventory_service.domain.ports.out.OutboxPort;
import com.meli.inventory_service.domain.ports.out.ReservationPort;
// import com.meli.inventory_service.infrastructure.metrics.InventoryMetrics;
// import com.meli.inventory_service.infrastructure.metrics.InventoryGauges;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ApplicationConfig {

    // TODO: Remove this old inventory bean after migration is complete
    /*
     * @Bean
     * public InventoryUseCase inventoryUseCase(InventoryPort inventoryPort,
     * ReservationPort reservationPort,
     * OutboxPort outboxPort,
     * ObjectMapper objectMapper,
     * InventoryMetrics inventoryMetrics,
     * InventoryGauges inventoryGauges,
     * 
     * @Value("${inventory.reservation.ttl.seconds:600}") long ttl) {
     * return new InventoryUseCase(
     * inventoryPort,
     * reservationPort,
     * outboxPort,
     * ttl,
     * objectMapper,
     * inventoryMetrics,
     * inventoryGauges);
     * }
     */
}

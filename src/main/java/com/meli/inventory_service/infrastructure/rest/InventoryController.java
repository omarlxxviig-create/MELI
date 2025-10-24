package com.meli.inventory_service.infrastructure.rest;

import com.meli.inventory_service.domain.ports.in.InventoryUseCasePort;
import com.meli.inventory_service.application.dto.ReserveRequest;
import com.meli.inventory_service.application.dto.ReserveResponse;
import com.meli.inventory_service.infrastructure.rest.dto.StoreInventoryResponse;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.dao.OptimisticLockingFailureException;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Controlador REST para operaciones de inventario.
 * Adaptador de entrada en la arquitectura hexagonal.
 */
@RestController
@RequestMapping("/api/inventory")
public class InventoryController {
    private static final Logger log = LoggerFactory.getLogger(InventoryController.class);
    private final InventoryUseCasePort useCase;

    public InventoryController(InventoryUseCasePort useCase) {
        this.useCase = useCase;
    }

    @PostMapping("/reserve")
    @CacheEvict(value = "inventories", allEntries = true)
    public ResponseEntity<?> reserve(@RequestBody ReserveRequest req) {
        try {
            // Validaciones básicas
            if (req.getStoreId() == null || req.getProductId() == null || req.getQuantity() <= 0) {
                return ResponseEntity.badRequest()
                        .body(new ErrorResponse("Invalid request parameters"));
            }

            ReserveResponse res = useCase.reserve(req);
            return ResponseEntity.ok(res);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(404)
                    .body(new ErrorResponse("Inventory not found: " + ex.getMessage()));
        } catch (IllegalStateException ex) {
            return ResponseEntity.status(409)
                    .body(new ErrorResponse("Insufficient stock: " + ex.getMessage()));
        } catch (Exception ex) {
            return ResponseEntity.status(500)
                    .body(new ErrorResponse("Internal error: " + ex.getMessage()));
        }
    }

    @PostMapping("/commit")
    @CacheEvict(value = "inventories", allEntries = true)
    @Retryable(value = {
            OptimisticLockingFailureException.class }, maxAttempts = 3, backoff = @Backoff(delay = 20, multiplier = 2, maxDelay = 200))
    public ResponseEntity<?> commit(@RequestParam String reservationId) {
        log.debug("Attempting commit for reservation: {}", reservationId);
        try {
            useCase.commit(reservationId);
            log.debug("Commit successful for: {}", reservationId);
            return ResponseEntity.ok().build();
        } catch (OptimisticLockingFailureException ex) {
            log.warn("Concurrency conflict for reservation {} after retries", reservationId);
            return ResponseEntity.status(409)
                    .body(new ErrorResponse("Concurrent modification detected. Please retry."));
        } catch (IllegalArgumentException ex) {
            log.error("Reservation not found: {}", reservationId);
            return ResponseEntity.status(404)
                    .body(new ErrorResponse("Reservation not found: " + ex.getMessage()));
        } catch (IllegalStateException ex) {
            log.error("Reservation expired or invalid state: {}", reservationId);
            return ResponseEntity.status(410)
                    .body(new ErrorResponse("Reservation expired or invalid: " + ex.getMessage()));
        } catch (Exception ex) {
            log.error("Error committing reservation {}: {}", reservationId, ex.getMessage(), ex);
            return ResponseEntity.status(500)
                    .body(new ErrorResponse("Internal error: " + ex.getMessage()));
        }
    }

    @PostMapping("/release")
    @CacheEvict(value = "inventories", allEntries = true)
    public ResponseEntity<Void> release(@RequestParam String reservationId,
            @RequestParam(required = false) String reason) {
        useCase.release(reservationId, reason);
        return ResponseEntity.ok().build();
    }

    @GetMapping
    public ResponseEntity<List<StoreInventoryResponse>> getAllInventories() {
        log.info("Fetching all inventories");
        List<StoreInventoryResponse> inventories = getCachedInventories();
        return ResponseEntity.ok(inventories);
    }

    @Cacheable(value = "inventories", unless = "#result == null || #result.isEmpty()")
    public List<StoreInventoryResponse> getCachedInventories() {
        log.info("Cache miss - fetching from database");
        return useCase.getAllInventories().stream()
                .map(StoreInventoryResponse::fromDomain)
                .collect(Collectors.toList());
    }

    // Clase interna para manejar errores
    private static class ErrorResponse {
        private final String message;

        public ErrorResponse(String message) {
            this.message = message;
        }

        public String getMessage() {
            return message;
        }
    }
}

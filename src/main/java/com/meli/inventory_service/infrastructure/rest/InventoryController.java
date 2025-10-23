package com.meli.inventory_service.infrastructure.rest;

import com.meli.inventory_service.domain.ports.in.InventoryUseCasePort;
import com.meli.inventory_service.application.dto.ReserveRequest;
import com.meli.inventory_service.application.dto.ReserveResponse;
import com.meli.inventory_service.infrastructure.rest.dto.StoreInventoryResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Controlador REST para operaciones de inventario.
 * Adaptador de entrada en la arquitectura hexagonal.
 */
@RestController
@RequestMapping("/api/inventory")
public class InventoryController {
    private final InventoryUseCasePort useCase;

    public InventoryController(InventoryUseCasePort useCase) {
        this.useCase = useCase;
    }

    @PostMapping("/reserve")
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
    public ResponseEntity<Void> commit(@RequestParam String reservationId) {
        useCase.commit(reservationId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/release")
    public ResponseEntity<Void> release(@RequestParam String reservationId,
            @RequestParam(required = false) String reason) {
        useCase.release(reservationId, reason);
        return ResponseEntity.ok().build();
    }

    @GetMapping
    public ResponseEntity<List<StoreInventoryResponse>> getAllInventories() {
        return ResponseEntity.ok(
                useCase.getAllInventories().stream()
                        .map(StoreInventoryResponse::fromDomain)
                        .collect(Collectors.toList()));
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

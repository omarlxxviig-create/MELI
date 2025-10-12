package com.meli.inventory_service.domain.ports.in;

import com.meli.inventory_service.domain.model.StoreInventory;
import com.meli.inventory_service.application.dto.ReserveRequest;
import com.meli.inventory_service.application.dto.ReserveResponse;

import java.util.List;

/**
 * Puerto de entrada para casos de uso de inventario.
 * Define las operaciones que puede realizar la capa de aplicación.
 */
public interface InventoryUseCasePort {
    ReserveResponse reserve(ReserveRequest req);

    void commit(String reservationId);

    void release(String reservationId, String reason);

    List<StoreInventory> getAllInventories();
}

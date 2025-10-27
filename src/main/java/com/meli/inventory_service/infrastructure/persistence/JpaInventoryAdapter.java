package com.meli.inventory_service.infrastructure.persistence;

import com.meli.inventory_service.domain.ports.out.InventoryPort;
import com.meli.inventory_service.domain.model.StoreInventory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.util.Optional;
import java.util.List;

@Component
@Transactional
public class JpaInventoryAdapter implements InventoryPort {
    private final StoreInventoryRepository repo;

    public JpaInventoryAdapter(StoreInventoryRepository repo) {
        this.repo = repo;
    }

    @Override
    public Optional<StoreInventory> findByStoreAndProduct(String storeId, String productId) {
        return repo.findByStoreIdAndProductId(storeId, productId);
    }

    @Override
    public StoreInventory save(StoreInventory inventory) {
        return repo.save(inventory);
    }

    @Override
    public List<StoreInventory> findAll() {
        return repo.findAll();
    }
}

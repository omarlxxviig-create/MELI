package com.meli.inventory_service.infrastructure.persistence;

import com.meli.inventory_service.domain.model.StoreInventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.stereotype.Repository;
import jakarta.persistence.LockModeType;
import java.util.Optional;

@Repository
public interface StoreInventoryRepository extends JpaRepository<StoreInventory, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<StoreInventory> findByStoreIdAndProductId(String storeId, String productId);

    boolean existsByStoreIdAndProductId(String storeId, String productId);
}

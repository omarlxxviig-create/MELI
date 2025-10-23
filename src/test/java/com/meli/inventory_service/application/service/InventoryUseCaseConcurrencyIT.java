package com.meli.inventory_service.application.service;

import com.meli.inventory_service.domain.model.StoreInventory;
import com.meli.inventory_service.infrastructure.persistence.StoreInventoryRepository;
import com.meli.inventory_service.application.dto.ReserveRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class InventoryUseCaseConcurrencyIT {

    @Autowired
    private InventoryUseCase inventoryUseCase;

    @Autowired
    private StoreInventoryRepository inventoryRepository;

    @Test
    @Sql("/test-data.sql") // Create this file with initial inventory data
    void concurrentReservations() throws InterruptedException {
        // Arrange
        String storeId = "store-1";
        String productId = "sku-1";
        int initialQuantity = 1000;
        int numThreads = 20;
        int quantityPerReservation = 2;

        // Initialize inventory
        StoreInventory inventory = new StoreInventory();
        inventory.setStoreId(storeId);
        inventory.setProductId(productId);
        inventory.setTotalQuantity(initialQuantity);
        inventory.setReservedQuantity(0);
        inventoryRepository.save(inventory);

        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch endLatch = new CountDownLatch(numThreads);
        List<Exception> exceptions = new ArrayList<>();

        // Act
        ExecutorService executor = Executors.newFixedThreadPool(numThreads);
        for (int i = 0; i < numThreads; i++) {
            final String txId = "tx-" + i;
            executor.submit(() -> {
                try {
                    startLatch.await(); // Wait for all threads to be ready
                    ReserveRequest request = new ReserveRequest();
                    request.setStoreId(storeId);
                    request.setProductId(productId);
                    request.setQuantity(quantityPerReservation);
                    request.setTransactionId(txId);
                    inventoryUseCase.reserve(request);
                } catch (Exception e) {
                    exceptions.add(e);
                } finally {
                    endLatch.countDown();
                }
            });
        }

        startLatch.countDown(); // Start all threads
        endLatch.await(10, TimeUnit.SECONDS); // Wait for all threads to finish
        executor.shutdown();

        // Assert
        StoreInventory finalInventory = inventoryRepository.findByStoreIdAndProductId(storeId, productId).orElseThrow();
        assertTrue(exceptions.isEmpty(), "No exceptions should occur");
        assertTrue(finalInventory.getReservedQuantity() <= initialQuantity,
                "Reserved quantity should not exceed total quantity");
        assertEquals(initialQuantity, finalInventory.getTotalQuantity(),
                "Total quantity should remain unchanged");
    }
}

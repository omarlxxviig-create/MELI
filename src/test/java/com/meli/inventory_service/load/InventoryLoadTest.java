package com.meli.inventory_service.load;

import com.meli.inventory_service.domain.model.StoreInventory;
import com.meli.inventory_service.infrastructure.persistence.StoreInventoryRepository;
import com.meli.inventory_service.infrastructure.rest.dto.ReserveRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Disabled;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.test.context.jdbc.Sql;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * DEPRECATED: Load test for old inventory system
 * See BookingUseCaseConcurrencyIT for new transport booking concurrency tests
 */
@Disabled("Old inventory system - replaced by transport booking system")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class InventoryLoadTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private StoreInventoryRepository inventoryRepository;

    @Test
    @Sql("/test-data.sql")
    public void highConcurrencyReservationTest() throws InterruptedException {
        int numThreads = 50;
        int requestsPerThread = 20;
        ExecutorService executor = Executors.newFixedThreadPool(numThreads);
        CountDownLatch latch = new CountDownLatch(numThreads * requestsPerThread);
        List<Future<Boolean>> futures = new ArrayList<>();

        // Initialize test data
        String storeId = "store-1";
        String productId = "sku-1";
        int initialStock = 1000;

        StoreInventory inventory = new StoreInventory();
        inventory.setStoreId(storeId);
        inventory.setProductId(productId);
        inventory.setTotalQuantity(initialStock);
        inventory.setReservedQuantity(0);
        inventoryRepository.save(inventory);

        // Launch concurrent requests
        for (int i = 0; i < numThreads; i++) {
            final int threadId = i;
            futures.add(executor.submit(() -> {
                for (int j = 0; j < requestsPerThread; j++) {
                    try {
                        ReserveRequest request = new ReserveRequest();
                        request.setStoreId(storeId);
                        request.setProductId(productId);
                        request.setQuantity(1);
                        request.setTransactionId("load-test-" + threadId + "-" + j);

                        restTemplate.postForEntity("/api/inventory/reserve", request, String.class);
                        latch.countDown();
                        return true;
                    } catch (Exception e) {
                        e.printStackTrace();
                        return false;
                    }
                }
                return true;
            }));
        }

        // Wait for all requests to complete
        latch.await(30, TimeUnit.SECONDS);
        executor.shutdown();

        // Verify final state
        StoreInventory finalInventory = inventoryRepository.findByStoreIdAndProductId(storeId, productId).orElseThrow();
        assertTrue(finalInventory.getReservedQuantity() <= initialStock,
                "Reserved quantity should not exceed initial stock");
    }
}

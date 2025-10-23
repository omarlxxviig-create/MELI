package com.meli.inventory_service.application.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.meli.inventory_service.domain.model.StoreInventory;
import com.meli.inventory_service.domain.ports.out.InventoryPort;
import com.meli.inventory_service.domain.ports.out.OutboxPort;
import com.meli.inventory_service.domain.ports.out.ReservationPort;
import com.meli.inventory_service.application.dto.ReserveRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class InventoryUseCaseTest {

    @Mock
    private InventoryPort inventoryPort;
    @Mock
    private ReservationPort reservationPort;
    @Mock
    private OutboxPort outboxPort;
    private ObjectMapper objectMapper;
    private InventoryUseCase useCase;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // Configure ObjectMapper with JavaTimeModule
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        useCase = new InventoryUseCase(inventoryPort, reservationPort, outboxPort, 600, objectMapper, null, null);
    }

    @Test
    void reserveSuccess() {
        // Arrange
        StoreInventory inventory = new StoreInventory();
        inventory.setStoreId("store-1");
        inventory.setProductId("sku-1");
        inventory.setTotalQuantity(10);
        inventory.setReservedQuantity(0);

        when(inventoryPort.findByStoreAndProduct("store-1", "sku-1"))
                .thenReturn(Optional.of(inventory));
        when(inventoryPort.save(any())).thenReturn(inventory);
        when(reservationPort.save(any())).thenAnswer(i -> i.getArguments()[0]);

        ReserveRequest request = new ReserveRequest();
        request.setStoreId("store-1");
        request.setProductId("sku-1");
        request.setQuantity(5);
        request.setTransactionId("tx-1");

        // Act
        var response = useCase.reserve(request);

        // Assert
        assertNotNull(response);
        assertEquals("PENDING", response.getStatus());
        verify(inventoryPort).save(argThat(inv -> inv.getReservedQuantity() == 5));
    }

    @Test
    void reserveInsufficientStock() {
        // Arrange
        StoreInventory inventory = new StoreInventory();
        inventory.setStoreId("store-1");
        inventory.setProductId("sku-1");
        inventory.setTotalQuantity(10);
        inventory.setReservedQuantity(8);

        when(inventoryPort.findByStoreAndProduct("store-1", "sku-1"))
                .thenReturn(Optional.of(inventory));

        ReserveRequest request = new ReserveRequest();
        request.setStoreId("store-1");
        request.setProductId("sku-1");
        request.setQuantity(5);
        request.setTransactionId("tx-1");

        // Act & Assert
        assertThrows(IllegalStateException.class, () -> useCase.reserve(request));
        verify(inventoryPort, never()).save(any());
        verify(reservationPort, never()).save(any());
    }
}

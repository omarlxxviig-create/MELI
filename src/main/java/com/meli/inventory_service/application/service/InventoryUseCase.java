package com.meli.inventory_service.application.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.meli.inventory_service.domain.ports.out.*;
import com.meli.inventory_service.domain.ports.in.InventoryUseCasePort;
import com.meli.inventory_service.infrastructure.metrics.InventoryGauges;
import com.meli.inventory_service.infrastructure.metrics.InventoryMetrics;
import com.meli.inventory_service.application.dto.ReserveRequest;
import com.meli.inventory_service.application.dto.ReserveResponse;
import com.meli.inventory_service.domain.model.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Isolation;

import java.time.Instant;
import java.util.List;

public class InventoryUseCase implements InventoryUseCasePort {
    private static final Logger log = LoggerFactory.getLogger(InventoryUseCase.class);
    private final InventoryPort inventoryPort;
    private final ReservationPort reservationPort;
    private final OutboxPort outboxPort;
    private final long reservationTtlSeconds;
    private final ObjectMapper objectMapper;
    private final InventoryMetrics inventoryMetrics;
    private final InventoryGauges inventoryGauges;

    public InventoryUseCase(
            InventoryPort inventoryPort,
            ReservationPort reservationPort,
            OutboxPort outboxPort,
            long reservationTtlSeconds,
            ObjectMapper objectMapper,
            InventoryMetrics inventoryMetrics,
            InventoryGauges inventoryGauges) {
        this.inventoryPort = inventoryPort;
        this.reservationPort = reservationPort;
        this.outboxPort = outboxPort;
        this.reservationTtlSeconds = reservationTtlSeconds;
        this.objectMapper = objectMapper; // Inject ObjectMapper
        this.inventoryMetrics = inventoryMetrics;
        this.inventoryGauges = inventoryGauges;
        // Initialize gauge with current total
        updateAvailableGauge();
    }

    private void updateAvailableGauge() {
        int totalAvailable = inventoryPort.findAll().stream()
                .mapToInt(StoreInventory::getAvailable)
                .sum();
        inventoryGauges.setAvailable(totalAvailable);
    }

    private int calculateTotalAvailableAcrossStoresOrThisStore() {
        return inventoryPort.findAll()
                .stream()
                .mapToInt(StoreInventory::getAvailable)
                .sum();
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public ReserveResponse reserve(ReserveRequest req) {
        log.info("Starting reserve request for storeId={}, productId={}, quantity={}, transactionId={}",
                req.getStoreId(), req.getProductId(), req.getQuantity(), req.getTransactionId());

        int maxAttempts = 3;
        long backoffMs = 50;
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                return doReserveTransactional(req);
            } catch (org.springframework.orm.ObjectOptimisticLockingFailureException ex) {
                if (attempt == maxAttempts)
                    throw ex;
                try {
                    Thread.sleep(backoffMs);
                } catch (InterruptedException ignored) {
                }
                backoffMs *= 2;
            }
        }
        throw new RuntimeException("Could not reserve after retries");
    }

    @Transactional
    protected ReserveResponse doReserveTransactional(ReserveRequest req) {
        var invOpt = inventoryPort.findByStoreAndProduct(req.getStoreId(), req.getProductId());
        var inv = invOpt.orElseThrow(() -> new IllegalArgumentException("Inventory not found"));

        int available = inv.getAvailable();
        if (available < req.getQuantity()) {
            throw new IllegalStateException("Insufficient stock");
        }

        inv.setReservedQuantity(inv.getReservedQuantity() + req.getQuantity());
        inv.setUpdatedAt(Instant.now());
        inventoryPort.save(inv);
        updateAvailableGauge();

        Reservation r = new Reservation();
        r.setStoreId(req.getStoreId());
        r.setProductId(req.getProductId());
        r.setQuantity(req.getQuantity());
        r.setTransactionId(req.getTransactionId());
        r.setStatus(Reservation.Status.PENDING);
        r.setCreatedAt(Instant.now());
        r.setExpiresAt(Instant.now().plusSeconds(reservationTtlSeconds));
        reservationPort.save(r);

        // Increment metrics after successful save
        inventoryMetrics.incrementReservations();

        log.info("Created reservation id={}, transactionId={}, storeId={}, productId={}, quantity={}",
                r.getReservationId(), r.getTransactionId(), r.getStoreId(), r.getProductId(), r.getQuantity());

        try {
            String payload = objectMapper.writeValueAsString(r);
            OutboxMessage m = new OutboxMessage();
            m.setAggregateType("Reservation");
            m.setAggregateId(r.getReservationId());
            m.setTopic("inventory.reserved");
            m.setPayload(payload);
            outboxPort.save(m);
            log.debug("Created outbox message for reservation={}, topic={}",
                    r.getReservationId(), "inventory.reserved");
        } catch (Exception e) {
            log.error("Failed to create outbox message for reservation={}: {}",
                    r.getReservationId(), e.getMessage());
            throw new RuntimeException(e);
        }

        return new ReserveResponse(r.getReservationId(), r.getStatus().name(), r.getExpiresAt());
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void commit(String reservationId) {
        var r = reservationPort.findById(reservationId).orElseThrow();
        log.info("Starting commit for reservation={}, transactionId={}",
                reservationId, r.getTransactionId());

        if (r.getStatus() != Reservation.Status.PENDING) {
            log.warn("Cannot commit reservation={} in status {}",
                    reservationId, r.getStatus());
            throw new IllegalStateException("Reservation not pending");
        }

        var inv = inventoryPort.findByStoreAndProduct(r.getStoreId(), r.getProductId()).orElseThrow();
        inv.setTotalQuantity(inv.getTotalQuantity() - r.getQuantity());
        inv.setReservedQuantity(inv.getReservedQuantity() - r.getQuantity());
        inv.setUpdatedAt(Instant.now());
        inventoryPort.save(inv);
        inventoryGauges.setAvailable(calculateTotalAvailableAcrossStoresOrThisStore());

        r.setStatus(Reservation.Status.COMMITTED);
        reservationPort.save(r);

        try {
            String payload = objectMapper.writeValueAsString(r);
            OutboxMessage m = new OutboxMessage();
            m.setAggregateType("Reservation");
            m.setAggregateId(r.getReservationId());
            m.setTopic("inventory.committed");
            m.setPayload(payload);
            outboxPort.save(m);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        log.info("Committed reservation={}, transactionId={}",
                reservationId, r.getTransactionId());
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void release(String reservationId, String reason) {
        var r = reservationPort.findById(reservationId).orElseThrow();
        log.info("Starting release for reservation={}, transactionId={}, reason={}",
                reservationId, r.getTransactionId(), reason);

        if (r.getStatus() != Reservation.Status.PENDING) {
            log.debug("Skipping release of reservation={} in status {}",
                    reservationId, r.getStatus());
            return;
        }

        var inv = inventoryPort.findByStoreAndProduct(r.getStoreId(), r.getProductId()).orElseThrow();
        inv.setReservedQuantity(inv.getReservedQuantity() - r.getQuantity());
        inv.setUpdatedAt(Instant.now());
        inventoryPort.save(inv);
        inventoryGauges.setAvailable(calculateTotalAvailableAcrossStoresOrThisStore());

        r.setStatus(Reservation.Status.RELEASED);
        r.setExpiresAt(Instant.now());
        reservationPort.save(r);

        try {
            String payload = objectMapper.writeValueAsString(r);
            OutboxMessage m = new OutboxMessage();
            m.setAggregateType("Reservation");
            m.setAggregateId(r.getReservationId());
            m.setTopic("inventory.released");
            m.setPayload(payload);
            outboxPort.save(m);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        log.info("Released reservation={}, transactionId={}, reason={}",
                reservationId, r.getTransactionId(), reason);
    }

    @Override
    @Transactional(readOnly = true)
    public List<StoreInventory> getAllInventories() {
        return inventoryPort.findAll();
    }
}
